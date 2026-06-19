package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_record")
@Schema(description = "审批操作记录实体")
public class ApprovalRecord extends BaseEntity {

    @Schema(description = "审批流ID")
    private Long processId;

    @Schema(description = "审批节点ID")
    private Long nodeId;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作意见")
    private String remark;

    @Schema(description = "操作时间")
    private java.time.LocalDateTime operateTime;
}
