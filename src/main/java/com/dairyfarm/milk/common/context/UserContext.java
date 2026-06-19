package com.dairyfarm.milk.common.context;

import lombok.Data;

@Data
public class UserContext {

    private Long userId;
    private String username;
    private String roleCode;
    private Long pastureId;

    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

    public static void set(UserContext userContext) {
        context.set(userContext);
    }

    public static UserContext get() {
        return context.get();
    }

    public static void remove() {
        context.remove();
    }

    public static Long getUserId() {
        UserContext ctx = context.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static String getRoleCode() {
        UserContext ctx = context.get();
        return ctx != null ? ctx.getRoleCode() : null;
    }

    public static Long getPastureId() {
        UserContext ctx = context.get();
        return ctx != null ? ctx.getPastureId() : null;
    }
}
