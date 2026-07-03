package com.lightcatch.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightcatch.system.entity.SysUser;
import com.lightcatch.system.dto.LoginReq;
import com.lightcatch.system.dto.RegisterReq;
import com.lightcatch.system.dto.UserInfoVO;

public interface IUserService extends IService<SysUser> {
    String login(LoginReq req);
    void register(RegisterReq req);
    UserInfoVO getUserInfo(String userId);
}
