package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dairyfarm.milk.common.enums.ApprovalStatusEnum;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.RejectTypeEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.enums.TestCategoryEnum;
import com.dairyfarm.milk.common.enums.TestResultEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.entity.AntibioticTest;
import com.dairyfarm.milk.entity.LoadingRecord;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.entity.Pasture;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.entity.SysUser;
import com.dairyfarm.milk.entity.TankCleaning;
import com.dairyfarm.milk.mapper.AntibioticTestMapper;
import com.dairyfarm.milk.mapper.LoadingRecordMapper;
import com.dairyfarm.milk.mapper.MilkBatchMapper;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import com.dairyfarm.milk.mapper.PastureMapper;
import com.dairyfarm.milk.mapper.RejectRecordMapper;
import com.dairyfarm.milk.mapper.SysUserMapper;
import com.dairyfarm.milk.mapper.TankCleaningMapper;
import com.dairyfarm.milk.vo.TraceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraceService {

    private final MilkBatchMapper milkBatchMapper;
    private final MilkTankMapper milkTankMapper;
    private final TankCleaningMapper tankCleaningMapper;
    private final AntibioticTestMapper antibioticTestMapper;
    private final LoadingRecordMapper loadingRecordMapper;
    private final RejectRecordMapper rejectRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final PastureMapper pastureMapper;

    public TraceVO getTraceByBatchId(Long batchId) {
        MilkBatch batch = milkBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        TraceVO vo = new TraceVO();
        vo.setBatchId(batch.getId());
        vo.setBatchNo(batch.getBatchNo());
        vo.setPastureId(batch.getPastureId());

        Pasture pasture = pastureMapper.selectById(batch.getPastureId());
        if (pasture != null) {
            vo.setPastureName(pasture.getPastureName());
        }

        List<Long> userIds = new ArrayList<>();
        if (batch.getMilkerId() != null) {
            userIds.add(batch.getMilkerId());
        }

        MilkTank tank = milkTankMapper.selectById(batch.getTankId());
        if (tank != null) {
            TraceVO.TankInfoVO tankInfo = new TraceVO.TankInfoVO();
            tankInfo.setTankId(tank.getId());
            tankInfo.setTankCode(tank.getTankCode());
            tankInfo.setTankName(tank.getTankName());
            tankInfo.setCapacity(tank.getCapacity());
            tankInfo.setTankStatus(tank.getTankStatus());
            tankInfo.setTankStatusDesc(TankStatusEnum.getByCode(tank.getTankStatus()) != null
                    ? TankStatusEnum.getByCode(tank.getTankStatus()).getDesc() : tank.getTankStatus());
            vo.setTankInfo(tankInfo);
        }

        TraceVO.BatchInfoVO batchInfo = new TraceVO.BatchInfoVO();
        batchInfo.setMilkingStartTime(batch.getMilkingStartTime());
        batchInfo.setMilkingEndTime(batch.getMilkingEndTime());
        batchInfo.setMilkVolume(batch.getMilkVolume());
        batchInfo.setMilkTemperature(batch.getMilkTemperature());
        batchInfo.setCowCount(batch.getCowCount());
        batchInfo.setMilkerId(batch.getMilkerId());
        batchInfo.setBatchStatus(batch.getBatchStatus());
        batchInfo.setBatchStatusDesc(BatchStatusEnum.getByCode(batch.getBatchStatus()) != null
                ? BatchStatusEnum.getByCode(batch.getBatchStatus()).getDesc() : batch.getBatchStatus());
        vo.setBatchInfo(batchInfo);

        LambdaQueryWrapper<TankCleaning> cleaningWrapper = new LambdaQueryWrapper<>();
        cleaningWrapper.eq(TankCleaning::getTankId, batch.getTankId())
                .lt(TankCleaning::getCreateTime, batch.getCreateTime())
                .eq(TankCleaning::getIsQualified, 1)
                .orderByDesc(TankCleaning::getCheckTime)
                .last("LIMIT 1");
        TankCleaning lastCleaning = tankCleaningMapper.selectOne(cleaningWrapper);
        if (lastCleaning != null) {
            TraceVO.CleaningInfoVO cleaningInfo = new TraceVO.CleaningInfoVO();
            cleaningInfo.setCleaningId(lastCleaning.getId());
            cleaningInfo.setCleaningNo(lastCleaning.getCleaningNo());
            cleaningInfo.setOperatorId(lastCleaning.getOperatorId());
            cleaningInfo.setCleaningStartTime(lastCleaning.getCleaningStartTime());
            cleaningInfo.setCleaningEndTime(lastCleaning.getCleaningEndTime());
            cleaningInfo.setCleaningMethod(lastCleaning.getCleaningMethod());
            cleaningInfo.setIsQualified(lastCleaning.getIsQualified());
            cleaningInfo.setConfirmStatus(lastCleaning.getConfirmStatus());
            cleaningInfo.setConfirmStatusDesc("CONFIRMED".equals(lastCleaning.getConfirmStatus()) ? "已确认" : "待确认");
            cleaningInfo.setCheckerId(lastCleaning.getCheckerId());
            cleaningInfo.setCheckTime(lastCleaning.getCheckTime());
            vo.setCleaningInfo(cleaningInfo);

            if (lastCleaning.getOperatorId() != null) {
                userIds.add(lastCleaning.getOperatorId());
            }
            if (lastCleaning.getCheckerId() != null) {
                userIds.add(lastCleaning.getCheckerId());
            }
        }

        LambdaQueryWrapper<AntibioticTest> testWrapper = new LambdaQueryWrapper<>();
        testWrapper.eq(AntibioticTest::getBatchId, batchId)
                .orderByAsc(AntibioticTest::getCreateTime);
        List<AntibioticTest> tests = antibioticTestMapper.selectList(testWrapper);
        List<TraceVO.TestInfoVO> testInfoList = new ArrayList<>();
        for (AntibioticTest test : tests) {
            TraceVO.TestInfoVO testInfo = new TraceVO.TestInfoVO();
            testInfo.setTestId(test.getId());
            testInfo.setTestNo(test.getTestNo());
            testInfo.setParentId(test.getParentId());
            testInfo.setTestCategory(test.getTestCategory());
            testInfo.setTestCategoryDesc(TestCategoryEnum.getByCode(test.getTestCategory()) != null
                    ? TestCategoryEnum.getByCode(test.getTestCategory()).getDesc() : test.getTestCategory());
            testInfo.setTestType(test.getTestType());
            testInfo.setTestMethod(test.getTestMethod());
            testInfo.setTestResult(test.getTestResult());
            testInfo.setTestResultDesc(TestResultEnum.getByCode(test.getTestResult()) != null
                    ? TestResultEnum.getByCode(test.getTestResult()).getDesc() : test.getTestResult());
            testInfo.setTestValue(test.getTestValue());
            testInfo.setTestTime(test.getTestTime());
            testInfo.setLabId(test.getLabId());
            testInfo.setIsLocked(test.getIsLocked());
            testInfo.setApproveStatus(test.getApproveStatus());
            testInfo.setApproveStatusDesc(ApprovalStatusEnum.getByCode(test.getApproveStatus()) != null
                    ? ApprovalStatusEnum.getByCode(test.getApproveStatus()).getDesc() : test.getApproveStatus());
            testInfoList.add(testInfo);

            if (test.getLabId() != null) {
                userIds.add(test.getLabId());
            }
        }
        vo.setTestInfoList(testInfoList);

        LambdaQueryWrapper<LoadingRecord> loadingWrapper = new LambdaQueryWrapper<>();
        loadingWrapper.eq(LoadingRecord::getBatchId, batchId)
                .orderByDesc(LoadingRecord::getCreateTime)
                .last("LIMIT 1");
        LoadingRecord loading = loadingRecordMapper.selectOne(loadingWrapper);
        if (loading != null) {
            TraceVO.LoadingInfoVO loadingInfo = new TraceVO.LoadingInfoVO();
            loadingInfo.setLoadingId(loading.getId());
            loadingInfo.setLoadingNo(loading.getLoadingNo());
            loadingInfo.setDriverId(loading.getDriverId());
            loadingInfo.setTransporterName(loading.getTransporterName());
            loadingInfo.setTruckNo(loading.getTruckNo());
            loadingInfo.setLoadVolume(loading.getLoadVolume());
            loadingInfo.setLoadTemperature(loading.getLoadTemperature());
            loadingInfo.setLoadTime(loading.getLoadTime());
            loadingInfo.setDestination(loading.getDestination());
            loadingInfo.setArrivalTime(loading.getArrivalTime());
            loadingInfo.setLoadingStatus(loading.getLoadingStatus());
            loadingInfo.setLoadingStatusDesc("LOADED".equals(loading.getLoadingStatus()) ? "已装车"
                    : "IN_TRANSIT".equals(loading.getLoadingStatus()) ? "运输中"
                    : "ARRIVED".equals(loading.getLoadingStatus()) ? "已到达" : loading.getLoadingStatus());
            vo.setLoadingInfo(loadingInfo);

            if (loading.getDriverId() != null) {
                userIds.add(loading.getDriverId());
            }
        }

        LambdaQueryWrapper<RejectRecord> rejectWrapper = new LambdaQueryWrapper<>();
        rejectWrapper.eq(RejectRecord::getBatchId, batchId)
                .orderByDesc(RejectRecord::getRejectTime)
                .last("LIMIT 1");
        RejectRecord reject = rejectRecordMapper.selectOne(rejectWrapper);
        if (reject != null) {
            TraceVO.RejectInfoVO rejectInfo = new TraceVO.RejectInfoVO();
            rejectInfo.setRejectId(reject.getId());
            rejectInfo.setRejectNo(reject.getRejectNo());
            rejectInfo.setRejectType(reject.getRejectType());
            rejectInfo.setRejectTypeDesc(RejectTypeEnum.getByCode(reject.getRejectType()) != null
                    ? RejectTypeEnum.getByCode(reject.getRejectType()).getDesc() : reject.getRejectType());
            rejectInfo.setRejectReason(reject.getRejectReason());
            rejectInfo.setRejectTime(reject.getRejectTime());
            rejectInfo.setOperatorId(reject.getOperatorId());
            rejectInfo.setHandleStatus(reject.getHandleStatus());
            rejectInfo.setHandleStatusDesc("PENDING".equals(reject.getHandleStatus()) ? "待处理"
                    : "HANDLING".equals(reject.getHandleStatus()) ? "处理中"
                    : "RESOLVED".equals(reject.getHandleStatus()) ? "已解决" : reject.getHandleStatus());
            rejectInfo.setHandleResult(reject.getHandleResult());
            rejectInfo.setHandlerId(reject.getHandlerId());
            rejectInfo.setHandleTime(reject.getHandleTime());
            vo.setRejectInfo(rejectInfo);

            if (reject.getOperatorId() != null) {
                userIds.add(reject.getOperatorId());
            }
            if (reject.getHandlerId() != null) {
                userIds.add(reject.getHandlerId());
            }
        }

        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            Map<Long, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));

            if (vo.getBatchInfo() != null && vo.getBatchInfo().getMilkerId() != null) {
                vo.getBatchInfo().setMilkerName(userNameMap.get(vo.getBatchInfo().getMilkerId()));
            }
            if (vo.getCleaningInfo() != null) {
                if (vo.getCleaningInfo().getOperatorId() != null) {
                    vo.getCleaningInfo().setOperatorName(userNameMap.get(vo.getCleaningInfo().getOperatorId()));
                }
                if (vo.getCleaningInfo().getCheckerId() != null) {
                    vo.getCleaningInfo().setCheckerName(userNameMap.get(vo.getCleaningInfo().getCheckerId()));
                }
            }
            if (vo.getTestInfoList() != null) {
                for (TraceVO.TestInfoVO testInfo : vo.getTestInfoList()) {
                    if (testInfo.getLabId() != null) {
                        testInfo.setLabName(userNameMap.get(testInfo.getLabId()));
                    }
                }
            }
            if (vo.getLoadingInfo() != null && vo.getLoadingInfo().getDriverId() != null) {
                vo.getLoadingInfo().setDriverName(userNameMap.get(vo.getLoadingInfo().getDriverId()));
            }
            if (vo.getRejectInfo() != null) {
                if (vo.getRejectInfo().getOperatorId() != null) {
                    vo.getRejectInfo().setOperatorName(userNameMap.get(vo.getRejectInfo().getOperatorId()));
                }
                if (vo.getRejectInfo().getHandlerId() != null) {
                    vo.getRejectInfo().setHandlerName(userNameMap.get(vo.getRejectInfo().getHandlerId()));
                }
            }
        }

        return vo;
    }

    public TraceVO getTraceByTankId(Long tankId) {
        LambdaQueryWrapper<MilkBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(MilkBatch::getTankId, tankId)
                .orderByDesc(MilkBatch::getCreateTime)
                .last("LIMIT 1");
        MilkBatch lastBatch = milkBatchMapper.selectOne(batchWrapper);
        if (lastBatch == null) {
            throw new BusinessException("奶罐暂无批次记录");
        }
        return getTraceByBatchId(lastBatch.getId());
    }
}
