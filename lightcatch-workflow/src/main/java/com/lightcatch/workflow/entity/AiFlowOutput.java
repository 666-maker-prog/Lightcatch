package com.lightcatch.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_flow_output")
public class AiFlowOutput {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String flowId;
    private String userId;
    private String title;
    private String content;
    private String media;
    private String platforms;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Date updateTime;
}
