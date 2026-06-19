package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.*;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.config.TestConfig;
import com.dairyfarm.milk.dto.ApprovalHandleDTO;
import com.dairyfarm.milk.entity.*;
import com.dairyfarm.milk.mapper.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
@Rollback
public class ApprovalServiceTest {

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PastureMapper pastureMapper;

    @Autowired
    private MilkTankMapper milkTankMapper;

    @Autowired
    private MilkBatchMapper milkBatchMapper;

    @Autowired
    private AntibioticTestMapper antibioticTestMapper;

    @Autowired
    private ApprovalProcessMapper approvalProcessMapper;

    @Autowired
    private ApprovalNodeMapper approvalNodeMapper;

    private Pasture testPasture;
    private SysUser labUser;
    private SysUser labSupervisor;
    private SysUser qualitySupervisor;

    @Before
    public void setUp() {
        testPasture = new Pasture();
        testPasture.setPastureCode("TEST_PASTURE_002");
        testPasture.setPastureName("测试牧场2");
        testPasture.setAddress("测试地址2");
        testPasture.setContactPerson("测试联系人2");
        testPasture.setContactPhone("13800138001");
        testPasture.setStatus(1);
        pastureMapper.insert(testPasture);

        labUser = createUser("lab_test", "化验员测试", RoleEnum.LAB.getCode());
        labSupervisor = createUser("lab_super_test", "化验室主管测试", RoleEnum.LAB_SUPERVISOR.getCode());
        qualitySupervisor = createUser("quality_super_test", "质量主管测试", RoleEnum.QUALITY_SUPERVISOR.getCode());

        setUserContext(labUser);
    }

    @After
    public void tearDown() {
        UserContext.remove();
    }

    private SysUser createUser(String username, String realName, String roleCode) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword("123456");
        user.setRealName(realName);
        user.setPhone("13800138000");
        user.setRoleCode(roleCode);
        user.setPastureId(testPasture.getId());
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user;
    }

    private void setUserContext(SysUser user) {
        UserContext userContext = new UserContext();
        userContext.setUserId(user.getId());
        userContext.setUsername(user.getUsername());
        userContext.setRoleCode(user.getRoleCode());
        userContext.setPastureId(user.getPastureId());
        UserContext.set(userContext);
    }

    private MilkTank createMilkTank() {
        MilkTank tank = new MilkTank();
        tank.setTankCode("TANK_APPR_" + System.currentTimeMillis());
        tank.setTankName("审批测试奶罐");
        tank.setCapacity(new BigDecimal("5000.00"));
        tank.setCurrentVolume(new BigDecimal("0.00"));
        tank.setTankStatus(TankStatusEnum.IDLE.getCode());
        tank.setPastureId(testPasture.getId());
        milkTankMapper.insert(tank);
        return tank;
    }

    private MilkBatch createMilkBatch(MilkTank tank) {
        MilkBatch batch = new MilkBatch();
        batch.setBatchNo("BATCH_APPR_" + System.currentTimeMillis());
        batch.setPastureId(testPasture.getId());
        batch.setTankId(tank.getId());
        batch.setMilkVolume(new BigDecimal("2000.00"));
        batch.setMilkTemperature(new BigDecimal("4.0"));
        batch.setCowCount(50);
        batch.setBatchStatus(BatchStatusEnum.PENDING_TEST.getCode());
        batch.setMilkingStartTime(LocalDateTime.now().minusHours(2));
        batch.setMilkingEndTime(LocalDateTime.now().minusHours(1));
        milkBatchMapper.insert(batch);
        return batch;
    }

    private AntibioticTest createAntibioticTest(MilkBatch batch) {
        AntibioticTest test = new AntibioticTest();
        test.setTestNo("TEST_APPR_" + System.currentTimeMillis());
        test.setBatchId(batch.getId());
        test.setTankId(batch.getTankId());
        test.setPastureId(testPasture.getId());
        test.setLabId(labUser.getId());
        test.setTestType("青霉素类");
        test.setTestMethod("快检卡");
        test.setTestResult(TestResultEnum.PASSED.getCode());
        test.setTestValue("阴性");
        test.setTestTime(LocalDateTime.now());
        test.setIsLocked(0);
        test.setApproveStatus("DRAFT");
        antibioticTestMapper.insert(test);
        return test;
    }

    private ApprovalHandleDTO createApprovalHandleDTO(ApprovalProcess process, ApprovalNode node, String result, String remark) {
        ApprovalHandleDTO dto = new ApprovalHandleDTO();
        dto.setProcessId(process.getId());
        dto.setNodeId(node.getId());
        dto.setApprovalResult(result);
        dto.setRemark(remark);
        return dto;
    }

    private ApprovalNode getCurrentApprovalNode(Long processId) {
        LambdaQueryWrapper<ApprovalNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalNode::getProcessId, processId)
                .in(ApprovalNode::getNodeStatus, ApprovalNodeStatusEnum.PENDING.getCode(), ApprovalNodeStatusEnum.PROCESSING.getCode())
                .orderByAsc(ApprovalNode::getNodeOrder)
                .last("LIMIT 1");
        return approvalNodeMapper.selectOne(wrapper);
    }

    private List<ApprovalNode> getApprovalNodes(Long processId) {
        LambdaQueryWrapper<ApprovalNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalNode::getProcessId, processId)
                .orderByAsc(ApprovalNode::getNodeOrder);
        return approvalNodeMapper.selectList(wrapper);
    }

    @Test
    public void test_提交检测结果审批成功() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        assertThat(process).isNotNull();
        assertThat(process.getApprovalStatus()).isEqualTo(ApprovalStatusEnum.PENDING.getCode());
        assertThat(process.getTotalNodes()).isEqualTo(2);
        assertThat(process.getCurrentNode()).isEqualTo(0);

        List<ApprovalNode> nodes = getApprovalNodes(process.getId());
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0).getNodeName()).isEqualTo("化验室主管审批");
        assertThat(nodes.get(0).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.PROCESSING.getCode());
        assertThat(nodes.get(1).getNodeName()).isEqualTo("质量主管审批");
        assertThat(nodes.get(1).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.PENDING.getCode());

        AntibioticTest updatedTest = antibioticTestMapper.selectById(test.getId());
        assertThat(updatedTest.getApproveStatus()).isEqualTo(ApprovalStatusEnum.PENDING.getCode());
    }

    @Test
    public void test_第一级审批通过后推进到第二级() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        setUserContext(labSupervisor);
        ApprovalNode firstNode = getCurrentApprovalNode(process.getId());
        ApprovalHandleDTO approveDTO = createApprovalHandleDTO(process, firstNode, ApprovalStatusEnum.APPROVED.getCode(), "同意");
        approvalService.approve(approveDTO);

        ApprovalProcess updatedProcess = approvalProcessMapper.selectById(process.getId());
        assertThat(updatedProcess.getCurrentNode()).isEqualTo(1);
        assertThat(updatedProcess.getApprovalStatus()).isEqualTo(ApprovalStatusEnum.PENDING.getCode());

        List<ApprovalNode> nodes = getApprovalNodes(process.getId());
        assertThat(nodes.get(0).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.COMPLETED.getCode());
        assertThat(nodes.get(0).getApprovalResult()).isEqualTo(ApprovalStatusEnum.APPROVED.getCode());
        assertThat(nodes.get(1).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.PROCESSING.getCode());
    }

    @Test
    public void test_第二级审批通过后流程完成() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        setUserContext(labSupervisor);
        ApprovalNode firstNode = getCurrentApprovalNode(process.getId());
        ApprovalHandleDTO firstApproveDTO = createApprovalHandleDTO(process, firstNode, ApprovalStatusEnum.APPROVED.getCode(), "第一级同意");
        approvalService.approve(firstApproveDTO);

        setUserContext(qualitySupervisor);
        ApprovalNode secondNode = getCurrentApprovalNode(process.getId());
        ApprovalHandleDTO secondApproveDTO = createApprovalHandleDTO(process, secondNode, ApprovalStatusEnum.APPROVED.getCode(), "第二级同意");
        approvalService.approve(secondApproveDTO);

        ApprovalProcess finalProcess = approvalProcessMapper.selectById(process.getId());
        assertThat(finalProcess.getApprovalStatus()).isEqualTo(ApprovalStatusEnum.APPROVED.getCode());
        assertThat(finalProcess.getCompleteTime()).isNotNull();

        List<ApprovalNode> nodes = getApprovalNodes(process.getId());
        assertThat(nodes.get(0).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.COMPLETED.getCode());
        assertThat(nodes.get(0).getApprovalResult()).isEqualTo(ApprovalStatusEnum.APPROVED.getCode());
        assertThat(nodes.get(1).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.COMPLETED.getCode());
        assertThat(nodes.get(1).getApprovalResult()).isEqualTo(ApprovalStatusEnum.APPROVED.getCode());

        AntibioticTest finalTest = antibioticTestMapper.selectById(test.getId());
        assertThat(finalTest.getApproveStatus()).isEqualTo(ApprovalStatusEnum.APPROVED.getCode());
    }

    @Test
    public void test_审批驳回后流程终止() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        setUserContext(labSupervisor);
        ApprovalNode firstNode = getCurrentApprovalNode(process.getId());
        ApprovalHandleDTO rejectDTO = createApprovalHandleDTO(process, firstNode, ApprovalStatusEnum.REJECTED.getCode(), "检测结果有疑问，请重新检测");
        approvalService.reject(rejectDTO);

        ApprovalProcess finalProcess = approvalProcessMapper.selectById(process.getId());
        assertThat(finalProcess.getApprovalStatus()).isEqualTo(ApprovalStatusEnum.REJECTED.getCode());
        assertThat(finalProcess.getCompleteTime()).isNotNull();

        List<ApprovalNode> nodes = getApprovalNodes(process.getId());
        assertThat(nodes.get(0).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.COMPLETED.getCode());
        assertThat(nodes.get(0).getApprovalResult()).isEqualTo(ApprovalStatusEnum.REJECTED.getCode());
        assertThat(nodes.get(1).getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.SKIPPED.getCode());

        AntibioticTest finalTest = antibioticTestMapper.selectById(test.getId());
        assertThat(finalTest.getApproveStatus()).isEqualTo(ApprovalStatusEnum.REJECTED.getCode());
    }

    @Test
    public void test_非审批人不能审批() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        setUserContext(labUser);
        ApprovalNode firstNode = getCurrentApprovalNode(process.getId());
        ApprovalHandleDTO approveDTO = createApprovalHandleDTO(process, firstNode, ApprovalStatusEnum.APPROVED.getCode(), "同意");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.approve(approveDTO);
        });

        assertThat(exception.getMessage()).contains("无权限");
    }

    @Test
    public void test_获取审批详情() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        Map<String, Object> detail = approvalService.getApprovalDetail(process.getId());

        assertThat(detail).isNotNull();
        assertThat(detail).containsKeys("process", "nodes", "records");

        ApprovalProcess detailProcess = (ApprovalProcess) detail.get("process");
        assertThat(detailProcess.getId()).isEqualTo(process.getId());

        @SuppressWarnings("unchecked")
        List<ApprovalNode> detailNodes = (List<ApprovalNode>) detail.get("nodes");
        assertThat(detailNodes).hasSize(2);
    }

    @Test
    public void test_取消审批() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        approvalService.cancelApproval(process.getId());

        ApprovalProcess cancelledProcess = approvalProcessMapper.selectById(process.getId());
        assertThat(cancelledProcess.getApprovalStatus()).isEqualTo(ApprovalStatusEnum.CANCELLED.getCode());

        List<ApprovalNode> nodes = getApprovalNodes(process.getId());
        for (ApprovalNode node : nodes) {
            assertThat(node.getNodeStatus()).isEqualTo(ApprovalNodeStatusEnum.SKIPPED.getCode());
        }
    }

    @Test
    public void test_非提交人不能取消审批() {
        setUserContext(labUser);
        MilkTank tank = createMilkTank();
        MilkBatch batch = createMilkBatch(tank);
        AntibioticTest test = createAntibioticTest(batch);

        ApprovalProcess process = approvalService.createTestResultApproval(test.getId());

        setUserContext(labSupervisor);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.cancelApproval(process.getId());
        });

        assertThat(exception.getMessage()).contains("只有提交人可以取消审批");
    }
}
