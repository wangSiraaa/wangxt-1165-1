package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reject_record")
@Schema(description = "拒收记录实体")
public class RejectRecord extends BaseEntity {

    @Schema(description = "拒收单号")
    private String rejectNo;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "检测单ID")
    private Long testId;

    @Schema(description = "拒收类型")
    private String rejectType;

    @Schema(description = "拒收原因")
    private String rejectReason;

    @Schema(description = "拒收时间")
    private LocalDateTime rejectTime;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理状态")
    private String handleStatus;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "备注")
    private String remark;
}
