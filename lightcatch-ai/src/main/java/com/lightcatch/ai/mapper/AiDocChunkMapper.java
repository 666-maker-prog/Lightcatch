package com.lightcatch.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lightcatch.ai.entity.AiDocChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiDocChunkMapper extends BaseMapper<AiDocChunk> {
    @Select("SELECT id, content, chunk_index AS \"chunkIndex\", " +
            "1 - (embedding <=> CAST(#{embedding} AS vector)) AS similarity " +
            "FROM ai_doc_chunk " +
            "WHERE knowledge_id = #{knowledgeId} " +
            "AND embedding IS NOT NULL " +
            "AND 1 - (embedding <=> CAST(#{embedding} AS vector)) >= #{minScore} " +
            "ORDER BY similarity DESC LIMIT #{topK}")
    List<Map<String, Object>> vectorSearch(
            @Param("knowledgeId") String knowledgeId,
            @Param("embedding") String embedding,
            @Param("topK") int topK,
            @Param("minScore") double minScore);

    @Update("UPDATE ai_doc_chunk SET embedding = CAST(#{embedding} AS vector) WHERE id = #{id}")
    int updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);

    @Select("SELECT count(*) FROM ai_doc_chunk WHERE knowledge_id = #{knowledgeId} AND embedding IS NOT NULL")
    int countEmbeddings(@Param("knowledgeId") String knowledgeId);
}
