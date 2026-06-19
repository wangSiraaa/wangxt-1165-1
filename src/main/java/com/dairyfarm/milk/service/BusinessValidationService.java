package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dairyfarm.milk.common.enums.ApprovalStatusEnum;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.enums.TestResultEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.entity.AntibioticTest;
import com.dairyfarm.milk.entity.LoadingRecord;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.entity.TankCleaning;
import com.dairyfarm.milk.mapper.AntibioticTestMapper;
import com.dairyfarm.milk.mapper.LoadingRecordMapper;
import com.dairyfarm.milk.mapper.MilkBatchMapper;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import com.dairyfarm.milk.mapper.RejectRecordMapper;
import com.dairyfarm.milk.mapper.TankCleaningMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessValidationService {

    private final MilkTankMapper milkTankMapper;
    private final MilkBatchMapper milkBatchMapper;
    private final AntibioticTestMapper antibioticTestMapper;
    private final LoadingRecordMapper loadingRecordMapper;
    private final TankCleaningMapper tankCleaningMapper;
    private final RejectRecordMapper rejectRecordMapper;

    public void validateTankForNewBatch(Long tankId, Long pastureId) {
        MilkTank tank = milkTankMapper.selectById(tankId);
        if (tank == null) {
            throw new BusinessException("奶罐不存在");
        }
        if (!tank.getPastureId().equals(pastureId)) {
            throw new BusinessException("该奶罐不属于当前牧场");
        }
        if (!TankStatusEnum.IDLE.getCode().equals(tank.getTankStatus())) {
            throw new BusinessException("奶罐当前状态为[" + TankStatusEnum.getByCode(tank.getTankStatus()).getDesc() + "]，不能接收新批次");
        }
        if (!isTankCleaned(tankId)) {
            throw new BusinessException("奶罐未清洗，不能接收新批次，请先完成清洗并确认合格");
        }
        if (!isPreviousBatchClosed(tankId)) {
            throw new BusinessException("奶罐上一批次快检未闭环，不能接入新批次，请先处理上一批次");
        }
    }

    public boolean isPreviousBatchClosed(Long tankId) {
        LambdaQueryWrapper<MilkBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(MilkBatch::getTankId, tankId)
                .orderByDesc(MilkBatch::getCreateTime)
                .last("LIMIT 1");
        MilkBatch lastBatch = milkBatchMapper.selectOne(batchWrapper);

        if (lastBatch == null) {
            return true;
        }

        String status = lastBatch.getBatchStatus();
        if (BatchStatusEnum.TEST_PASSED.getCode().equals(status) ||
            BatchStatusEnum.LOADED.getCode().equals(status) ||
            BatchStatusEnum.REJECTED.getCode().equals(status)) {
            return true;
        }

        if (BatchStatusEnum.TEST_FAILED.getCode().equals(status)) {
            LambdaQueryWrapper<RejectRecord> rejectWrapper = new LambdaQueryWrapper<>();
            rejectWrapper.eq(RejectRecord::getBatchId, lastBatch.getId())
                    .eq(RejectRecord::getHandleStatus, "RESOLVED");
            Long resolvedCount = rejectRecordMapper.selectCount(rejectWrapper);
            return resolvedCount > 0;
        }

        return false;
    }

    public boolean isTankCleaned(Long tankId) {
        LambdaQueryWrapper<TankCleaning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TankCleaning::getTankId, tankId)
                .eq(TankCleaning::getIsQualified, 1)
                .orderByDesc(TankCleaning::getCheckTime)
                .last("LIMIT 1");
        TankCleaning lastCleaning = tankCleaningMapper.selectOne(wrapper);
        
        if (lastCleaning == null) {
            LambdaQueryWrapper<MilkBatch> batchWrapper = new LambdaQueryWrapper<>();
            batchWrapper.eq(MilkBatch::getTankId, tankId);
            Long batchCount = milkBatchMapper.selectCount(batchWrapper);
            return batchCount == 0;
        }
        
        LambdaQueryWrapper<MilkBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(MilkBatch::getTankId, tankId)
                .gt(MilkBatch::getCreateTime, lastCleaning.getCheckTime());
        Long batchAfterClean = milkBatchMapper.selectCount(batchWrapper);
        
        return batchAfterClean == 0;
    }

    public void validateTestResultForLoading(Long batchId) {
        LambdaQueryWrapper<AntibioticTest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AntibioticTest::getBatchId, batchId)
                .orderByDesc(AntibioticTest::getTestTime)
                .last("LIMIT 1");
        AntibioticTest latestTest = antibioticTestMapper.selectOne(wrapper);
        
        if (latestTest == null) {
            throw new BusinessException("该批次尚未进行抗生素检测，不能装车");
        }

        if (!ApprovalStatusEnum.APPROVED.getCode().equals(latestTest.getApproveStatus())) {
            if (ApprovalStatusEnum.PENDING.getCode().equals(latestTest.getApproveStatus())) {
                throw new BusinessException("该批次检测结果正在审批中，请等待审批完成后再装车");
            } else if (ApprovalStatusEnum.REJECTED.getCode().equals(latestTest.getApproveStatus())) {
                throw new BusinessException("该批次检测结果审批已被驳回，不能装车");
            } else if (ApprovalStatusEnum.CANCELLED.getCode().equals(latestTest.getApproveStatus())) {
                throw new BusinessException("该批次检测结果审批已取消，请重新提交检测，不能装车");
            } else {
                throw new BusinessException("该批次检测结果审批状态异常，不能装车");
            }
        }
        
        if (TestResultEnum.FAILED.getCode().equals(latestTest.getTestResult())) {
            throw new BusinessException("该批次抗生素检测不合格，不能装车");
        }
    }

    public void validateTestModification(Long testId) {
        AntibioticTest test = antibioticTestMapper.selectById(testId);
        if (test == null) {
            throw new BusinessException("检测记录不存在");
        }
        
        if (test.getIsLocked() != null && test.getIsLocked() == 1) {
            throw new BusinessException("该批次已装车，检测结果已锁定，不能修改为合格");
        }
    }

    public void validateTestResultChange(Long testId, String newResult) {
        AntibioticTest test = antibioticTestMapper.selectById(testId);
        if (test == null) {
            throw new BusinessException("检测记录不存在");
        }
        
        if (TestResultEnum.PASSED.getCode().equals(newResult) && 
            test.getIsLocked() != null && test.getIsLocked() == 1) {
            throw new BusinessException("该批次已装车，检测结果已锁定，不能修改为合格");
        }
    }

    public void validateBatchStatusForLoading(Long batchId) {
        MilkBatch batch = milkBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        
        if (!BatchStatusEnum.TEST_PASSED.getCode().equals(batch.getBatchStatus())) {
            throw new BusinessException("批次当前状态为[" + BatchStatusEnum.getByCode(batch.getBatchStatus()).getDesc() + "]，不能装车");
        }
        
        LambdaQueryWrapper<LoadingRecord> loadingWrapper = new LambdaQueryWrapper<>();
        loadingWrapper.eq(LoadingRecord::getBatchId, batchId);
        Long loadedCount = loadingRecordMapper.selectCount(loadingWrapper);
        if (loadedCount > 0) {
            throw new BusinessException("该批次已装车，不能重复装车");
        }
    }

    public void lockTestResultAfterLoading(Long batchId) {
        LambdaQueryWrapper<AntibioticTest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AntibioticTest::getBatchId, batchId);
        List<AntibioticTest> tests = antibioticTestMapper.selectList(wrapper);
        
        for (AntibioticTest test : tests) {
            test.setIsLocked(1);
            antibioticTestMapper.updateById(test);
        }
        log.info("批次{}已装车，锁定{}条检测记录", batchId, tests.size());
    }

    public LocalDateTime getLastCleanTime(Long tankId) {
        LambdaQueryWrapper<TankCleaning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TankCleaning::getTankId, tankId)
                .eq(TankCleaning::getIsQualified, 1)
                .orderByDesc(TankCleaning::getCheckTime)
                .last("LIMIT 1");
        TankCleaning lastCleaning = tankCleaningMapper.selectOne(wrapper);
        return lastCleaning != null ? lastCleaning.getCheckTime() : null;
    }
}
