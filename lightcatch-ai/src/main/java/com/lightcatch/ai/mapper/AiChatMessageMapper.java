package com.lightcatch.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lightcatch.ai.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
    @Select("SELECT * FROM ai_chat_message WHERE conversation_id = #{conversationId} ORDER BY create_time ASC")
    List<AiChatMessage> findByConversationId(String conversationId);
}
