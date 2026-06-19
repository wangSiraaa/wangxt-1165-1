package com.dairyfarm.milk.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "全链路追踪VO")
public class TraceVO {

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "牧场名称")
    private String pastureName;

    @Schema(description = "奶罐信息")
    private TankInfoVO tankInfo;

    @Schema(description = "批次信息")
    private BatchInfoVO batchInfo;

    @Schema(description = "清洗信息")
    private CleaningInfoVO cleaningInfo;

    @Schema(description = "检测信息列表")
    private List<TestInfoVO> testInfoList;

    @Schema(description = "装车/运输信息")
    private LoadingInfoVO loadingInfo;

    @Schema(description = "拒收信息")
    private RejectInfoVO rejectInfo;

    @Data
    @Schema(description = "奶罐信息")
    public static class TankInfoVO {
        @Schema(description = "奶罐ID")
        private Long tankId;

        @Schema(description = "奶罐编号")
        private String tankCode;

        @Schema(description = "奶罐名称")
        private String tankName;

        @Schema(description = "容量(升)")
        private BigDecimal capacity;

        @Schema(description = "状态")
        private String tankStatus;

        @Schema(description = "状态描述")
        private String tankStatusDesc;
    }

    @Data
    @Schema(description = "批次信息")
    public static class BatchInfoVO {
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

        @Schema(description = "挤奶员姓名")
        private String milkerName;

        @Schema(description = "批次状态")
        private String batchStatus;

        @Schema(description = "批次状态描述")
        private String batchStatusDesc;
    }

    @Data
    @Schema(description = "清洗信息")
    public static class CleaningInfoVO {
        @Schema(description = "清洗记录ID")
        private Long cleaningId;

        @Schema(description = "清洗单号")
        private String cleaningNo;

        @Schema(description = "操作员ID")
        private Long operatorId;

        @Schema(description = "操作员姓名")
        private String operatorName;

        @Schema(description = "清洗开始时间")
        private LocalDateTime cleaningStartTime;

        @Schema(description = "清洗结束时间")
        private LocalDateTime cleaningEndTime;

        @Schema(description = "清洗方式")
        private String cleaningMethod;

        @Schema(description = "是否合格")
        private Integer isQualified;

        @Schema(description = "确认状态")
        private String confirmStatus;

        @Schema(description = "确认状态描述")
        private String confirmStatusDesc;

        @Schema(description = "检查人ID")
        private Long checkerId;

        @Schema(description = "检查人姓名")
        private String checkerName;

        @Schema(description = "检查时间")
        private LocalDateTime checkTime;
    }

    @Data
    @Schema(description = "检测信息")
    public static class TestInfoVO {
        @Schema(description = "检测单ID")
        private Long testId;

        @Schema(description = "检测单号")
        private String testNo;

        @Schema(description = "父检测单ID")
        private Long parentId;

        @Schema(description = "检测类别: QUICK快检, RECHECK复检")
        private String testCategory;

        @Schema(description = "检测类别描述")
        private String testCategoryDesc;

        @Schema(description = "检测类型")
        private String testType;

        @Schema(description = "检测方法")
        private String testMethod;

        @Schema(description = "检测结果")
        private String testResult;

        @Schema(description = "检测结果描述")
        private String testResultDesc;

        @Schema(description = "检测值")
        private String testValue;

        @Schema(description = "检测时间")
        private LocalDateTime testTime;

        @Schema(description = "化验员ID")
        private Long labId;

        @Schema(description = "化验员姓名")
        private String labName;

        @Schema(description = "是否锁定")
        private Integer isLocked;

        @Schema(description = "审批状态")
        private String approveStatus;

        @Schema(description = "审批状态描述")
        private String approveStatusDesc;
    }

    @Data
    @Schema(description = "装车/运输信息")
    public static class LoadingInfoVO {
        @Schema(description = "装车记录ID")
        private Long loadingId;

        @Schema(description = "装车单号")
        private String loadingNo;

        @Schema(description = "司机ID")
        private Long driverId;

        @Schema(description = "司机姓名")
        private String driverName;

        @Schema(description = "运输责任人姓名")
        private String transporterName;

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

        @Schema(description = "状态描述")
        private String loadingStatusDesc;
    }

    @Data
    @Schema(description = "拒收信息")
    public static class RejectInfoVO {
        @Schema(description = "拒收记录ID")
        private Long rejectId;

        @Schema(description = "拒收单号")
        private String rejectNo;

        @Schema(description = "拒收类型")
        private String rejectType;

        @Schema(description = "拒收类型描述")
        private String rejectTypeDesc;

        @Schema(description = "拒收原因")
        private String rejectReason;

        @Schema(description = "拒收时间")
        private LocalDateTime rejectTime;

        @Schema(description = "操作人ID")
        private Long operatorId;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "处理状态")
        private String handleStatus;

        @Schema(description = "处理状态描述")
        private String handleStatusDesc;

        @Schema(description = "处理结果")
        private String handleResult;

        @Schema(description = "处理人ID")
        private Long handlerId;

        @Schema(description = "处理人姓名")
        private String handlerName;

        @Schema(description = "处理时间")
        private LocalDateTime handleTime;
    }
}
