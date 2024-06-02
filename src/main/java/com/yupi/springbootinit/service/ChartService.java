package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;

/**
* @author 18126
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-05-17 15:37:12
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    public void handleChartUpdateError(long chartId,String message);

}
