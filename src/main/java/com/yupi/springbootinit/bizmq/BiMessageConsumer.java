package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = BiMqConstant.BI_QUEUE_NAME,ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receive message = " + message);

        if(StringUtils.isNotBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }

        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if( chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }

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
       String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, userInput(chart));
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
           channel.basicNack(deliveryTag,false,false);
           chartService.handleChartUpdateError(chart.getId() ,"更新图表执行完成后状态失败");
       }
        channel.basicAck(deliveryTag,false);
    }

    private String userInput(Chart chart){
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求").append("\n");

        String userGoal = chart.getGoal();
        if (StringUtils.isNotBlank(chart.getChartType())) {
            userGoal += ",请使用" + chart.getChartType();
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据，把multipart传过来
        String csvData = (chart.getChartData());
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }
}