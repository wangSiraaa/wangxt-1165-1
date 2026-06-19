package com.dairyfarm.milk.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "化验室提交抗生素检测结果请求")
public class AntibioticTestSubmitDTO {

    @NotNull(message = "批次ID不能为空")
    @Schema(description = "批次ID")
    private Long batchId;

    @NotBlank(message = "检测类型不能为空")
    @Schema(description = "检测类型")
    private String testType;

    @Schema(description = "检测方法")
    private String testMethod;

    @NotBlank(message = "检测结果不能为空")
    @Schema(description = "检测结果: PASSED合格, FAILED不合格")
    private String testResult;

    @Schema(description = "检测值")
    private String testValue;

    @NotNull(message = "检测时间不能为空")
    @Schema(description = "检测时间")
    private LocalDateTime testTime;

    @Schema(description = "备注")
    private String remark;
}
