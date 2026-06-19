package com.dairyfarm.milk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dairyfarm.milk.common.Result;
import com.dairyfarm.milk.dto.AntibioticTestSubmitDTO;
import com.dairyfarm.milk.entity.AntibioticTest;
import com.dairyfarm.milk.service.AntibioticTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "抗生素检测管理")
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class AntibioticTestController {

    private final AntibioticTestService antibioticTestService;

    @Operation(summary = "化验室提交检测结果")
    @PostMapping("/submit")
    public Result<AntibioticTest> submitTest(@Validated @RequestBody AntibioticTestSubmitDTO dto) {
        return Result.success(antibioticTestService.submitTest(dto));
    }

    @Operation(summary = "修改检测结果")
    @PutMapping("/{id}")
    public Result<AntibioticTest> updateTest(
            @Parameter(description = "检测单ID") @PathVariable Long id,
            @Validated @RequestBody AntibioticTestSubmitDTO dto) {
        return Result.success(antibioticTestService.updateTest(id, dto));
    }

    @Operation(summary = "查询检测列表")
    @GetMapping("/list")
    public Result<Page<AntibioticTest>> getTestList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "检测结果") @RequestParam(required = false) String result) {
        return Result.success(antibioticTestService.getTestList(pageNum, pageSize, result));
    }

    @Operation(summary = "查询检测详情")
    @GetMapping("/{id}")
    public Result<AntibioticTest> getTestDetail(@Parameter(description = "检测单ID") @PathVariable Long id) {
        return Result.success(antibioticTestService.getTestDetail(id));
    }
}
