package cn.edu.sustech.cs209.chatting.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sustech.cs209.chatting.client.Chat;
import cn.edu.sustech.cs209.chatting.client.Controller;
import cn.edu.sustech.cs209.chatting.client.User;

public class Server {
    private int port;
    public ServerSocket serverSocket;
    public List<User> onlineUsers; //用户列表
    public List<String> names;

    public List<Chat> chatList;//私聊列表

    public Server(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.onlineUsers = new ArrayList<>();
        this.chatList = new ArrayList<>();
        this.names = new ArrayList<>();
    }

    public void waitUser() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("成功连接服务器");
                //服务器处理用户发来信息，一个线程对应一个客户
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    class ServerThread extends Thread {
        public String name;
        public Socket socket;
        public BufferedReader br;
        public PrintWriter pw;

        public ServerThread(Socket socket) {
            this.socket = socket;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pw = new PrintWriter(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String msg = "";
            try {
                while (true) {
                    msg = br.readLine();
                    System.out.println(msg);
                    handleMsg(msg);
                }
            } catch (SocketException se) {
                System.out.println(name + "该用户断开");
                //更新所有用户的onlineList
                onlineUsers = onlineUsers.stream()
                        .filter(user -> !user.username.equals(name)).toList();
                names = names.stream()
                        .filter(n -> !n.equals(name)).toList();
                handleMsg("delete");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public void handleMsg(String msg) {
            //处理各种请求
            try {
                if (msg.split("@")[0].equals("SINGLE_CHAT")) {
                    //私聊模式，传递信息给server，server直接传递给客户端B
                    String user = msg.split("@")[3];
                    System.out.println(msg);
                    for (User u : onlineUsers
                    ) {
                        if (u.username.equals(user)) {
                            u.pw.println(msg);
                            u.pw.flush();
                        }
                    }

                } else if (msg.split(" ")[0].equals("update")) {//加入一个新的用户
                    String name = msg.split(" ")[1];
                    this.name = name;
                    User user = new User(name, socket, br, pw);
                    onlineUsers.add(user);
                    names.add(name);
                    System.out.println(names);
                    StringBuilder sb = new StringBuilder();
                    for (String s : names
                    ) {
                        sb.append(s).append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    for (User u : onlineUsers
                    ) {
                        //所有的客户端更新user
                        u.pw.println("UPDATE!" + sb);
                        u.pw.flush();
                    }
                } else if (msg.equals("delete")) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : names
                    ) {
                        sb.append(s).append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    for (User u : onlineUsers
                    ) {
                        //所有的客户端更新user
                        u.pw.println("UPDATE!" + sb);
                        u.pw.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

