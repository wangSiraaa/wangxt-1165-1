package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.dto.ApprovalHandleDTO;
import com.dairyfarm.milk.dto.ApprovalQueryDTO;
import com.dairyfarm.milk.dto.ApprovalSubmitDTO;
import com.dairyfarm.milk.entity.ApprovalProcess;
import com.dairyfarm.milk.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.Map;

@Tag(name = "审批管理")
@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "提交审批")
    @PostMapping("/submit")
    public Result<ApprovalProcess> submit(@Validated @RequestBody ApprovalSubmitDTO dto) {
        return Result.success(approvalService.submitApproval(dto));
    }

    @Operation(summary = "审批通过")
    @PostMapping("/approve")
    public Result<Void> approve(@Validated @RequestBody ApprovalHandleDTO dto) {
        approvalService.approve(dto);
        return Result.success();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/reject")
    public Result<Void> reject(@Validated @RequestBody ApprovalHandleDTO dto) {
        approvalService.reject(dto);
        return Result.success();
    }

    @Operation(summary = "取消审批")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@Parameter(description = "审批流ID") @PathVariable Long id) {
        approvalService.cancelApproval(id);
        return Result.success();
    }

    @Operation(summary = "获取审批详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@Parameter(description = "审批流ID") @PathVariable Long id) {
        return Result.success(approvalService.getApprovalDetail(id));
    }

    @Operation(summary = "获取我的待审批列表")
    @GetMapping("/my-pending")
    public Result<Page<ApprovalProcess>> getMyPending(
            @Parameter(description = "审批状态") @RequestParam(required = false) String status,
            @Parameter(description = "审批类型") @RequestParam(required = false) String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize) {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setStatus(status);
        dto.setType(type);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return Result.success(approvalService.getMyApprovalList(dto));
    }

    @Operation(summary = "获取我提交的审批列表")
    @GetMapping("/my-applied")
    public Result<Page<ApprovalProcess>> getMyApplied(
            @Parameter(description = "审批状态") @RequestParam(required = false) String status,
            @Parameter(description = "审批类型") @RequestParam(required = false) String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize) {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setStatus(status);
        dto.setType(type);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return Result.success(approvalService.getMyAppliedList(dto));
    }

    @Operation(summary = "获取业务单据审批历史")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @Parameter(description = "业务单据ID") @RequestParam Long bizId,
            @Parameter(description = "审批类型") @RequestParam String approvalType) {
        return Result.success(approvalService.getApprovalHistory(bizId, approvalType));
    }
}
