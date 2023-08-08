package org.example;

import cn.gjsm.api.openai.OpenAiClient;
import cn.gjsm.api.openai.OpenAiClientFactory;
import cn.gjsm.api.pojo.chat.ChatCompletionRequest;
import cn.gjsm.api.pojo.chat.ChatCompletionResponse;
import cn.gjsm.api.pojo.chat.ChatMessage;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import retrofit2.Call;
import retrofit2.Response;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class chatGpt {
    private OpenAiClient openAiClient;
    private List<ChatMessage> list;
    private String ans;
    public chatGpt(List<ChatMessage> list) throws IOException {
        openAiClient = OpenAiClientFactory.createClient(getToken());
        this.list = list;
    }

    public String userSend() throws IOException {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(list)
                .model("gpt-3.5-turbo")
                .build();

        // 执行请求 Execute request
        Call<ChatCompletionResponse> chatCompletion = openAiClient.callChatCompletion(request);

        try{
            Response<ChatCompletionResponse> response = chatCompletion.execute();
            if (response.isSuccessful()){
                String s = JSON.toJSONString(response.body());
                // 创建ObjectMapper对象
                ObjectMapper objectMapper = new ObjectMapper();

                // 将JSON字符串转化为JsonNode对象
                JsonNode rootNode = objectMapper.readTree(s);

                // 从JsonNode对象中提取"content"字段的值
                String content = rootNode.get("choices").get(0).get("message").get("content").asText();
                ans = content;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return ans;

    }

    public String getToken() throws IOException {
        Properties props = new Properties();
        InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties");
        props.load(input);
        return props.getProperty("token");
    }

}
