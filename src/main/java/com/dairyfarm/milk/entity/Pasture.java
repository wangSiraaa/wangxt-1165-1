package com.dairyfarm.milk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dairyfarm.milk.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pasture")
@Schema(description = "牧场实体")
public class Pasture extends BaseEntity {

    @Schema(description = "牧场编码")
    private String pastureCode;

    @Schema(description = "牧场名称")
    private String pastureName;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态: 0禁用, 1启用")
    private Integer status;
}
