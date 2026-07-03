package com.lightcatch.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_doc_chunk")
public class AiDocChunk {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String docId;
    private String knowledgeId;
    private String content;
    private Integer chunkIndex;
    private String metadata;
    private String tenantId;
    private String username;
    private Date createTime;
}
