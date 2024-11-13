package main.java.com.kightnite.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientListener extends Thread{
    public ServerSocket listenerServerSocket;
    public List<ClientChat> connectedChats;

    public ClientListener(ServerSocket serverSocket) {
        listenerServerSocket = serverSocket;
        connectedChats = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("Client Listener is listening on port " + listenerServerSocket.getLocalPort());

        try {
            listenToConnection();

        } catch (IOException e) {
            System.out.println("Client Listener I/O error: " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    public void connectToPeer(SocketAddress socketAddress) {
        // TODO! Cleanup
        try {
            Socket socket = new Socket();

            // Connect to client
            socket.connect(socketAddress);
//            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Create Chat instance
            ClientChat chat = new ClientChat(socket);

            // Send Request
            chat.sendData("HELLO WORLD");

            // Receive Answer
            String text = reader.readLine();
            System.out.println("RECEIVED: " + text);

            if (Objects.equals(text, "accepted")) {
                connectedChats.add(chat);
                chat.start();
            } else {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenToConnection() throws IOException {
        // TODO! Cleanup
        while (true) {
            // Start listening for connections
            Socket socket = listenerServerSocket.accept();
            System.out.println("New client connection request");
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Receive Data
            String text = reader.readLine();
            System.out.println("Request: " + text);

            // TODO!!! Implement accepting/declining new connections
            // Accept or decline connection
            boolean accept = true;
            if (!accept) {
                socket.close();
                continue;
            }

            // Send Approval confirmation
            writer.println("accepted");

            // Start new Chat instance and add it to the list
            ClientChat chat = new ClientChat(socket, reader, writer);
            connectedChats.add(chat);

            chat.start();
        }
    }


    /// FOR DEBUG
    public void listenToConnectionTest() throws IOException {
        while (true) {
            // Start listening for connections
            Socket socket = listenerServerSocket.accept();
            System.out.println("New client connection request");
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Receive Data
            System.out.println("Start: " + reader.readLine());

            // Send Data
            writer.println("REPLY TEXT");

            System.out.println("End: " + reader.readLine());
            socket.close();
        }
    }
}
