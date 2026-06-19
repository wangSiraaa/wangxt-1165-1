package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum TankStatusEnum {

    IDLE("IDLE", "空闲"),
    LOADING("LOADING", "接奶中"),
    PENDING_TEST("PENDING_TEST", "待检测"),
    TESTED("TESTED", "检测完成"),
    LOADED("LOADED", "已装车"),
    PENDING_CLEAN("PENDING_CLEAN", "待清洗");

    private final String code;
    private final String desc;

    TankStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TankStatusEnum getByCode(String code) {
        for (TankStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
