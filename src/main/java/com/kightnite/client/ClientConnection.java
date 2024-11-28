package main.java.com.kightnite.client;

import javafx.application.Platform;
import main.java.com.kightnite.events.ChatListener;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ClientConnection extends Thread{
    public ServerSocket listenerServerSocket;
    public Hashtable<SocketAddress, ClientChatThread> connectedChats;
    private Hashtable<SocketAddress, Socket> pendingSockets;
    private List<ChatListener> chatListeners;

    public ClientConnection(ServerSocket serverSocket) {
        listenerServerSocket = serverSocket;
        connectedChats = new Hashtable<>();
        pendingSockets = new Hashtable<>();
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
        if(pendingSockets.containsKey(socketAddress) || connectedChats.containsKey(socketAddress)){
            return false;
        }

        try {
            Socket socket = new Socket();

            // Connect to client
            socket.connect(socketAddress);
//            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Create Chat instance
            startChat(socket, socketAddress);

            // Send Request
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.writeObject(listenerServerSocket.getLocalSocketAddress());

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
//        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//        InputStream input = socket.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        ObjectInputStream objectReader = new ObjectInputStream(socket.getInputStream());

        // Receive Data
        SocketAddress address = (SocketAddress) objectReader.readObject();
        System.out.println(address);

        // CHECK FOR EXISTING PENDING CONNECTION AND CLIENTCHAT FROM THIS ADDRESS
        if(pendingSockets.containsKey(address) || connectedChats.containsKey(address)){
            socket.close();
            return;
        }

        pendingSockets.put(address, socket);
        updatePendingConnections();
    }

    public void acceptRequest(SocketAddress address) {
        Socket socket = pendingSockets.get(address);
        try {
            ClientChatThread chat = startChat(socket, address);
            chat.sendData("Accepted");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            pendingSockets.remove(address);
            updatePendingConnections();
            //DEBUG
            System.out.println("Chat Request Accepted");
        }
    }

    public void rejectRequest(SocketAddress address) {
        Socket socket = pendingSockets.get(address);
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            pendingSockets.remove(address);
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
        return pendingSockets;
    }

    private ClientChatThread startChat(Socket socket, SocketAddress address) throws IOException {
        ClientChatThread chat = new ClientChatThread(socket, address, chatListeners);
        chat.connectedChats = this.connectedChats;
        connectedChats.put(address, chat);
        chat.start();

        return chat;
    }
}
