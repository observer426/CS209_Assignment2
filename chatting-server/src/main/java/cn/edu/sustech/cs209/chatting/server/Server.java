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

import cn.edu.sustech.cs209.chatting.client.User;

public class Server {
    private int port;
    public ServerSocket serverSocket;
    public List<User> users; //用户列表

    public Server(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.users = new ArrayList<>();
    }

    public void waitUser() throws IOException {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("成功连接服务器");
                //服务器处理用户发来信息，一个线程对应一个客户
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }catch (IOException e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }
    class ServerThread extends Thread {
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
            String msg="";
            try {
                //默认msg最后一位为用户
                while(true){
                    msg = br.readLine();
                    System.out.println(msg);
                    handleMsg(msg);
                }
            } catch (SocketException se) {
                System.out.println("该用户断开");
                for (User u: users
                     ) {
                    if (msg.split(" ")[1].equals(u.username)){
                        u.isOnline = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void handleMsg(String msg) {
            //处理各种请求
            try {
                if (msg.split(" ")[0].equals("GET_ONLINE_USER")) {
                    StringBuilder sb = new StringBuilder();
                    //判断是否为新的用户，如果是就new一个，如果不是就重新设置用户的socket,br,pw,都要新开一个线程
                    //同时返回在线user的name用于客户端显示，返回对于是否要重新登录的判断
                    int test = 0; //0:创建新用户  1：在线用户  2：未在线老用户
                    String name = msg.split(" ")[1];
                    for (User u : users
                    ) {
                        if (u.username.equals(name) && u.isOnline) {//存在相同的用户而且在线
                            test = 1;
                            break;
                        } else if (u.username.equals(name)) {//相同用户但是不在线,设置为在线
                            u.socket = socket;
                            u.br = br;
                            u.pw = pw;
                            u.isOnline = true;
                            test = 2;
                            break;
                        }
                    }
                    if(test == 0){
                        User user = new User(name,socket);
                        user.isOnline = true;
                        users.add(user);
                    }
                    for (User u: users
                         ) {
                        if (u.isOnline) {//当前在线用户（一定包括自己）
                            sb.append(u.username).append(",");
                        }
                    }
                    sb.append("#").append(test);
                    pw.println(sb);
                    pw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

