package com.lightcatch.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String username;
    private String realName;
    private String avatar;
    private String password;
    private String salt;
    private Integer status;
    private String phone;
    private String email;
    private String tenantId;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private String updateBy;
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}
