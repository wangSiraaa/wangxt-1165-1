package com.dairyfarm.milk.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.enums.TankStatusEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.entity.TankCleaning;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import com.dairyfarm.milk.mapper.TankCleaningMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TankCleaningService {

    private final TankCleaningMapper tankCleaningMapper;
    private final MilkTankMapper milkTankMapper;

    @Transactional(rollbackFor = Exception.class)
    public TankCleaning startCleaning(Long tankId, LocalDateTime startTime, String cleaningMethod, String cleaningAgent) {
        Long pastureId = UserContext.getPastureId();
        if (pastureId == null) {
            throw new BusinessException("用户未关联牧场");
        }

        MilkTank tank = milkTankMapper.selectById(tankId);
        if (tank == null) {
            throw new BusinessException("奶罐不存在");
        }
        if (!tank.getPastureId().equals(pastureId)) {
            throw new BusinessException("该奶罐不属于当前牧场");
        }

        String cleaningNo = "QX" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.randomUUID().substring(0, 4).toUpperCase();

        TankCleaning cleaning = new TankCleaning();
        cleaning.setCleaningNo(cleaningNo);
        cleaning.setTankId(tankId);
        cleaning.setPastureId(pastureId);
        cleaning.setOperatorId(UserContext.getUserId());
        cleaning.setCleaningStartTime(startTime != null ? startTime : LocalDateTime.now());
        cleaning.setCleaningMethod(cleaningMethod);
        cleaning.setCleaningAgent(cleaningAgent);
        tankCleaningMapper.insert(cleaning);

        log.info("开始清洗奶罐，清洗单号: {}, 奶罐: {}", cleaningNo, tankId);
        return cleaning;
    }

    @Transactional(rollbackFor = Exception.class)
    public TankCleaning finishCleaning(Long id, LocalDateTime endTime, BigDecimal waterTemperature, Integer isQualified) {
        TankCleaning cleaning = tankCleaningMapper.selectById(id);
        if (cleaning == null) {
            throw new BusinessException("清洗记录不存在");
        }

        cleaning.setCleaningEndTime(endTime != null ? endTime : LocalDateTime.now());
        cleaning.setWaterTemperature(waterTemperature);
        cleaning.setIsQualified(isQualified);
        cleaning.setCheckerId(UserContext.getUserId());
        cleaning.setCheckTime(LocalDateTime.now());
        tankCleaningMapper.updateById(cleaning);

        if (isQualified != null && isQualified == 1) {
            MilkTank tank = milkTankMapper.selectById(cleaning.getTankId());
            tank.setTankStatus(TankStatusEnum.IDLE.getCode());
            tank.setLastCleanTime(LocalDateTime.now());
            milkTankMapper.updateById(tank);
            log.info("奶罐清洗合格，奶罐状态已更新为空闲，奶罐: {}", cleaning.getTankId());
        }

        return cleaning;
    }

    public Page<TankCleaning> getCleaningList(Long pageNum, Long pageSize, Long tankId, Integer isQualified) {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<TankCleaning> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(TankCleaning::getPastureId, pastureId);
        }
        if (tankId != null) {
            wrapper.eq(TankCleaning::getTankId, tankId);
        }
        if (isQualified != null) {
            wrapper.eq(TankCleaning::getIsQualified, isQualified);
        }
        wrapper.orderByDesc(TankCleaning::getCreateTime);
        return tankCleaningMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public TankCleaning getCleaningDetail(Long id) {
        return tankCleaningMapper.selectById(id);
    }
}
