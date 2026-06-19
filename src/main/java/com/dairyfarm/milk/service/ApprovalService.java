package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.ApprovalNodeStatusEnum;
import com.dairyfarm.milk.common.enums.ApprovalStatusEnum;
import com.dairyfarm.milk.common.enums.ApprovalTypeEnum;
import com.dairyfarm.milk.common.enums.OperationTypeEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.dto.ApprovalHandleDTO;
import com.dairyfarm.milk.dto.ApprovalQueryDTO;
import com.dairyfarm.milk.dto.ApprovalSubmitDTO;
import com.dairyfarm.milk.entity.*;
import com.dairyfarm.milk.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalProcessMapper approvalProcessMapper;
    private final ApprovalNodeMapper approvalNodeMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final AntibioticTestMapper antibioticTestMapper;
    private final RejectRecordMapper rejectRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public ApprovalProcess submitApproval(String approvalType, Long bizId, String bizNo, String title, String remark) {
        ApprovalTypeEnum typeEnum = ApprovalTypeEnum.getByCode(approvalType);
        if (typeEnum == null) {
            throw new BusinessException("审批类型不存在");
        }

        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        SysUser submitter = sysUserMapper.selectById(currentUserId);
        if (submitter == null) {
            throw new BusinessException("用户不存在");
        }

        ApprovalProcess process = new ApprovalProcess();
        process.setApprovalType(approvalType);
        process.setBizId(bizId);
        process.setBizNo(bizNo);
        process.setBizType(typeEnum.getDesc());
        process.setTitle(title);
        process.setApprovalStatus(ApprovalStatusEnum.PENDING.getCode());
        process.setCurrentNode(0);
        process.setTotalNodes(typeEnum.getApprovalLevelCount());
        process.setSubmitterId(currentUserId);
        process.setSubmitterName(submitter.getRealName());
        process.setRemark(remark);
        approvalProcessMapper.insert(process);

        createApprovalNodes(process.getId(), typeEnum);

        createApprovalRecord(process.getId(), null, OperationTypeEnum.SUBMIT.getCode(),
                currentUserId, submitter.getRealName(), remark);

        activateNextNode(process);

        log.info("提交审批成功，审批流ID: {}, 类型: {}", process.getId(), typeEnum.getDesc());
        return process;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApprovalProcess submitApproval(ApprovalSubmitDTO dto) {
        return submitApproval(dto.getApprovalType(), dto.getBizId(), dto.getBizNo(),
                dto.getTitle(), dto.getRemark());
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(ApprovalHandleDTO dto) {
        Long currentUserId = UserContext.getUserId();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        ApprovalProcess process = approvalProcessMapper.selectById(dto.getProcessId());
        if (process == null) {
            throw new BusinessException("审批流不存在");
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(process.getApprovalStatus())) {
            throw new BusinessException("审批流当前状态不允许审批");
        }

        ApprovalNode currentNode = approvalNodeMapper.selectById(dto.getNodeId());
        if (currentNode == null) {
            throw new BusinessException("审批节点不存在");
        }

        if (!process.getId().equals(currentNode.getProcessId())) {
            throw new BusinessException("审批节点不属于该审批流");
        }

        if (!ApprovalNodeStatusEnum.PENDING.getCode().equals(currentNode.getNodeStatus()) &&
                !ApprovalNodeStatusEnum.PROCESSING.getCode().equals(currentNode.getNodeStatus())) {
            throw new BusinessException("该节点当前状态不允许审批");
        }

        if (!isUserApprover(currentUser, currentNode, process)) {
            throw new BusinessException("您没有该节点的审批权限");
        }

        currentNode.setApprovalResult(ApprovalStatusEnum.APPROVED.getCode());
        currentNode.setApprovalRemark(dto.getRemark());
        currentNode.setApprovalTime(LocalDateTime.now());
        currentNode.setApproverId(currentUserId);
        currentNode.setApproverName(currentUser.getRealName());
        currentNode.setNodeStatus(ApprovalNodeStatusEnum.COMPLETED.getCode());
        approvalNodeMapper.updateById(currentNode);

        createApprovalRecord(process.getId(), currentNode.getId(), OperationTypeEnum.APPROVE.getCode(),
                currentUserId, currentUser.getRealName(), dto.getRemark());

        if (currentNode.getNodeOrder() < process.getTotalNodes() - 1) {
            process.setCurrentNode(currentNode.getNodeOrder() + 1);
            approvalProcessMapper.updateById(process);
            activateNextNode(process);
        } else {
            process.setApprovalStatus(ApprovalStatusEnum.APPROVED.getCode());
            process.setCompleteTime(LocalDateTime.now());
            approvalProcessMapper.updateById(process);
            handleApprovalComplete(process);
        }

        log.info("审批通过，审批流ID: {}, 节点: {}", process.getId(), currentNode.getNodeOrder());
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(ApprovalHandleDTO dto) {
        Long currentUserId = UserContext.getUserId();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        ApprovalProcess process = approvalProcessMapper.selectById(dto.getProcessId());
        if (process == null) {
            throw new BusinessException("审批流不存在");
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(process.getApprovalStatus())) {
            throw new BusinessException("审批流当前状态不允许审批");
        }

        ApprovalNode currentNode = approvalNodeMapper.selectById(dto.getNodeId());
        if (currentNode == null) {
            throw new BusinessException("审批节点不存在");
        }

        if (!process.getId().equals(currentNode.getProcessId())) {
            throw new BusinessException("审批节点不属于该审批流");
        }

        if (!ApprovalNodeStatusEnum.PENDING.getCode().equals(currentNode.getNodeStatus()) &&
                !ApprovalNodeStatusEnum.PROCESSING.getCode().equals(currentNode.getNodeStatus())) {
            throw new BusinessException("该节点当前状态不允许审批");
        }

        if (!isUserApprover(currentUser, currentNode, process)) {
            throw new BusinessException("您没有该节点的审批权限");
        }

        currentNode.setApprovalResult(ApprovalStatusEnum.REJECTED.getCode());
        currentNode.setApprovalRemark(dto.getRemark());
        currentNode.setApprovalTime(LocalDateTime.now());
        currentNode.setApproverId(currentUserId);
        currentNode.setApproverName(currentUser.getRealName());
        currentNode.setNodeStatus(ApprovalNodeStatusEnum.COMPLETED.getCode());
        approvalNodeMapper.updateById(currentNode);

        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ApprovalNode::getProcessId, process.getId())
                .gt(ApprovalNode::getNodeOrder, currentNode.getNodeOrder());
        List<ApprovalNode> remainingNodes = approvalNodeMapper.selectList(nodeWrapper);
        for (ApprovalNode node : remainingNodes) {
            node.setNodeStatus(ApprovalNodeStatusEnum.SKIPPED.getCode());
            approvalNodeMapper.updateById(node);
        }

        process.setApprovalStatus(ApprovalStatusEnum.REJECTED.getCode());
        process.setCompleteTime(LocalDateTime.now());
        approvalProcessMapper.updateById(process);

        createApprovalRecord(process.getId(), currentNode.getId(), OperationTypeEnum.REJECT.getCode(),
                currentUserId, currentUser.getRealName(), dto.getRemark());

        handleApprovalReject(process);

        log.info("审批驳回，审批流ID: {}, 节点: {}", process.getId(), currentNode.getNodeOrder());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelApproval(Long processId) {
        Long currentUserId = UserContext.getUserId();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        ApprovalProcess process = approvalProcessMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("审批流不存在");
        }

        if (!process.getSubmitterId().equals(currentUserId)) {
            throw new BusinessException("只有提交人可以取消审批");
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(process.getApprovalStatus())) {
            throw new BusinessException("只有待审批状态的流程可以取消");
        }

        process.setApprovalStatus(ApprovalStatusEnum.CANCELLED.getCode());
        process.setCompleteTime(LocalDateTime.now());
        approvalProcessMapper.updateById(process);

        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ApprovalNode::getProcessId, processId);
        List<ApprovalNode> nodes = approvalNodeMapper.selectList(nodeWrapper);
        for (ApprovalNode node : nodes) {
            if (ApprovalNodeStatusEnum.PENDING.getCode().equals(node.getNodeStatus()) ||
                    ApprovalNodeStatusEnum.PROCESSING.getCode().equals(node.getNodeStatus())) {
                node.setNodeStatus(ApprovalNodeStatusEnum.SKIPPED.getCode());
                approvalNodeMapper.updateById(node);
            }
        }

        createApprovalRecord(processId, null, OperationTypeEnum.CANCEL.getCode(),
                currentUserId, currentUser.getRealName(), null);

        log.info("取消审批，审批流ID: {}", processId);
    }

    public Map<String, Object> getApprovalDetail(Long processId) {
        ApprovalProcess process = approvalProcessMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("审批流不存在");
        }

        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ApprovalNode::getProcessId, processId)
                .orderByAsc(ApprovalNode::getNodeOrder);
        List<ApprovalNode> nodes = approvalNodeMapper.selectList(nodeWrapper);

        LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ApprovalRecord::getProcessId, processId)
                .orderByDesc(ApprovalRecord::getOperateTime);
        List<ApprovalRecord> records = approvalRecordMapper.selectList(recordWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("process", process);
        result.put("nodes", nodes);
        result.put("records", records);
        return result;
    }

    public Page<ApprovalProcess> getMyApprovalList(ApprovalQueryDTO dto) {
        Long currentUserId = UserContext.getUserId();
        String currentUserRole = UserContext.getRoleCode();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.and(wrapper -> wrapper
                        .eq(ApprovalNode::getApproverId, currentUserId)
                        .or()
                        .eq(ApprovalNode::getApprovalRole, currentUserRole))
                .in(ApprovalNode::getNodeStatus,
                        ApprovalNodeStatusEnum.PENDING.getCode(),
                        ApprovalNodeStatusEnum.PROCESSING.getCode());

        List<ApprovalNode> myNodes = approvalNodeMapper.selectList(nodeWrapper);
        if (myNodes.isEmpty()) {
            return new Page<>(dto.getPageNum(), dto.getPageSize());
        }

        Set<Long> processIds = new HashSet<>();
        for (ApprovalNode node : myNodes) {
            processIds.add(node.getProcessId());
        }

        LambdaQueryWrapper<ApprovalProcess> processWrapper = new LambdaQueryWrapper<>();
        processWrapper.in(ApprovalProcess::getId, processIds)
                .eq(ApprovalProcess::getApprovalStatus, ApprovalStatusEnum.PENDING.getCode());

        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            processWrapper.eq(ApprovalProcess::getApprovalStatus, dto.getStatus());
        }
        if (dto.getType() != null && !dto.getType().isEmpty()) {
            processWrapper.eq(ApprovalProcess::getApprovalType, dto.getType());
        }
        processWrapper.orderByDesc(ApprovalProcess::getCreateTime);

        return approvalProcessMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), processWrapper);
    }

    public Page<ApprovalProcess> getMyAppliedList(ApprovalQueryDTO dto) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        LambdaQueryWrapper<ApprovalProcess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProcess::getSubmitterId, currentUserId);

        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            wrapper.eq(ApprovalProcess::getApprovalStatus, dto.getStatus());
        }
        if (dto.getType() != null && !dto.getType().isEmpty()) {
            wrapper.eq(ApprovalProcess::getApprovalType, dto.getType());
        }
        wrapper.orderByDesc(ApprovalProcess::getCreateTime);

        return approvalProcessMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }

    public List<Map<String, Object>> getApprovalHistory(Long bizId, String approvalType) {
        LambdaQueryWrapper<ApprovalProcess> processWrapper = new LambdaQueryWrapper<>();
        processWrapper.eq(ApprovalProcess::getBizId, bizId)
                .eq(ApprovalProcess::getApprovalType, approvalType)
                .orderByDesc(ApprovalProcess::getCreateTime);

        List<ApprovalProcess> processes = approvalProcessMapper.selectList(processWrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ApprovalProcess process : processes) {
            Map<String, Object> detail = getApprovalDetail(process.getId());
            result.add(detail);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApprovalProcess createTestResultApproval(Long testId) {
        AntibioticTest test = antibioticTestMapper.selectById(testId);
        if (test == null) {
            throw new BusinessException("检测记录不存在");
        }

        String title = "抗生素检测结果审批 - " + test.getTestNo();
        String remark = "检测类型: " + test.getTestType() + ", 检测结果: " + test.getTestResult();

        test.setApproveStatus(ApprovalStatusEnum.PENDING.getCode());
        test.setApproverId(null);
        test.setApproveTime(null);
        test.setApproveRemark(null);
        antibioticTestMapper.updateById(test);

        return submitApproval(ApprovalTypeEnum.TEST_RESULT.getCode(),
                testId, test.getTestNo(), title, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public ApprovalProcess createRejectHandleApproval(Long rejectId) {
        RejectRecord reject = rejectRecordMapper.selectById(rejectId);
        if (reject == null) {
            throw new BusinessException("拒收记录不存在");
        }

        String title = "拒收处理审批 - " + reject.getRejectNo();
        String remark = reject.getRejectReason();

        return submitApproval(ApprovalTypeEnum.REJECT_HANDLE.getCode(),
                rejectId, reject.getRejectNo(), title, remark);
    }

    private void createApprovalNodes(Long processId, ApprovalTypeEnum typeEnum) {
        List<String> roles = typeEnum.getApprovalRoles();
        for (int i = 0; i < roles.size(); i++) {
            ApprovalNode node = new ApprovalNode();
            node.setProcessId(processId);
            node.setNodeOrder(i);
            node.setNodeName(getNodeName(i, typeEnum));
            node.setApprovalRole(roles.get(i));
            node.setNodeStatus(ApprovalNodeStatusEnum.PENDING.getCode());
            approvalNodeMapper.insert(node);
        }
    }

    private String getNodeName(int level, ApprovalTypeEnum typeEnum) {
        switch (typeEnum) {
            case TEST_RESULT:
                return level == 0 ? "化验室主管审批" : "质量主管审批";
            case REJECT_HANDLE:
                return level == 0 ? "牧场主管审批" : "质量主管审批";
            case EXCEPTION:
                if (level == 0) return "现场主管审批";
                if (level == 1) return "部门经理审批";
                return "质量总监审批";
            default:
                return "第" + (level + 1) + "级审批";
        }
    }

    private void activateNextNode(ApprovalProcess process) {
        int currentNodeOrder = process.getCurrentNode();
        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ApprovalNode::getProcessId, process.getId())
                .eq(ApprovalNode::getNodeOrder, currentNodeOrder);
        ApprovalNode nextNode = approvalNodeMapper.selectOne(nodeWrapper);

        if (nextNode != null) {
            nextNode.setNodeStatus(ApprovalNodeStatusEnum.PROCESSING.getCode());
            approvalNodeMapper.updateById(nextNode);
        }
    }

    private boolean isUserApprover(SysUser user, ApprovalNode node, ApprovalProcess process) {
        if (node.getApproverId() != null) {
            return node.getApproverId().equals(user.getId());
        }

        return node.getApprovalRole() != null && node.getApprovalRole().equals(user.getRoleCode());
    }

    private void createApprovalRecord(Long processId, Long nodeId, String operationType,
                                      Long operatorId, String operatorName, String remark) {
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessId(processId);
        record.setNodeId(nodeId);
        record.setOperationType(operationType);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setRemark(remark);
        record.setOperateTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    private void handleApprovalComplete(ApprovalProcess process) {
        if (ApprovalTypeEnum.TEST_RESULT.getCode().equals(process.getApprovalType())) {
            AntibioticTest test = antibioticTestMapper.selectById(process.getBizId());
            if (test != null) {
                test.setApproveStatus(ApprovalStatusEnum.APPROVED.getCode());
                test.setApproverId(UserContext.getUserId());
                test.setApproveTime(LocalDateTime.now());
                test.setApproveRemark("审批通过");
                antibioticTestMapper.updateById(test);
                log.info("检测结果审批通过，更新检测记录状态，检测ID: {}", test.getId());
            }
        }
    }

    private void handleApprovalReject(ApprovalProcess process) {
        if (ApprovalTypeEnum.TEST_RESULT.getCode().equals(process.getApprovalType())) {
            AntibioticTest test = antibioticTestMapper.selectById(process.getBizId());
            if (test != null) {
                test.setApproveStatus(ApprovalStatusEnum.REJECTED.getCode());
                test.setApproverId(UserContext.getUserId());
                test.setApproveTime(LocalDateTime.now());
                test.setApproveRemark("审批驳回");
                antibioticTestMapper.updateById(test);
                log.info("检测结果审批驳回，更新检测记录状态，检测ID: {}", test.getId());
            }
        }
    }
}
