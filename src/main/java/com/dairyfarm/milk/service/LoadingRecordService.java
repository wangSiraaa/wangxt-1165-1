package com.dairyfarm.milk.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.RejectTypeEnum;
import com.dairyfarm.milk.common.enums.RoleEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.enums.TestResultEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.dto.LoadingConfirmDTO;
import com.dairyfarm.milk.entity.AntibioticTest;
import com.dairyfarm.milk.entity.LoadingRecord;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.mapper.AntibioticTestMapper;
import com.dairyfarm.milk.mapper.LoadingRecordMapper;
import com.dairyfarm.milk.mapper.MilkBatchMapper;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import com.dairyfarm.milk.mapper.RejectRecordMapper;
import com.dairyfarm.milk.vo.LoadingEntranceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadingRecordService {

    private final LoadingRecordMapper loadingRecordMapper;
    private final MilkBatchMapper milkBatchMapper;
    private final MilkTankMapper milkTankMapper;
    private final AntibioticTestMapper antibioticTestMapper;
    private final RejectRecordMapper rejectRecordMapper;
    private final BusinessValidationService validationService;

    @Transactional(rollbackFor = Exception.class)
    public LoadingRecord confirmLoading(LoadingConfirmDTO dto) {
        String roleCode = UserContext.getRoleCode();
        if (!RoleEnum.DRIVER.getCode().equals(roleCode)) {
            throw new BusinessException("只有司机可以确认装车");
        }

        validationService.validateBatchStatusForLoading(dto.getBatchId());
        validationService.validateTestResultForLoading(dto.getBatchId());

        MilkBatch batch = milkBatchMapper.selectById(dto.getBatchId());

        String loadingNo = "ZC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();

        LoadingRecord loading = new LoadingRecord();
        loading.setLoadingNo(loadingNo);
        loading.setBatchId(dto.getBatchId());
        loading.setTankId(batch.getTankId());
        loading.setPastureId(batch.getPastureId());
        loading.setDriverId(UserContext.getUserId());
        loading.setTruckNo(dto.getTruckNo());
        loading.setTransporterName(dto.getTransporterName());
        loading.setLoadVolume(dto.getLoadVolume());
        loading.setLoadTemperature(dto.getLoadTemperature());
        loading.setLoadTime(dto.getLoadTime());
        loading.setDestination(dto.getDestination());
        loading.setLoadingStatus("LOADED");
        loading.setRemark(dto.getRemark());
        loadingRecordMapper.insert(loading);

        batch.setBatchStatus(BatchStatusEnum.LOADED.getCode());
        milkBatchMapper.updateById(batch);

        MilkTank tank = milkTankMapper.selectById(batch.getTankId());
        tank.setTankStatus(TankStatusEnum.PENDING_CLEAN.getCode());
        tank.setCurrentVolume(java.math.BigDecimal.ZERO);
        tank.setCurrentBatchId(null);
        milkTankMapper.updateById(tank);

        validationService.lockTestResultAfterLoading(dto.getBatchId());

        log.info("司机确认装车成功，装车单号: {}, 批次: {}", loadingNo, dto.getBatchId());
        return loading;
    }

    public Page<LoadingRecord> getLoadingList(Long pageNum, Long pageSize, String status) {
        Long userId = UserContext.getUserId();
        String roleCode = UserContext.getRoleCode();
        Long pastureId = UserContext.getPastureId();

        LambdaQueryWrapper<LoadingRecord> wrapper = new LambdaQueryWrapper<>();
        if (RoleEnum.DRIVER.getCode().equals(roleCode)) {
            wrapper.eq(LoadingRecord::getDriverId, userId);
        }
        if (pastureId != null) {
            wrapper.eq(LoadingRecord::getPastureId, pastureId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LoadingRecord::getLoadingStatus, status);
        }
        wrapper.orderByDesc(LoadingRecord::getCreateTime);
        return loadingRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public LoadingRecord getLoadingDetail(Long id) {
        return loadingRecordMapper.selectById(id);
    }

    public LoadingEntranceVO getLoadingEntranceStatus(Long batchId) {
        MilkBatch batch = milkBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        MilkTank tank = milkTankMapper.selectById(batch.getTankId());

        LoadingEntranceVO vo = new LoadingEntranceVO();
        vo.setBatchId(batch.getId());
        vo.setBatchNo(batch.getBatchNo());
        vo.setTankId(batch.getTankId());
        vo.setTankCode(tank != null ? tank.getTankCode() : "");
        vo.setMilkVolume(batch.getMilkVolume());
        vo.setBatchStatus(batch.getBatchStatus());
        vo.setBatchStatusDesc(BatchStatusEnum.getByCode(batch.getBatchStatus()) != null
                ? BatchStatusEnum.getByCode(batch.getBatchStatus()).getDesc() : batch.getBatchStatus());

        LambdaQueryWrapper<AntibioticTest> testWrapper = new LambdaQueryWrapper<>();
        testWrapper.eq(AntibioticTest::getBatchId, batchId)
                .orderByDesc(AntibioticTest::getTestTime)
                .last("LIMIT 1");
        AntibioticTest latestTest = antibioticTestMapper.selectOne(testWrapper);

        if (latestTest != null) {
            vo.setLatestTestResult(latestTest.getTestResult());
            vo.setLatestTestResultDesc(TestResultEnum.getByCode(latestTest.getTestResult()) != null
                    ? TestResultEnum.getByCode(latestTest.getTestResult()).getDesc() : latestTest.getTestResult());
            vo.setTestTime(latestTest.getTestTime());
            vo.setTestNo(latestTest.getTestNo());
        }

        boolean canLoad = true;
        boolean isLocked = false;
        String lockReason = "";
        String rejectType = "";
        String rejectTypeDesc = "";

        if (!BatchStatusEnum.TEST_PASSED.getCode().equals(batch.getBatchStatus())) {
            canLoad = false;
            if (BatchStatusEnum.TEST_FAILED.getCode().equals(batch.getBatchStatus())) {
                isLocked = true;
                LambdaQueryWrapper<RejectRecord> rejectWrapper = new LambdaQueryWrapper<>();
                rejectWrapper.eq(RejectRecord::getBatchId, batchId)
                        .orderByDesc(RejectRecord::getRejectTime)
                        .last("LIMIT 1");
                RejectRecord reject = rejectRecordMapper.selectOne(rejectWrapper);
                if (reject != null) {
                    lockReason = reject.getRejectReason();
                    rejectType = reject.getRejectType();
                    rejectTypeDesc = RejectTypeEnum.getByCode(reject.getRejectType()) != null
                            ? RejectTypeEnum.getByCode(reject.getRejectType()).getDesc() : reject.getRejectType();
                } else {
                    lockReason = "该批次抗生素检测不合格，禁止装车";
                }
            } else if (BatchStatusEnum.PENDING_TEST.getCode().equals(batch.getBatchStatus())) {
                lockReason = "该批次尚未完成检测，请等待检测结果";
            } else if (BatchStatusEnum.LOADED.getCode().equals(batch.getBatchStatus())) {
                isLocked = true;
                lockReason = "该批次已装车，不能重复装车";
            } else if (BatchStatusEnum.REJECTED.getCode().equals(batch.getBatchStatus())) {
                isLocked = true;
                lockReason = "该批次已被拒收，不能装车";
            } else {
                lockReason = "批次当前状态不允许装车";
            }
        }

        if (canLoad && latestTest != null) {
            if (!"APPROVED".equals(latestTest.getApproveStatus())) {
                canLoad = false;
                lockReason = "检测结果正在审批中，请等待审批完成";
            }
        }

        vo.setCanLoad(canLoad);
        vo.setIsLocked(isLocked);
        vo.setLockReason(lockReason);
        vo.setRejectType(rejectType);
        vo.setRejectTypeDesc(rejectTypeDesc);

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLoadingStatus(Long id, String status, LocalDateTime arrivalTime) {
        LoadingRecord loading = loadingRecordMapper.selectById(id);
        if (loading == null) {
            throw new BusinessException("装车记录不存在");
        }
        loading.setLoadingStatus(status);
        if (arrivalTime != null) {
            loading.setArrivalTime(arrivalTime);
        }
        loadingRecordMapper.updateById(loading);
    }
}
