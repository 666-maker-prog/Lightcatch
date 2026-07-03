package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_app")
public class AiApp {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;
    private String type;
    private String description;
    private String prompt;
    private String modelId;
    private String knowledgeIds;
    private String flowId;
    private String plugins;
    private String memoryId;
    private Integer msgNum;
    private String metadata;
    private Integer status;
    private String tenantId;
    private String userId;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
