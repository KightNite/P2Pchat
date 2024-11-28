package main.java.com.kightnite.client;

import javafx.application.Platform;
import main.java.com.kightnite.events.ChatListener;
import main.java.com.kightnite.model.ChatMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ClientChatThread extends Thread{
    public Socket socket;
    public SocketAddress socketAddress;
    public BufferedReader reader;
    public PrintWriter writer;
    public Hashtable<SocketAddress, ClientChatThread> connectedChats;
    private final List<ChatListener> chatListeners;

    public List<ChatMessage> chatHistory = new ArrayList<>();

    public ClientChatThread(Socket socket, SocketAddress socketAddress, List<ChatListener> chatListeners) throws IOException {
        this.socket = socket;
        this.socketAddress = socketAddress;
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatListeners = chatListeners;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                chatHistory.add(new ChatMessage(message));
                System.out.println("ClientChat RECEIVED: " + message);

                // Notify UI about new message.
                updateChat();
            }
            System.out.println("ClientChat Stream Terminated");
        } catch (IOException e) {
            System.out.println("ClientPeerChat I/O error: " + e.getMessage());
        } finally {
            notifyClose();
            connectedChats.remove(socketAddress);
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public ChatMessage sendData(String data) {
        System.out.println("ClientChat SENT: " + data);
        ChatMessage message = new ChatMessage(data, true);
        chatHistory.add(message);
        writer.println(data);

        return message;
    }

    public void updateChat() {
        Platform.runLater(() -> {
            chatListeners.forEach(x -> x.onNewMessage(socketAddress));
        });
    }

    public void notifyClose() {
        Platform.runLater(() -> {
            chatListeners.forEach(x -> x.onConnectionClose(socketAddress));
        });
    }
}
