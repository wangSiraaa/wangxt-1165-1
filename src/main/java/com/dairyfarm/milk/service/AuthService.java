package com.dairyfarm.milk.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dairyfarm.milk.common.enums.RoleEnum;
import com.dairyfarm.milk.common.exception.BusinessException;
import com.dairyfarm.milk.common.utils.JwtUtil;
import com.dairyfarm.milk.dto.LoginDTO;
import com.dairyfarm.milk.entity.SysUser;
import com.dairyfarm.milk.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode(), user.getPastureId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("roleCode", user.getRoleCode());
        result.put("roleName", RoleEnum.getByCode(user.getRoleCode()).getDesc());
        result.put("pastureId", user.getPastureId());
        return result;
    }

    public void initDefaultUsers() {
        String[][] defaultUsers = {
            {"admin", "123456", "系统管理员", "13800138000", RoleEnum.ADMIN.getCode(), null},
            {"pasture1", "123456", "牧场用户1", "13800138001", RoleEnum.PASTURE.getCode(), "1"},
            {"lab1", "123456", "化验员1", "13800138002", RoleEnum.LAB.getCode(), "1"},
            {"driver1", "123456", "司机1", "13800138003", RoleEnum.DRIVER.getCode(), "1"}
        };

        for (String[] userInfo : defaultUsers) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, userInfo[0]);
            if (sysUserMapper.selectCount(wrapper) == 0) {
                SysUser user = new SysUser();
                user.setId(IdUtil.getSnowflakeNextId());
                user.setUsername(userInfo[0]);
                user.setPassword(BCrypt.hashpw(userInfo[1], BCrypt.gensalt()));
                user.setRealName(userInfo[2]);
                user.setPhone(userInfo[3]);
                user.setRoleCode(userInfo[4]);
                if (userInfo[5] != null) {
                    user.setPastureId(Long.parseLong(userInfo[5]));
                }
                user.setStatus(1);
                sysUserMapper.insert(user);
                log.info("初始化默认用户: {}", userInfo[0]);
            }
        }
    }
}
