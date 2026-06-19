package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("milk_batch")
@Schema(description = "挤奶批次实体")
public class MilkBatch extends BaseEntity {

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "挤奶开始时间")
    private LocalDateTime milkingStartTime;

    @Schema(description = "挤奶结束时间")
    private LocalDateTime milkingEndTime;

    @Schema(description = "奶量(升)")
    private BigDecimal milkVolume;

    @Schema(description = "奶温(℃)")
    private BigDecimal milkTemperature;

    @Schema(description = "头数")
    private Integer cowCount;

    @Schema(description = "挤奶员ID")
    private Long milkerId;

    @Schema(description = "状态")
    private String batchStatus;

    @Schema(description = "备注")
    private String remark;
}
