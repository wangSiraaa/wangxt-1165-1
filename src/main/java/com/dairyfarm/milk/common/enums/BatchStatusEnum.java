package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum BatchStatusEnum {

    MILKING("MILKING", "挤奶中"),
    PENDING_TEST("PENDING_TEST", "待检测"),
    TEST_PASSED("TEST_PASSED", "检测通过"),
    TEST_FAILED("TEST_FAILED", "检测不合格"),
    LOADED("LOADED", "已装车"),
    REJECTED("REJECTED", "已拒收");

    private final String code;
    private final String desc;

    BatchStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BatchStatusEnum getByCode(String code) {
        for (BatchStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
