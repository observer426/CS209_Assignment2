package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Chat {
    //聊天名称
    public String chatName;

    //私聊下指另外一个人
    public String creator;

    //聊天信息
    public ObservableList<Message> messages;

    public Chat(String chatName, String creator) {
        this.chatName = chatName;
        this.creator = creator;
        this.messages = FXCollections.observableArrayList();
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }
}
