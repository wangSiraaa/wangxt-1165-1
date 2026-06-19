package com.dairyfarm.milk.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "牧场提交挤奶批次请求")
public class MilkBatchSubmitDTO {

    @NotNull(message = "奶罐ID不能为空")
    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "挤奶开始时间")
    private LocalDateTime milkingStartTime;

    @Schema(description = "挤奶结束时间")
    private LocalDateTime milkingEndTime;

    @NotNull(message = "奶量不能为空")
    @Schema(description = "奶量(升)")
    private BigDecimal milkVolume;

    @Schema(description = "奶温(℃)")
    private BigDecimal milkTemperature;

    @Schema(description = "头数")
    private Integer cowCount;

    @Schema(description = "挤奶员ID")
    private Long milkerId;

    @Schema(description = "备注")
    private String remark;
}
