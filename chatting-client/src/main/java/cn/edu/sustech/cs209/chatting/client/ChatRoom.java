package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom extends Chat {

    public List<String> total;
    public ObservableList<Message> messages;

    public ChatRoom(String creator, List<String> member) {
        super(creator, member.get(0));
        this.total = new ArrayList<>();
        total.addAll(member);
        this.messages = FXCollections.observableArrayList();
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }
}
