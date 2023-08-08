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
