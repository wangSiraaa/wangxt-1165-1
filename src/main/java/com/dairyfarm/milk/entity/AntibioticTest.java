package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("antibiotic_test")
@Schema(description = "抗生素检测实体")
public class AntibioticTest extends BaseEntity {

    @Schema(description = "父检测单ID(复检关联)")
    private Long parentId;

    @Schema(description = "检测单号")
    private String testNo;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "奶罐ID")
    private Long tankId;

    @Schema(description = "牧场ID")
    private Long pastureId;

    @Schema(description = "化验员ID")
    private Long labId;

    @Schema(description = "检测类型")
    private String testType;

    @Schema(description = "检测类别: QUICK快检, RECHECK复检")
    private String testCategory;

    @Schema(description = "检测方法")
    private String testMethod;

    @Schema(description = "检测结果")
    private String testResult;

    @Schema(description = "检测值")
    private String testValue;

    @Schema(description = "检测时间")
    private LocalDateTime testTime;

    @Schema(description = "是否锁定")
    private Integer isLocked;

    @Schema(description = "审批状态")
    private String approveStatus;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "审批意见")
    private String approveRemark;

    @Schema(description = "备注")
    private String remark;
}
