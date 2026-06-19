package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum TestResultEnum {

    PASSED("PASSED", "合格"),
    FAILED("FAILED", "不合格");

    private final String code;
    private final String desc;

    TestResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TestResultEnum getByCode(String code) {
        for (TestResultEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
