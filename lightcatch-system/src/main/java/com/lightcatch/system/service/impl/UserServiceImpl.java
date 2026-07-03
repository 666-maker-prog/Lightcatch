package com.lightcatch.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.common.exception.BusinessException;
import com.lightcatch.common.util.JwtUtil;
import com.lightcatch.system.dto.LoginReq;
import com.lightcatch.system.dto.RegisterReq;
import com.lightcatch.system.dto.UserInfoVO;
import com.lightcatch.system.entity.SysUser;
import com.lightcatch.system.mapper.SysUserMapper;
import com.lightcatch.system.service.IUserService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements IUserService {

    @Override
    public String login(LoginReq req) {
        SysUser user = baseMapper.findByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        String hashed = hashPassword(req.getPassword(), user.getSalt());
        if (!hashed.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return JwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public void register(RegisterReq req) {
        SysUser existing = baseMapper.findByUsername(req.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        String salt = UUID.randomUUID().toString().substring(0, 8);
        user.setSalt(salt);
        user.setPassword(hashPassword(req.getPassword(), salt));
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setStatus(1);
        user.setTenantId("0");
        save(user);
    }

    @Override
    public UserInfoVO getUserInfo(String userId) {
        SysUser user = getById(userId);
        if (user == null) return null;
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setTenantId(user.getTenantId());
        return vo;
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}
