package com.dairyfarm.milk.config;

import com.dairyfarm.milk.common.context.UserContext;
import com.dairyfarm.milk.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.contains("/auth/login") || uri.contains("/doc.html") || uri.contains("/swagger") || 
            uri.contains("/v2/api-docs") || uri.contains("/webjars")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                UserContext userContext = new UserContext();
                userContext.setUserId(jwtUtil.getUserIdFromToken(token));
                userContext.setRoleCode(jwtUtil.getRoleCodeFromToken(token));
                userContext.setPastureId(jwtUtil.getPastureIdFromToken(token));
                UserContext.set(userContext);
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
