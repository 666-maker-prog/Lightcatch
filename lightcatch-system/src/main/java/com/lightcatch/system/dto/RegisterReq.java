package com.lightcatch.system.dto;

import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
}
