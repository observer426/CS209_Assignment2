package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Server server = new Server(8080);
        System.out.println("Starting server");
        try {
            server.waitUser();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
