# QQBOT
This is a QQBot with ChatGPT

# 使用条件

1.拥有OPAI账号，并有API-keys
2.网络加速器（由于国内无法使用chatgpt）

当你满足以上两个条件时，chatgpt机器人便可以成功实现\^o\^!

## 一分钟了解该项目

 - 使用语言**java**
 - 使用框架**mirai**、**openai**、**fastjson**
 - 项目结构：

 	![在这里插入图片描述](https://img-blog.csdnimg.cn/7496a445423246aea1f5cc336de29de7.png)


## 项目开始

一、使用idea搭建maven工程（自己搭建）
二、导入maven依赖

```java
		<dependency>
            <groupId>net.mamoe</groupId>
            <artifactId>mirai-core-jvm</artifactId>
            <version>2.14.0</version>
        </dependency>
		<dependency>
            <groupId>cn.gjsm</groupId>
            <artifactId>openai</artifactId>
            <version>0.1.3</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>2.0.26</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.14.2</version>
        </dependency>
    </dependencies>
```

三、在resource文件下新建application.properties文件
![在这里插入图片描述](https://img-blog.csdnimg.cn/ecfeb0fdfbce4901b3a1b82a9df3b1d6.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2235bddf8a99439c8296b408c5dd72b2.png)
四、编写QQBot机器人类
该机器人类主要实现qq的登录，在此基础上加上消息监听器就可以成功获取好友or群消息啦！

```java
package org.example;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;

public class QQBot {

    private Bot qqBot;
    private long qq;
    private String password;

    public QQBot(long qq, String password) {
        this.qqBot = BotFactory.INSTANCE.newBot(qq,password,new BotConfiguration(){{
            setProtocol(MiraiProtocol.MACOS);
        }});
        this.qq = qq;
        this.password = password;
    }
    public void login(){
        qqBot.getEventChannel().registerListenerHost(new MessageHandler());
        qqBot.login();
    }
}

```

五、编写chatgpt类
该类主要是接入chatgpt，实现与chatgpt对话。
对话上分两种角色user（使用者）和assistant（chatgpt），
对话的内容使用ChatMessage类封装。

```java
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

//从application.properties里取token
    public String getToken() throws IOException {
        Properties props = new Properties();
        InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties");
        props.load(input);
        return props.getProperty("token");
    }
}
```

六、编写消息监听器

```java
package org.example;

import cn.gjsm.api.pojo.chat.ChatMessage;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler implements ListenerHost {
    /**
     * 私聊
     */
    private Map<Long,List<ChatMessage>> map = new HashMap<>();
    /**
     * 群聊
     */
    private Map<String,List<ChatMessage>> groupMap = new HashMap<>();

    /**
     * 好友消息事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onFriendMessageEvent(FriendMessageEvent event) throws IOException {
        System.out.println(event.getSubject().getId());
        if ((event instanceof FriendMessageEvent)&&(event.getSubject().getId()!=event.getBot().getId())) {
            List<ChatMessage> list = new ArrayList<>();
            if (map.containsKey(event.getFriend().getId())){
                list = map.get(event.getFriend().getId());
            }

            // 获取发送的消息内容
            String message = event.getMessage().contentToString();

            ChatMessage question = new ChatMessage();
            question.setRole("user");
            question.setContent(message);
            //添加ChatMessage
            list.add(question);
            //向ChatGpt提问
            chatGpt gpt = new chatGpt(list);
            //ChatGpt的答复
            String gptAns = gpt.userSend();


            if (gptAns!=null&&gptAns!=""){
                ChatMessage gptMsg = new ChatMessage();
                gptMsg.setRole("assistant");
                gptMsg.setContent(gptAns);
                list.add(gptMsg);
                event.getFriend().sendMessage(gptAns);
            }else {
                event.getFriend().sendMessage("出现未知bug，请联系管理员！");
            }

            map.put(event.getFriend().getId(),list);

        }
    }

    /**
     * 群聊消息事件
     *
     * @param event 事件
     */

    @EventHandler
    public void onGroupMessageEvent(GroupMessageEvent event) throws IOException {
        // 判断消息是否是针对机器人的
        long botQq = event.getBot().getId();
        String msg = event.getMessage().contentToString();
        String arr[] = msg.split(" ");

        if (msg.contains("@"+botQq)){
            List<ChatMessage> list = new ArrayList<>();
            String name = event.getSenderName();
            if (groupMap.containsKey(name)){
                list=groupMap.get(name);
            }

            ChatMessage question = new ChatMessage();
            question.setRole("user");
            question.setContent(arr[1]);

            //添加ChatMessage
            list.add(question);
            //向ChatGpt提问
            chatGpt gpt = new chatGpt(list);
            //ChatGpt的答复
            String gptAns = gpt.userSend();

            if (gptAns!=null&&gptAns!=""){
                ChatMessage gptMsg = new ChatMessage();
                gptMsg.setRole("assistant");
                gptMsg.setContent(gptAns);
                list.add(gptMsg);
                event.getGroup().sendMessage("@"+name+" "+gptAns);
            }else {
                event.getGroup().sendMessage("@"+name+"出现未知bug，请联系管理员！");
            }

            groupMap.put(name,list);
        }

    }

    /**
     * 好友申请事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onNewFriendRequestEvent(NewFriendRequestEvent event){
        //自动接受邀请
        event.accept();
    }

    /**
     * 群聊邀请事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onNewGroupRequestEvent(BotInvitedJoinGroupRequestEvent event){
        //自动接受邀请
        event.accept();
    }

}
```

六、万事俱备，只欠登录
Login类：

```java
package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Login {
    private Long QQ;
    private String PWD;

    public Login() throws IOException {
        Properties props = new Properties();
        InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties");
        props.load(input);
        this.QQ = Long.parseLong(props.getProperty("QQ"));
        this.PWD = props.getProperty("PWD");
    }
    public void login(){
        QQBot qqBot = new QQBot(this.QQ,this.PWD);
        qqBot.login();
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/66ef1e68d22347c7a6fa51db4cbfeeb5.png)



## 第一次写文章，如有不满请见凉

 效果展示：
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/85a726b03c074ee2b2594bf081f20222.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/94cf2b6aefa44626b503e06da2db2347.png)

## 后续
简实现了对于qq群的聊天功能，代码里有

## 提示
此项目是闲暇之余的作品，程序以及功能还不尽完善，以后有时间再做升级！！！🙂


