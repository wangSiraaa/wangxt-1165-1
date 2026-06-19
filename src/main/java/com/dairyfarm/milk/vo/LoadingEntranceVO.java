package com.dairyfarm.milk.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "装车入口状态VO")
public class LoadingEntranceVO {

    @Schema(description = "是否可以装车")
    private Boolean canLoad;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "奶罐编号")
    private String tankCode;

    @Schema(description = "奶量(升)")
    private BigDecimal milkVolume;

    @Schema(description = "批次状态")
    private String batchStatus;

    @Schema(description = "批次状态描述")
    private String batchStatusDesc;

    @Schema(description = "是否锁定入口")
    private Boolean isLocked;

    @Schema(description = "锁定/拒收原因")
    private String lockReason;

    @Schema(description = "拒收类型")
    private String rejectType;

    @Schema(description = "拒收类型描述")
    private String rejectTypeDesc;

    @Schema(description = "最新检测结果")
    private String latestTestResult;

    @Schema(description = "最新检测结果描述")
    private String latestTestResultDesc;

    @Schema(description = "检测时间")
    private LocalDateTime testTime;

    @Schema(description = "检测单号")
    private String testNo;
}
