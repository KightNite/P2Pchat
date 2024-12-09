package com.kightnite.p2pchat.client;

import javafx.application.Platform;
import com.kightnite.p2pchat.events.ChatListener;
import com.kightnite.p2pchat.model.ChatMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ClientChatThread extends Thread{
    public boolean isPending;

    public Socket socket;
    public SocketAddress socketAddress;
    public ObjectInputStream objectReader;
    public ObjectOutputStream objectWriter;
    public Hashtable<SocketAddress, ClientChatThread> connectedChats;
    private final List<ChatListener> chatListeners;

    public List<ChatMessage> chatHistory = new ArrayList<>();

    public ClientChatThread(Socket socket,
                            SocketAddress socketAddress,
                            List<ChatListener> chatListeners) throws IOException {
        this.socket = socket;
        this.socketAddress = socketAddress;
        this.chatListeners = chatListeners;
        objectWriter = new ObjectOutputStream(socket.getOutputStream());
        objectReader = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            readData();
            System.out.println("ClientChat Stream Terminated");
        } catch (IOException e) {
            System.out.println("ClientChat reading I/O error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally {
            notifyClose();
            connectedChats.remove(socketAddress);
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public void readData() throws IOException, ClassNotFoundException {
        while (true) {
            ChatMessage message = (ChatMessage) objectReader.readObject();
            if (message == null) {
                break;
            }

            chatHistory.add(message);
            System.out.println("ClientChat RECEIVED: " + message);

            // Notify UI about new message.
            updateChat();
        }
    }

    public ChatMessage sendData(ChatMessage message) {
        try {
            System.out.println("ClientChat SENT: " + message);
            chatHistory.add(message);
            objectWriter.writeObject(message);
        } catch (IOException e) {
            System.out.println("ClientChat writing I/O error: " + e.getMessage());
        }

        return message;
    }

    public void updateChat() {
        Platform.runLater(() -> chatListeners.forEach(x -> x.onNewMessage(socketAddress)));
    }

    public void notifyClose() {
        Platform.runLater(() -> chatListeners.forEach(x -> x.onConnectionClose(socketAddress)));
    }
}
