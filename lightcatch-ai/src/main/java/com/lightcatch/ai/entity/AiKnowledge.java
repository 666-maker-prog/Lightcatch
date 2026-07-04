package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_knowledge")
public class AiKnowledge {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;
    private String description;
    private String type;
    private String embedModelId;
    private Integer docCount;
    private Integer status;
    private String tenantId;
    private String userId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
