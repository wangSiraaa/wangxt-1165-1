package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum RejectTypeEnum {

    TEST_FAILED("TEST_FAILED", "检测不合格"),
    TANK_DIRTY("TANK_DIRTY", "奶罐未清洗"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    RejectTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RejectTypeEnum getByCode(String code) {
        for (RejectTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
