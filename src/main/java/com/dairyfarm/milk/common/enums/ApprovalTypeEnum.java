package com.dairyfarm.milk.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ApprovalTypeEnum {

    TEST_RESULT("TEST_RESULT", "检测结果审批", Arrays.asList(
            RoleEnum.LAB_SUPERVISOR.getCode(),
            RoleEnum.QUALITY_SUPERVISOR.getCode()
    )),
    REJECT_HANDLE("REJECT_HANDLE", "拒收处理审批", Arrays.asList(
            RoleEnum.PASTURE_SUPERVISOR.getCode(),
            RoleEnum.QUALITY_SUPERVISOR.getCode()
    )),
    EXCEPTION("EXCEPTION", "异常处理审批", Arrays.asList(
            RoleEnum.ON_SITE_SUPERVISOR.getCode(),
            RoleEnum.DEPARTMENT_MANAGER.getCode(),
            RoleEnum.QUALITY_DIRECTOR.getCode()
    ));

    private final String code;
    private final String desc;
    private final List<String> approvalRoles;

    ApprovalTypeEnum(String code, String desc, List<String> approvalRoles) {
        this.code = code;
        this.desc = desc;
        this.approvalRoles = approvalRoles;
    }

    public static ApprovalTypeEnum getByCode(String code) {
        for (ApprovalTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public List<String> getApprovalRoles() {
        return approvalRoles;
    }

    public int getApprovalLevelCount() {
        return approvalRoles.size();
    }

    public String getApprovalRole(int level) {
        if (level < 0 || level >= approvalRoles.size()) {
            return null;
        }
        return approvalRoles.get(level);
    }
}
