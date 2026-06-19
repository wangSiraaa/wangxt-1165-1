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
@TableName("tank_cleaning")
@Schema(description = "奶罐清洗实体")
public class TankCleaning extends BaseEntity {

    @Schema(description = "清洗单号")
    private String cleaningNo;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "操作员ID")
    private Long operatorId;

    @Schema(description = "清洗开始时间")
    private LocalDateTime cleaningStartTime;

    @Schema(description = "清洗结束时间")
    private LocalDateTime cleaningEndTime;

    @Schema(description = "清洗方式")
    private String cleaningMethod;

    @Schema(description = "清洗剂")
    private String cleaningAgent;

    @Schema(description = "水温(℃)")
    private BigDecimal waterTemperature;

    @Schema(description = "是否合格: 0不合格, 1合格")
    private Integer isQualified;

    @Schema(description = "检查人ID")
    private Long checkerId;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "确认状态: PENDING待确认, CONFIRMED已确认")
    private String confirmStatus;

    @Schema(description = "备注")
    private String remark;
}
