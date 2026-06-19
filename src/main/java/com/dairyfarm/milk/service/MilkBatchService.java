package com.dairyfarm.milk.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.BatchStatusEnum;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.dto.MilkBatchSubmitDTO;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.mapper.MilkBatchMapper;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilkBatchService {

    private final MilkBatchMapper milkBatchMapper;
    private final MilkTankMapper milkTankMapper;
    private final BusinessValidationService validationService;

    @Transactional(rollbackFor = Exception.class)
    public MilkBatch submitBatch(MilkBatchSubmitDTO dto) {
        Long pastureId = UserContext.getPastureId();
        if (pastureId == null) {
            throw new BusinessException("用户未关联牧场");
        }

        validationService.validateTankForNewBatch(dto.getTankId(), pastureId);

        String batchNo = "PC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();

        MilkBatch batch = new MilkBatch();
        batch.setBatchNo(batchNo);
        batch.setPastureId(pastureId);
        batch.setTankId(dto.getTankId());
        batch.setMilkingStartTime(dto.getMilkingStartTime() != null ? dto.getMilkingStartTime() : LocalDateTime.now());
        batch.setMilkingEndTime(dto.getMilkingEndTime());
        batch.setMilkVolume(dto.getMilkVolume());
        batch.setMilkTemperature(dto.getMilkTemperature());
        batch.setCowCount(dto.getCowCount());
        batch.setMilkerId(dto.getMilkerId() != null ? dto.getMilkerId() : UserContext.getUserId());
        batch.setBatchStatus(BatchStatusEnum.PENDING_TEST.getCode());
        batch.setRemark(dto.getRemark());
        milkBatchMapper.insert(batch);

        MilkTank tank = milkTankMapper.selectById(dto.getTankId());
        tank.setTankStatus(TankStatusEnum.PENDING_TEST.getCode());
        tank.setCurrentBatchId(batch.getId());
        tank.setCurrentVolume(dto.getMilkVolume());
        milkTankMapper.updateById(tank);

        log.info("牧场提交挤奶批次成功，批次号: {}, 奶罐: {}", batchNo, dto.getTankId());
        return batch;
    }

    public Page<MilkBatch> getBatchList(Long pageNum, Long pageSize, String status) {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<MilkBatch> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(MilkBatch::getPastureId, pastureId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MilkBatch::getBatchStatus, status);
        }
        wrapper.orderByDesc(MilkBatch::getCreateTime);
        return milkBatchMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public MilkBatch getBatchDetail(Long id) {
        return milkBatchMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBatchStatus(Long batchId, String status) {
        MilkBatch batch = milkBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        batch.setBatchStatus(status);
        milkBatchMapper.updateById(batch);
    }
}
