package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_conversation")
public class AiConversation {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String userId;
    private String title;
    private String modelId;
    private String knowledgeIds;
    private String type;
    private String flowId;
    private Integer status;
    private String tenantId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
