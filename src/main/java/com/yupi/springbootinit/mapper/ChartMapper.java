package com.yupi.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Chart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* @author 18126
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-05-17 15:37:12
* @Entity generator.domain.Chart
*/

public interface ChartMapper extends BaseMapper<Chart> {


    /**
     *
     * @param querySql
     * @return
     */
    List<Map<String,Object>> queryChartData(String querySql);

}




