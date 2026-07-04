package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_mcp")
public class AiMcp {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;
    private String type;
    private String endpoint;
    private String headers;
    private String category;
    private String description;
    private Integer status;
    private String tenantId;
    private String userId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
