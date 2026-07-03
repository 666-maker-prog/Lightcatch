package com.lightcatch.system.dto;

import lombok.Data;

@Data
public class UserInfoVO {
    private String id;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private String tenantId;
}
