package com.dairyfarm.milk.controller;

import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.service.TraceService;
import com.dairyfarm.milk.vo.TraceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "全链路追踪")
@RestController
@RequestMapping("/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    @Operation(summary = "按批次ID查询全链路追踪信息")
    @GetMapping("/batch/{batchId}")
    public Result<TraceVO> getTraceByBatchId(
            @Parameter(description = "批次ID") @PathVariable Long batchId) {
        return Result.success(traceService.getTraceByBatchId(batchId));
    }

    @Operation(summary = "按奶罐ID查询最新批次全链路追踪信息")
    @GetMapping("/tank/{tankId}")
    public Result<TraceVO> getTraceByTankId(
            @Parameter(description = "奶罐ID") @PathVariable Long tankId) {
        return Result.success(traceService.getTraceByTankId(tankId));
    }
}
