package com.dairyfarm.milk.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "审批处理请求")
public class ApprovalHandleDTO {

    @NotNull(message = "审批流ID不能为空")
    @Schema(description = "审批流ID")
    private Long processId;

    @NotNull(message = "审批节点ID不能为空")
    @Schema(description = "审批节点ID")
    private Long nodeId;

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "审批结果: APPROVED同意, REJECTED驳回")
    private String approvalResult;

    @Schema(description = "审批意见")
    private String remark;
}
