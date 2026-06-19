package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_node")
@Schema(description = "审批节点实体")
public class ApprovalNode extends BaseEntity {

    @Schema(description = "审批流ID")
    private Long processId;

    @Schema(description = "节点序号")
    private Integer nodeOrder;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "审批角色")
    private String approvalRole;

    @Schema(description = "指定审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "节点状态")
    private String nodeStatus;

    @Schema(description = "审批结果")
    private String approvalResult;

    @Schema(description = "审批意见")
    private String approvalRemark;

    @Schema(description = "审批时间")
    private java.time.LocalDateTime approvalTime;
}
