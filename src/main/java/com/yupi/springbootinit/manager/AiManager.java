package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI对话
     *
     * @param message
     * @return
     */
    public String doChat(long modelId,String message){
        //构造请求参数
        DevChatRequest devChatRequest = new DevChatRequest();

//        //歌手推荐ModelId
//        devChatRequest.setModelId(1791918897707556865L);

//        图标分析ModelId
//        devChatRequest.setModelId(1791920823773896705L);
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);

        if (response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Ai响应错误！");
        }
        return response.getData().getContent();
    }
}
