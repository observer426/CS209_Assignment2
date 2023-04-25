package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    public String host = "localhost";
    public int port = 8080;
    public Socket socket;//链接服务器的socket

    public BufferedReader br;
    public PrintWriter pw;
    public List<String> onlineList;//在线用户
    @FXML
    ListView<Message> chatContentList;

    @FXML
    public ListView<String> chatList;

    public Chat currentChat;//当前聊天

    public List<Chat> myChat;
//    public Map<String, SplitPane> chatWindows;

    @FXML
    public TextArea inputArea;
    @FXML
    private Label currentUsername;
    @FXML
    private Label currentOnlineCnt;
    String username;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");
        Optional<String> input = dialog.showAndWait();
        inputArea.setWrapText(true);
        myChat = new ArrayList<>();
        try {
            socket = new Socket(host, port);
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());
            if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
                pw.println("GET_ONLINE_USER " +input.get());//指令+当前用户
                pw.flush();
                String resp = br.readLine();
                onlineList = Arrays.asList(resp.split(",#")[0].split(","));
                //client端存储online列表
                // 用于显示,最后一位是true，重复；否则false
                if(resp.split(",#")[1].equals("1")){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("User " + input.get() + " is already logged in. Please choose a different username.");
                    alert.showAndWait();
                    // 递归调用initialize()方法以重新显示登录对话框
                    initialize(url, resourceBundle);
                }else {
                    username = input.get();
                    Platform.runLater(()->{
                        currentUsername.textProperty().bind(Bindings.concat("Current User: ").concat(username));
                        currentOnlineCnt.textProperty().bind(Bindings.concat("Online: ").concat(onlineList.size()));
                    });
                }
            } else {
                System.out.println("Invalid username " + input + ", exiting");
                Platform.exit();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        chatList.setOnMouseClicked(mouseEvent -> {
            String name = chatList.getSelectionModel().getSelectedItem();
            for (Chat c: myChat
                 ) {
                if(c.chatName.equals(name)){
                    currentChat = c;
                    break;
                }
            }
            chatContentList.getItems().clear();//清除不是当前对话的msg
            chatContentList.getItems().addAll(currentChat.getMessages());//加入之前对话的msg
        });

        chatContentList.setCellFactory(new MessageCellFactory());
    }

    @FXML
    public void createPrivateChat() {
        //私聊的user名称
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        pw.println("GET_ONLINE_USER " + username);//指令+当前用户
        pw.flush();
        String resp = "";
        try {
            resp = br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        onlineList = Arrays.stream(resp.split(",#")[0].split(",")).toList();
        //再次更新online
        currentOnlineCnt.textProperty().bind(Bindings.concat("Online: ").concat(onlineList.size()));
        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll(onlineList.stream()
                .filter(s -> !s.equals(username))
                .toList());

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            ObservableList<String> items = chatList.getItems();
            System.out.println(items);
            //如果当前用户没有和所选用户私聊过，直接在当前对话框显示，只不过是message的更改
            boolean exist = false;
            for ( Chat c: myChat
                 ) {
                if (c.chatName.equals(user.toString())){//之前聊过
                    chatList.getSelectionModel().getSelectedItem();
                    chatContentList.getItems().clear();//清除不是当前对话的msg
                    chatContentList.getItems().addAll(c.getMessages());//加入之前对话的msg
                    exist = true;
                    currentChat = c;
                    break;
                }
            }
            if(!exist){
                Chat c = new Chat(user.toString());
                myChat.add(c);
                currentChat = c;
                items.add(user.toString());
            }
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user

        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
        System.out.println("send btn");
        String text = inputArea.getText();
        System.out.println(currentChat);
        if (text != null && !text.trim().isEmpty()){
            //发送message,接收方判断是否存在该聊天，没有则创建一个，有就跳转到，同时加入消息提示。

            Message message = new Message(System.currentTimeMillis(),
                    username, currentChat.chatName, text);
            currentChat.messages.add(message);
            chatContentList.getItems().clear();//清除不是当前对话的msg
            chatContentList.getItems().addAll(currentChat.getMessages());
            inputArea.clear();
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty message");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a non-empty message before sending.");
            alert.showAndWait();
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
