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
@TableName("loading_record")
@Schema(description = "装车记录实体")
public class LoadingRecord extends BaseEntity {

    @Schema(description = "装车单号")
    private String loadingNo;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "车牌号")
    private String truckNo;

    @Schema(description = "装车量(升)")
    private BigDecimal loadVolume;

    @Schema(description = "装车时奶温(℃)")
    private BigDecimal loadTemperature;

    @Schema(description = "装车时间")
    private LocalDateTime loadTime;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "到达时间")
    private LocalDateTime arrivalTime;

    @Schema(description = "状态")
    private String loadingStatus;

    @Schema(description = "备注")
    private String remark;
}
