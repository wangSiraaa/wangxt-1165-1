package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.entity.MilkTank;
import com.dairyfarm.milk.service.MilkTankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "奶罐管理")
@RestController
@RequestMapping("/tank")
@RequiredArgsConstructor
public class MilkTankController {

    private final MilkTankService milkTankService;

    @Operation(summary = "查询奶罐列表")
    @GetMapping("/list")
    public Result<Page<MilkTank>> getTankList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "奶罐状态") @RequestParam(required = false) String status) {
        return Result.success(milkTankService.getTankList(pageNum, pageSize, status));
    }

    @Operation(summary = "查询可用奶罐列表（空闲且已清洗）")
    @GetMapping("/available")
    public Result<List<MilkTank>> getAvailableTanks() {
        return Result.success(milkTankService.getAvailableTanks());
    }

    @Operation(summary = "查询奶罐详情")
    @GetMapping("/{id}")
    public Result<MilkTank> getTankDetail(@Parameter(description = "奶罐ID") @PathVariable Long id) {
        return Result.success(milkTankService.getTankDetail(id));
    }

    @Operation(summary = "检查奶罐是否已清洗")
    @GetMapping("/check-cleaned/{tankId}")
    public Result<Map<String, Boolean>> checkTankCleaned(@Parameter(description = "奶罐ID") @PathVariable Long tankId) {
        boolean cleaned = milkTankService.checkTankCleaned(tankId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("cleaned", cleaned);
        return Result.success(result);
    }

    @Operation(summary = "新增奶罐")
    @PostMapping
    public Result<MilkTank> createTank(@RequestBody MilkTank tank) {
        return Result.success(milkTankService.createTank(tank));
    }

    @Operation(summary = "更新奶罐")
    @PutMapping
    public Result<MilkTank> updateTank(@RequestBody MilkTank tank) {
        return Result.success(milkTankService.updateTank(tank));
    }
}
