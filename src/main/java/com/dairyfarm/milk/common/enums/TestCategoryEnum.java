package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum TestCategoryEnum {

    QUICK("QUICK", "快检"),
    RECHECK("RECHECK", "复检");

    private final String code;
    private final String desc;

    TestCategoryEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TestCategoryEnum getByCode(String code) {
        for (TestCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return null;
    }
}
