package com.yupi.springbootinit.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 18126
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-05-17 15:37:12
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        String chartType = chartQueryRequest.getChartType();
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null &&id>0 , "id",id);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        queryWrapper.eq(StringUtils.isNotBlank(name),"name",name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq("isDelete",false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

     public void handleChartUpdateError(long chartId,String message){
        Chart updateChartError = new Chart();
        updateChartError.setId(chartId);
        updateChartError.setStatus("failed");
        updateChartError.setGenResult(message);
        boolean b = this.updateById(updateChartError);
        if (!b) {
            log.error("图表更新失败" + chartId + message);
        }
    }
}




