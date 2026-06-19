package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum OperationTypeEnum {

    SUBMIT("SUBMIT", "提交"),
    APPROVE("APPROVE", "同意"),
    REJECT("REJECT", "驳回"),
    CANCEL("CANCEL", "取消"),
    REASSIGN("REASSIGN", "转办");

    private final String code;
    private final String desc;

    OperationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OperationTypeEnum getByCode(String code) {
        for (OperationTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
