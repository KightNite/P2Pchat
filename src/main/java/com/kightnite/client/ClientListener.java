package main.java.com.kightnite.client;

import javafx.application.Platform;
import main.java.com.kightnite.events.PendingConnectionListener;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class ClientListener extends Thread{
    public ServerSocket listenerServerSocket;
    public List<ClientChat> connectedChats;
    private Hashtable<SocketAddress, Socket> pendingSockets;
    private List<PendingConnectionListener> pendingConnectionListeners;

    public ClientListener(ServerSocket serverSocket) {
        listenerServerSocket = serverSocket;
        connectedChats = new ArrayList<>();
        pendingSockets = new Hashtable<>();
        pendingConnectionListeners = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("Client Listener is listening on port " + listenerServerSocket.getLocalPort());

        try {
            listenToConnection();

        } catch (IOException | ClassNotFoundException e) {
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
            SocketAddress address = listenerServerSocket.getLocalSocketAddress();


            chat.sendObjectData(address);

            // Receive Answer
            String text = reader.readLine();
            System.out.println("RECEIVED: " + text);

            if (Objects.equals(text, "accepted")) {
                //TODO!
//                connectedChats.add(chat);
//                chat.start();
                socket.close();
            } else {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenToConnection() throws IOException, ClassNotFoundException {
        // TODO! Cleanup
        while (true) {
            // Start listening for connections
            Socket socket = listenerServerSocket.accept();
            System.out.println("New client connection request");
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            ObjectInputStream objectReader = new ObjectInputStream(socket.getInputStream());

            // Receive Data
            SocketAddress address = (SocketAddress) objectReader.readObject();
            System.out.println(address);

            updatePendingConnections(address, socket);

            // TODO!!! Implement accepting/declining new connections
            // Accept or decline connection
            boolean accept = true;
            if (!accept) {
                socket.close();
                continue;
            }

            // Send Approval confirmation
            writer.println("accepted");

            //TODO!!! Create Chat instance
            // Start new Chat instance and add it to the list
//            ClientChat chat = new ClientChat(socket, reader, writer);
//            connectedChats.add(chat);

//            chat.start();
        }
    }

    public void resolveRequest() {
        // TODO!
    }

    public void updatePendingConnections(SocketAddress address, Socket socket) {
        pendingSockets.put(address, socket);
        Platform.runLater(() -> {
            pendingConnectionListeners.forEach(PendingConnectionListener::onNewConnection);
        });
    }

    public void addPendingConnectionListener(PendingConnectionListener listener) {
        pendingConnectionListeners.add(listener);
    }

    public Hashtable<SocketAddress, Socket> getPendingConnections() {
        return pendingSockets;
    }

    private SocketAddress resolveAddress(String address) {

        String ipAddress = address.substring(0, address.indexOf(':'));
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));

        return new InetSocketAddress(ipAddress, port);

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
