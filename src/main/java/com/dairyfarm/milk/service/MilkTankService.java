package com.dairyfarm.milk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.mapper.MilkTankMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilkTankService {

    private final MilkTankMapper milkTankMapper;
    private final BusinessValidationService validationService;

    public Page<MilkTank> getTankList(Long pageNum, Long pageSize, String status) {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<MilkTank> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(MilkTank::getPastureId, pastureId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MilkTank::getTankStatus, status);
        }
        wrapper.orderByAsc(MilkTank::getTankCode);
        return milkTankMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<MilkTank> getAvailableTanks() {
        Long pastureId = UserContext.getPastureId();
        LambdaQueryWrapper<MilkTank> wrapper = new LambdaQueryWrapper<>();
        if (pastureId != null) {
            wrapper.eq(MilkTank::getPastureId, pastureId);
        }
        wrapper.eq(MilkTank::getTankStatus, "IDLE");
        wrapper.orderByAsc(MilkTank::getTankCode);
        return milkTankMapper.selectList(wrapper);
    }

    public MilkTank getTankDetail(Long id) {
        return milkTankMapper.selectById(id);
    }

    public boolean checkTankCleaned(Long tankId) {
        return validationService.isTankCleaned(tankId);
    }

    public MilkTank createTank(MilkTank tank) {
        Long pastureId = UserContext.getPastureId();
        if (pastureId != null) {
            tank.setPastureId(pastureId);
        }
        if (tank.getTankStatus() == null) {
            tank.setTankStatus("IDLE");
        }
        if (tank.getCurrentVolume() == null) {
            tank.setCurrentVolume(java.math.BigDecimal.ZERO);
        }
        milkTankMapper.insert(tank);
        log.info("创建奶罐成功，奶罐编号: {}", tank.getTankCode());
        return tank;
    }

    public MilkTank updateTank(MilkTank tank) {
        MilkTank exist = milkTankMapper.selectById(tank.getId());
        if (exist == null) {
            throw new BusinessException("奶罐不存在");
        }
        milkTankMapper.updateById(tank);
        return tank;
    }
}
