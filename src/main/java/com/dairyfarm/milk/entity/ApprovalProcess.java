package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_process")
@Schema(description = "审批流主表实体")
public class ApprovalProcess extends BaseEntity {

    @Schema(description = "审批类型")
    private String approvalType;

    @Schema(description = "业务单据ID")
    private Long bizId;

    @Schema(description = "业务单据号")
    private String bizNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "审批标题")
    private String title;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "当前审批节点")
    private Integer currentNode;

    @Schema(description = "审批总级数")
    private Integer totalNodes;

    @Schema(description = "提交人ID")
    private Long submitterId;

    @Schema(description = "提交人姓名")
    private String submitterName;

    @Schema(description = "审批备注")
    private String remark;

    @Schema(description = "完成时间")
    private java.time.LocalDateTime completeTime;
}
