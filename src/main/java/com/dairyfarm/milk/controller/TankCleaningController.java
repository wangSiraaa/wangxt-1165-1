package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.entity.TankCleaning;
import com.dairyfarm.milk.service.TankCleaningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "奶罐清洗管理")
@RestController
@RequestMapping("/cleaning")
@RequiredArgsConstructor
public class TankCleaningController {

    private final TankCleaningService tankCleaningService;

    @Operation(summary = "开始清洗奶罐")
    @PostMapping("/start")
    public Result<TankCleaning> startCleaning(
            @Parameter(description = "奶罐ID") @RequestParam Long tankId,
            @Parameter(description = "清洗开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "清洗方式") @RequestParam(required = false) String cleaningMethod,
            @Parameter(description = "清洗剂") @RequestParam(required = false) String cleaningAgent) {
        return Result.success(tankCleaningService.startCleaning(tankId, startTime, cleaningMethod, cleaningAgent));
    }

    @Operation(summary = "完成清洗")
    @PostMapping("/finish/{id}")
    public Result<TankCleaning> finishCleaning(
            @Parameter(description = "清洗记录ID") @PathVariable Long id,
            @Parameter(description = "清洗结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "水温") @RequestParam(required = false) BigDecimal waterTemperature,
            @Parameter(description = "是否合格") @RequestParam Integer isQualified) {
        return Result.success(tankCleaningService.finishCleaning(id, endTime, waterTemperature, isQualified));
    }

    @Operation(summary = "查询清洗记录列表")
    @GetMapping("/list")
    public Result<Page<TankCleaning>> getCleaningList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "奶罐ID") @RequestParam(required = false) Long tankId,
            @Parameter(description = "是否合格") @RequestParam(required = false) Integer isQualified) {
        return Result.success(tankCleaningService.getCleaningList(pageNum, pageSize, tankId, isQualified));
    }

    @Operation(summary = "查询清洗记录详情")
    @GetMapping("/{id}")
    public Result<TankCleaning> getCleaningDetail(@Parameter(description = "清洗记录ID") @PathVariable Long id) {
        return Result.success(tankCleaningService.getCleaningDetail(id));
    }
}
