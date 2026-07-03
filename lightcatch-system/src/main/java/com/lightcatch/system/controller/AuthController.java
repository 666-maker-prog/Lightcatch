package com.lightcatch.system.controller;

import com.lightcatch.common.annotation.IgnoreAuth;
import com.lightcatch.common.model.Result;
import com.lightcatch.common.util.JwtUtil;
import com.lightcatch.system.dto.LoginReq;
import com.lightcatch.system.dto.RegisterReq;
import com.lightcatch.system.dto.UserInfoVO;
import com.lightcatch.system.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @IgnoreAuth
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginReq req) {
        String token = userService.login(req);
        return Result.ok(token);
    }

    @IgnoreAuth
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterReq req) {
        userService.register(req);
        return Result.ok("注册成功");
    }

    @GetMapping("/user-info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        return Result.ok(userService.getUserInfo(userId));
    }
}
