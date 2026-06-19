package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.dto.MilkBatchSubmitDTO;
import com.dairyfarm.milk.entity.MilkBatch;
import com.dairyfarm.milk.service.MilkBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "挤奶批次管理")
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class MilkBatchController {

    private final MilkBatchService milkBatchService;

    @Operation(summary = "牧场提交挤奶批次")
    @PostMapping("/submit")
    public Result<MilkBatch> submitBatch(@Validated @RequestBody MilkBatchSubmitDTO dto) {
        return Result.success(milkBatchService.submitBatch(dto));
    }

    @Operation(summary = "查询批次列表")
    @GetMapping("/list")
    public Result<Page<MilkBatch>> getBatchList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "批次状态") @RequestParam(required = false) String status) {
        return Result.success(milkBatchService.getBatchList(pageNum, pageSize, status));
    }

    @Operation(summary = "查询批次详情")
    @GetMapping("/{id}")
    public Result<MilkBatch> getBatchDetail(@Parameter(description = "批次ID") @PathVariable Long id) {
        return Result.success(milkBatchService.getBatchDetail(id));
    }
}
