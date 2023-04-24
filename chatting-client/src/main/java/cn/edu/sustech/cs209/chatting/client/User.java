package cn.edu.sustech.cs209.chatting.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    public String username;
    public boolean isOnline = false; //判断用户是否在线
    public Socket socket;//server用来收发数据的socket

    public BufferedReader br;
    public PrintWriter pw;

    public User(String username,Socket socket) {
        this.username = username;
        this.socket = socket;
        try {
            this.br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            this.pw = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
