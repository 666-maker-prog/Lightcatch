package com.lightcatch.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lightcatch.ai.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {
    @Select("SELECT * FROM ai_model WHERE model_type = #{type} AND status = 1 ORDER BY sort_order")
    List<AiModel> findByType(String type);

    @Select("SELECT * FROM ai_model WHERE is_default = true AND model_type = #{type} AND status = 1 LIMIT 1")
    AiModel findDefaultByType(String type);
}
