package com.dairyfarm.milk.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "审批查询请求")
public class ApprovalQueryDTO {

    @Schema(description = "审批状态")
    private String status;

    @Schema(description = "审批类型")
    private String type;

    @Schema(description = "页码")
    private Long pageNum = 1L;

    @Schema(description = "每页条数")
    private Long pageSize = 10L;
}
