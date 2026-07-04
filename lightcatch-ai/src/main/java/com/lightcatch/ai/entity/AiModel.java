package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_model")
public class AiModel {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;
    private String provider;
    private String modelName;
    private String apiKey;
    private String baseUrl;
    private String modelType;
    private String modelParams;
    private String description;
    private Integer sortOrder;
    private Boolean isDefault;
    private Integer status;
    private String tenantId;
    private String userId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
