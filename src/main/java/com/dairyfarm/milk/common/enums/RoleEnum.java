package com.dairyfarm.milk.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("ADMIN", "管理员"),
    PASTURE("PASTURE", "牧场用户"),
    LAB("LAB", "化验员"),
    DRIVER("DRIVER", "司机"),
    LAB_SUPERVISOR("LAB_SUPERVISOR", "化验室主管"),
    QUALITY_SUPERVISOR("QUALITY_SUPERVISOR", "质量主管"),
    PASTURE_SUPERVISOR("PASTURE_SUPERVISOR", "牧场主管"),
    DEPARTMENT_MANAGER("DEPARTMENT_MANAGER", "部门经理"),
    QUALITY_DIRECTOR("QUALITY_DIRECTOR", "质量总监"),
    ON_SITE_SUPERVISOR("ON_SITE_SUPERVISOR", "现场主管");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
