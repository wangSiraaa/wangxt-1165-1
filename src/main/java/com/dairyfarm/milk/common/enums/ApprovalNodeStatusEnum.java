package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum ApprovalNodeStatusEnum {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "已完成"),
    SKIPPED("SKIPPED", "已跳过");

    private final String code;
    private final String desc;

    ApprovalNodeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApprovalNodeStatusEnum getByCode(String code) {
        for (ApprovalNodeStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
