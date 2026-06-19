package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatusEnum {

    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    ApprovalStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApprovalStatusEnum getByCode(String code) {
        for (ApprovalStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
