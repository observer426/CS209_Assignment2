package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Chat {
    //聊天名称
    public String chatName;
    //聊天信息
    public ObservableList<Message> messages;

    public Chat(String chatName) {
        this.chatName = chatName;
        this.messages = FXCollections.observableArrayList();
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }
}
