package com.dairyfarm.milk.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "司机确认装车请求")
public class LoadingConfirmDTO {

    @NotNull(message = "批次ID不能为空")
    @Schema(description = "批次ID")
    private Long batchId;

    @NotNull(message = "装车量不能为空")
    @Schema(description = "装车量(升)")
    private BigDecimal loadVolume;

    @Schema(description = "装车时奶温(℃)")
    private BigDecimal loadTemperature;

    @NotNull(message = "装车时间不能为空")
    @Schema(description = "装车时间")
    private LocalDateTime loadTime;

    @Schema(description = "车牌号")
    private String truckNo;

    @Schema(description = "运输责任人姓名")
    private String transporterName;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "备注")
    private String remark;
}
