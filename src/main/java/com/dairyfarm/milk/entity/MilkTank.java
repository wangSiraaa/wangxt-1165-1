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
@TableName("milk_tank")
@Schema(description = "奶罐实体")
public class MilkTank extends BaseEntity {

    @Schema(description = "奶罐编号")
    private String tankCode;

    @Schema(description = "奶罐名称")
    private String tankName;

    @Schema(description = "容量(升)")
    private BigDecimal capacity;

    @Schema(description = "当前存量(升)")
    private BigDecimal currentVolume;

    @Schema(description = "状态")
    private String tankStatus;

    @Schema(description = "上次清洗时间")
    private LocalDateTime lastCleanTime;

    @Schema(description = "当前批次ID")
    private Long currentBatchId;

    @Schema(description = "所属牧场ID")
    private Long pastureId;

    @Schema(description = "备注")
    private String remark;
}
