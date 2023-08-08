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
            System.out.println(gptAns);

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

