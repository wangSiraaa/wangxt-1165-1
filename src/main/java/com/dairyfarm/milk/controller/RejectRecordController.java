package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.entity.RejectRecord;
import com.dairyfarm.milk.service.RejectRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "拒收记录管理")
@RestController
@RequestMapping("/reject")
@RequiredArgsConstructor
public class RejectRecordController {

    private final RejectRecordService rejectRecordService;

    @Operation(summary = "查询拒收记录列表（展示拒收原因）")
    @GetMapping("/list")
    public Result<Page<RejectRecord>> getRejectList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "处理状态") @RequestParam(required = false) String handleStatus,
            @Parameter(description = "拒收类型") @RequestParam(required = false) String rejectType) {
        return Result.success(rejectRecordService.getRejectList(pageNum, pageSize, handleStatus, rejectType));
    }

    @Operation(summary = "查询拒收记录详情")
    @GetMapping("/{id}")
    public Result<RejectRecord> getRejectDetail(@Parameter(description = "拒收记录ID") @PathVariable Long id) {
        return Result.success(rejectRecordService.getRejectDetail(id));
    }

    @Operation(summary = "处理拒收记录")
    @PostMapping("/handle/{id}")
    public Result<RejectRecord> handleReject(
            @Parameter(description = "拒收记录ID") @PathVariable Long id,
            @Parameter(description = "处理状态") @RequestParam String handleStatus,
            @Parameter(description = "处理结果") @RequestParam String handleResult) {
        return Result.success(rejectRecordService.handleReject(id, handleStatus, handleResult));
    }
}
