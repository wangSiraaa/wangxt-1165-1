package com.dairyfarm.milk.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.ApprovalStatusEnum;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.RejectTypeEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.enums.TestCategoryEnum;
import com.dairyfarm.milk.common.enums.TestResultEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.dto.AntibioticTestSubmitDTO;
import com.dairyfarm.milk.entity.AntibioticTest;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.mapper.AntibioticTestMapper;
import com.dairyfarm.milk.mapper.MilkBatchMapper;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import com.dairyfarm.milk.mapper.RejectRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntibioticTestService {

    private final AntibioticTestMapper antibioticTestMapper;
    private final MilkBatchMapper milkBatchMapper;
    private final MilkTankMapper milkTankMapper;
    private final RejectRecordMapper rejectRecordMapper;
    private final BusinessValidationService validationService;
    private final ApprovalService approvalService;

    @Transactional(rollbackFor = Exception.class)
    public AntibioticTest submitTest(AntibioticTestSubmitDTO dto) {
        MilkBatch batch = milkBatchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        if (!BatchStatusEnum.PENDING_TEST.getCode().equals(batch.getBatchStatus())) {
            throw new BusinessException("批次状态不允许提交检测");
        }

        String testNo = "JC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();

        AntibioticTest test = new AntibioticTest();
        test.setTestNo(testNo);
        test.setBatchId(dto.getBatchId());
        test.setTankId(batch.getTankId());
        test.setPastureId(batch.getPastureId());
        test.setLabId(UserContext.getUserId());
        test.setTestType(dto.getTestType());
        test.setTestCategory(TestCategoryEnum.QUICK.getCode());
        test.setTestMethod(dto.getTestMethod());
        test.setTestResult(dto.getTestResult());
        test.setTestValue(dto.getTestValue());
        test.setTestTime(dto.getTestTime());
        test.setIsLocked(0);
        test.setApproveStatus(ApprovalStatusEnum.PENDING.getCode());
        test.setApproverId(null);
        test.setApproveTime(null);
        test.setApproveRemark(null);
        test.setRemark(dto.getRemark());
        antibioticTestMapper.insert(test);

        if (TestResultEnum.FAILED.getCode().equals(dto.getTestResult())) {
            batch.setBatchStatus(BatchStatusEnum.TEST_FAILED.getCode());

            String rejectNo = "JS" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();
            RejectRecord reject = new RejectRecord();
            reject.setRejectNo(rejectNo);
            reject.setBatchId(batch.getId());
            reject.setTankId(batch.getTankId());
            reject.setPastureId(batch.getPastureId());
            reject.setTestId(test.getId());
            reject.setRejectType(RejectTypeEnum.TEST_FAILED.getCode());
            reject.setRejectReason("抗生素检测不合格，检测类型：" + dto.getTestType() + "，检测结果：不合格");
            reject.setRejectTime(LocalDateTime.now());
            reject.setOperatorId(UserContext.getUserId());
            reject.setHandleStatus("PENDING");
            rejectRecordMapper.insert(reject);

            MilkTank tank = milkTankMapper.selectById(batch.getTankId());
            tank.setTankStatus(TankStatusEnum.PENDING_CLEAN.getCode());
            milkTankMapper.updateById(tank);
            milkBatchMapper.updateById(batch);
            log.warn("批次{}检测不合格，已生成拒收记录", batch.getBatchNo());
        }

        approvalService.createTestResultApproval(test.getId());

        log.info("提交检测结果成功，检测单号: {}，已启动审批流", testNo);
        return test;
    }

    @Transactional(rollbackFor = Exception.class)
    public AntibioticTest updateTest(Long id, AntibioticTestSubmitDTO dto) {
        validationService.validateTestResultChange(id, dto.getTestResult());

        AntibioticTest test = antibioticTestMapper.selectById(id);
        if (test == null) {
            throw new BusinessException("检测记录不存在");
        }

        test.setTestType(dto.getTestType());
        test.setTestMethod(dto.getTestMethod());
        test.setTestResult(dto.getTestResult());
        test.setTestValue(dto.getTestValue());
        test.setTestTime(dto.getTestTime());
        test.setRemark(dto.getRemark());
        antibioticTestMapper.updateById(test);

        MilkBatch batch = milkBatchMapper.selectById(test.getBatchId());
        if (TestResultEnum.PASSED.getCode().equals(dto.getTestResult())) {
            batch.setBatchStatus(BatchStatusEnum.TEST_PASSED.getCode());
            LambdaQueryWrapper<RejectRecord> rejectWrapper = new LambdaQueryWrapper<>();
            rejectWrapper.eq(RejectRecord::getBatchId, batch.getId());
            RejectRecord reject = rejectRecordMapper.selectOne(rejectWrapper);
            if (reject != null) {
                rejectRecordMapper.deleteById(reject.getId());
            }
        } else {
            batch.setBatchStatus(BatchStatusEnum.TEST_FAILED.getCode());
        }
        milkBatchMapper.updateById(batch);

        return test;
    }

    public Page<AntibioticTest> getTestList(Long pageNum, Long pageSize, String result) {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<AntibioticTest> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(AntibioticTest::getPastureId, pastureId);
        }
        if (result != null && !result.isEmpty()) {
            wrapper.eq(AntibioticTest::getTestResult, result);
        }
        wrapper.orderByDesc(AntibioticTest::getCreateTime);
        return antibioticTestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public AntibioticTest getTestDetail(Long id) {
        return antibioticTestMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AntibioticTest submitRecheck(Long parentTestId, AntibioticTestSubmitDTO dto) {
        AntibioticTest parentTest = antibioticTestMapper.selectById(parentTestId);
        if (parentTest == null) {
            throw new BusinessException("原检测记录不存在");
        }

        MilkBatch batch = milkBatchMapper.selectById(parentTest.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        String testNo = "FJ" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();

        AntibioticTest recheckTest = new AntibioticTest();
        recheckTest.setParentId(parentTestId);
        recheckTest.setTestNo(testNo);
        recheckTest.setBatchId(parentTest.getBatchId());
        recheckTest.setTankId(parentTest.getTankId());
        recheckTest.setPastureId(parentTest.getPastureId());
        recheckTest.setLabId(UserContext.getUserId());
        recheckTest.setTestType(dto.getTestType() != null ? dto.getTestType() : parentTest.getTestType());
        recheckTest.setTestCategory(TestCategoryEnum.RECHECK.getCode());
        recheckTest.setTestMethod(dto.getTestMethod());
        recheckTest.setTestResult(dto.getTestResult());
        recheckTest.setTestValue(dto.getTestValue());
        recheckTest.setTestTime(dto.getTestTime());
        recheckTest.setIsLocked(parentTest.getIsLocked());
        recheckTest.setApproveStatus(ApprovalStatusEnum.PENDING.getCode());
        recheckTest.setApproverId(null);
        recheckTest.setApproveTime(null);
        recheckTest.setApproveRemark(null);
        recheckTest.setRemark(dto.getRemark());
        antibioticTestMapper.insert(recheckTest);

        if (parentTest.getIsLocked() == null || parentTest.getIsLocked() == 0) {
            if (TestResultEnum.PASSED.getCode().equals(dto.getTestResult())) {
                batch.setBatchStatus(BatchStatusEnum.TEST_PASSED.getCode());
                LambdaQueryWrapper<RejectRecord> rejectWrapper = new LambdaQueryWrapper<>();
                rejectWrapper.eq(RejectRecord::getBatchId, batch.getId());
                RejectRecord reject = rejectRecordMapper.selectOne(rejectWrapper);
                if (reject != null) {
                    rejectRecordMapper.deleteById(reject.getId());
                }
                MilkTank tank = milkTankMapper.selectById(batch.getTankId());
                if (tank != null && TankStatusEnum.PENDING_CLEAN.getCode().equals(tank.getTankStatus())) {
                    tank.setTankStatus(TankStatusEnum.TESTED.getCode());
                    milkTankMapper.updateById(tank);
                }
            } else {
                batch.setBatchStatus(BatchStatusEnum.TEST_FAILED.getCode());
            }
            milkBatchMapper.updateById(batch);
        }

        approvalService.createTestResultApproval(recheckTest.getId());

        log.info("提交复检记录成功，复检单号: {}，原检测单: {}", testNo, parentTest.getTestNo());
        return recheckTest;
    }

    public Page<AntibioticTest> getRecheckList(Long parentTestId, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<AntibioticTest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AntibioticTest::getParentId, parentTestId);
        wrapper.orderByDesc(AntibioticTest::getTestTime);
        return antibioticTestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
