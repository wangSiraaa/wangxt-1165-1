package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.dto.LoadingConfirmDTO;
import com.dairyfarm.milk.entity.LoadingRecord;
import com.dairyfarm.milk.service.LoadingRecordService;
import com.dairyfarm.milk.vo.LoadingEntranceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "装车记录管理")
@RestController
@RequestMapping("/loading")
@RequiredArgsConstructor
public class LoadingRecordController {

    private final LoadingRecordService loadingRecordService;

    @Operation(summary = "司机确认装车")
    @PostMapping("/confirm")
    public Result<LoadingRecord> confirmLoading(@Validated @RequestBody LoadingConfirmDTO dto) {
        return Result.success(loadingRecordService.confirmLoading(dto));
    }

    @Operation(summary = "查询装车记录列表")
    @GetMapping("/list")
    public Result<Page<LoadingRecord>> getLoadingList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "装车状态") @RequestParam(required = false) String status) {
        return Result.success(loadingRecordService.getLoadingList(pageNum, pageSize, status));
    }

    @Operation(summary = "查询装车记录详情")
    @GetMapping("/{id}")
    public Result<LoadingRecord> getLoadingDetail(@Parameter(description = "装车记录ID") @PathVariable Long id) {
        return Result.success(loadingRecordService.getLoadingDetail(id));
    }

    @Operation(summary = "查询装车入口状态（含拒收原因）")
    @GetMapping("/entrance/{batchId}")
    public Result<LoadingEntranceVO> getLoadingEntranceStatus(
            @Parameter(description = "批次ID") @PathVariable Long batchId) {
        return Result.success(loadingRecordService.getLoadingEntranceStatus(batchId));
    }
}
