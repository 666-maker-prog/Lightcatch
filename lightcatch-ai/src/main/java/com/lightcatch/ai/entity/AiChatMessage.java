package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_chat_message")
public class AiChatMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String conversationId;
    private String role;
    private String content;
    private String toolExecutionRequests;
    private String toolExecutionResult;
    private Integer tokens;
    private String modelName;
    private String thinking;
    private String tenantId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
