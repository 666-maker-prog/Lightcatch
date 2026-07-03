package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_knowledge_doc")
public class AiKnowledgeDoc {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String knowledgeId;
    private String title;
    private String fileType;
    private String content;
    private String metadata;
    private Integer wordCount;
    private Integer chunkCount;
    private Integer status;
    private String errorMsg;
    private String tenantId;
    private String userId;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
