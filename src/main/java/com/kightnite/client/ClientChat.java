package main.java.com.kightnite.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientChat extends Thread{
    public Socket socket;
    public BufferedReader reader;
    public PrintWriter writer;

    public ClientChat(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public ClientChat(Socket socket, BufferedReader reader, PrintWriter writer) throws IOException {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("ClientChat RECEIVED: " + reader.readLine());
            }
        } catch (IOException e) {
            System.out.println("ClientPeerChat I/O error: " + e.getMessage());
        }
    }

    public void sendData(String data) {
        System.out.println("ClientChat SENT: " + data);
        writer.println(data);
    }
}
