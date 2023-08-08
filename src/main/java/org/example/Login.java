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
