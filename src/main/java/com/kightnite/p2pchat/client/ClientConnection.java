package com.kightnite.p2pchat.client;

import com.kightnite.p2pchat.model.ChatMessage;
import com.kightnite.p2pchat.model.ClientData;
import javafx.application.Platform;
import com.kightnite.p2pchat.events.ChatListener;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ClientConnection extends Thread{
    public ServerSocket listenerServerSocket;
    public Hashtable<SocketAddress, ClientChatThread> connectedChats;
    private List<ChatListener> chatListeners;

    public ClientConnection(ServerSocket serverSocket) {
        listenerServerSocket = serverSocket;
        connectedChats = new Hashtable<>();
        chatListeners = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("Client Listener is listening on port " + listenerServerSocket.getLocalPort());

        try {
            while (true) {
                listenToConnection();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client Listener I/O error: " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        listenerServerSocket.close();

        for (ClientChatThread clientChat : connectedChats.values()) {
            clientChat.close();
        }
    }

    public boolean connectToPeer(SocketAddress socketAddress) {
        // TODO! Cleanup
        // CHECK FOR EXISTING PENDING CONNECTION TO THIS ADDRESS
        if(connectedChats.containsKey(socketAddress)){
            return false;
        }

        try {
            Socket socket = new Socket();

            // Connect to client
            socket.connect(socketAddress);

            // Send Request
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.writeObject(listenerServerSocket.getLocalSocketAddress());

            // Create Chat instance
            startChat(socket, socketAddress);

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void listenToConnection() throws IOException, ClassNotFoundException {
        // TODO! Cleanup

        // Start listening for connections
        Socket socket = listenerServerSocket.accept();
        System.out.println("New client connection request");

        ObjectInputStream objectReader = new ObjectInputStream(socket.getInputStream());

        // Receive Data
        SocketAddress address = (SocketAddress) objectReader.readObject();
        System.out.println(address);

        // CHECK FOR EXISTING PENDING CONNECTION AND CLIENTCHAT FROM THIS ADDRESS
        if(connectedChats.containsKey(address)){
            socket.close();
            return;
        }

        ClientChatThread chat = startChat(socket, address);
        chat.isPending = true;
        updatePendingConnections();
    }

    public void acceptRequest(ClientData data) {
        ClientChatThread chat = connectedChats.get(data.address);
        chat.sendData(new ChatMessage("Accepted", Instant.now(), ""));

        connectedChats.get(data.address).isPending = false;
        updatePendingConnections();

        //DEBUG
        System.out.println("Chat Request Accepted");
    }

    public void rejectRequest(SocketAddress address) {
        Socket socket = connectedChats.get(address).socket;
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            updatePendingConnections();
            System.out.println("Chat Request Rejected");
        }
    }

    public void updatePendingConnections() {
        Platform.runLater(() -> {
            chatListeners.forEach(ChatListener::onNewConnection);
        });
    }

    public void addPendingConnectionListener(ChatListener listener) {
        chatListeners.add(listener);
    }

    public Hashtable<SocketAddress, Socket> getPendingConnections() {
        return null;
    }

    private ClientChatThread startChat(Socket socket, SocketAddress address) throws IOException {
        try {
            ClientChatThread chat = new ClientChatThread(socket, address, chatListeners);
            chat.connectedChats = this.connectedChats;
            connectedChats.put(address, chat);
            chat.start();
            return chat;
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }
}
