package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.BiMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimitManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimitManager redisLimitManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
//    @PostMapping("/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
//        long current = chartQueryRequest.getCurrent();
//        long size = chartQueryRequest.getPageSize();
//        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
//                getQueryWrapper(chartQueryRequest));
//        return ResultUtils.success(chartPage);
//    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);


        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        return chartService.getQueryWrapper(chartQueryRequest);
    }


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String chartType = genChartByAiRequest.getChartType();
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();

        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");

        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR,"名称过长");
        /**
         * 校验文件
         *
         * 首先拿到用户请求的文件
         * 取到文件的大小
         */
        long size = multipartFile.getSize();

        //获取文件的原始文件名
        String originalFilename = multipartFile.getOriginalFilename();

        /**
         * 检验文件的大小
         *
         * 定义一个常量表示1MB
         * 1MB = 1024 * 1024 Byte = 2^10 Byte(字节)
         */

        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件大小超过1MB");


        /**
         * 校验文件后缀
         * 一般文件名是aaa.png ,所以我们只需要取到 . 后面的内容即可
         * 使用FileUtil工具类中的getSuffix方法获取文件名后缀
         */
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("xls","xlsx");
        //如果suffix的后缀不在范围内，抛出异常
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀非法 ");

//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("你是一个数据分析师，接下来我会给你我的分析目标和原始数据，请告诉我分析结论。");
//        stringBuilder.append("分析目标：").append(goal).append("\n");
//
//        String result = ExcelUtils.excelToCsv(multipartFile);
//        stringBuilder.append("数据：").append(result).append("\n");

        User loginUser = userService.getLoginUser(request);
        //redis限流
        redisLimitManager.doRedisLimit("genChartByAi " + loginUser.getId());
        long biModelId = CommonConstant.BI_MODEL_ID;

        /**
         * 分析需求：
         * 网站用户增长情况
         * 原始数据：
         * 日期,用户数
         * 1号,10
         * 2号,20
         * 3号,30
         */

        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求").append("\n");

        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据，把multipart传过来
        String csvData =  ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");


        //拿到返回结果
        String result = aiManager.doChat(biModelId, userInput.toString());

        String[] split = result.split("【【【【【");
        if (split.length < 3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Ai 生成错误");
        }

        String genChart = split[1].trim();
        String genResult = split[2].trim();

        //插入数据
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setStatus("success");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");


        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 异步响应
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String chartType = genChartByAiRequest.getChartType();
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();

        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR,"名称过长");

        long size = multipartFile.getSize();
        //获取文件的原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件大小超过1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("xls","xlsx");
        //如果suffix的后缀不在范围内，抛出异常
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀非法 ");

        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRedisLimit("genChartByAi " + loginUser.getId());
        long biModelId = CommonConstant.BI_MODEL_ID;
        /**
         * 分析需求：
         * 网站用户增长情况
         * 原始数据：
         * 日期,用户数
         * 1号,10
         * 2号,20
         * 3号,30
         */

        //用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求").append("\n");

        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据，把multipart传过来
        String csvData =  ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");


        //生成图表，插入数据
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        CompletableFuture.runAsync(()->{
            //更新图表状态正在运行中
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                chartService.handleChartUpdateError(chart.getId(),"更新图表执行中失败");
                return;
            }


            //拿到返回结果
            String result = aiManager.doChat(biModelId, userInput.toString());
            String[] split = result.split("【【【【【");
            if (split.length < 3){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Ai 生成错误");
            }

            String genChart = split[1].trim();
            String genResult = split[2].trim();

            Chart resultBiAiChart = new Chart();
            resultBiAiChart.setId(chart.getId());
            resultBiAiChart.setGenChart(genChart);
            resultBiAiChart.setGenResult(genResult);
            resultBiAiChart.setStatus("success");
            boolean responseResult = chartService.updateById(resultBiAiChart);
            if (!responseResult) {
                chartService.handleChartUpdateError(chart.getId() ,"更新图表执行完成后状态失败");
            }
        },threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String chartType = genChartByAiRequest.getChartType();
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();

        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR,"名称过长");

        long size = multipartFile.getSize();
        //获取文件的原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件大小超过1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("xls","xlsx");
        //如果suffix的后缀不在范围内，抛出异常
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀非法 ");

        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRedisLimit("genChartByAi " + loginUser.getId());
        long biModelId = CommonConstant.BI_MODEL_ID;
        /**
         * 分析需求：
         * 网站用户增长情况
         * 原始数据：
         * 日期,用户数
         * 1号,10
         * 2号,20
         * 3号,30
         */

        //用户输入
//        StringBuilder userInput = new StringBuilder();
//        userInput.append("分析需求").append("\n");
//
//        String userGoal = goal;
//        if (StringUtils.isNotBlank(chartType)) {
//            userGoal += ",请使用" + chartType;
//        }
//
//        userInput.append(userGoal).append("\n");
//        userInput.append("原始数据：").append("\n");
//        //压缩后的数据，把multipart传过来
        String csvData =  ExcelUtils.excelToCsv(multipartFile);
//        userInput.append(csvData).append("\n");


        //生成图表，插入数据
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        biMessageProducer.sendMessage(String.valueOf(chart.getId()));


        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


}
