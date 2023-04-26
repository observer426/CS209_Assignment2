package cn.edu.sustech.cs209.chatting.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
  public String username;
  public Socket socket;//server用来收发数据的socket
   public BufferedReader br;
  public PrintWriter pw;


  public User(String username,Socket socket,BufferedReader br, PrintWriter pw) {
    this.username = username;
    this.socket = socket;
    this.br = br;
    this.pw = pw;
  }
}
