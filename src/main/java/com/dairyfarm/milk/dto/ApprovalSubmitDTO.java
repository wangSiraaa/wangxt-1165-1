package com.dairyfarm.milk.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "提交审批请求")
public class ApprovalSubmitDTO {

    @NotBlank(message = "审批类型不能为空")
    @Schema(description = "审批类型")
    private String approvalType;

    @NotNull(message = "业务单据ID不能为空")
    @Schema(description = "业务单据ID")
    private Long bizId;

    @Schema(description = "业务单据号")
    private String bizNo;

    @Schema(description = "业务类型")
    private String bizType;

    @NotBlank(message = "审批标题不能为空")
    @Schema(description = "审批标题")
    private String title;

    @Schema(description = "审批备注")
    private String remark;
}
