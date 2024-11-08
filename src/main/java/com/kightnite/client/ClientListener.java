package main.java.com.kightnite.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread{
    public ServerSocket listenerServerSocket;

    public ClientListener(ServerSocket serverSocket) {
        listenerServerSocket = serverSocket;
    }

    @Override
    public void run() {
        System.out.println("Client Listener is listening on port " + listenerServerSocket.getLocalPort());

        try {
            while (true) {
                // Start listening for connections
                Socket socket = listenerServerSocket.accept();
                System.out.println("New client connection request");
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Receive Data
                System.out.println(reader.readLine());

                // Send Data
                writer.println("REPLY TEXT");
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("Client Listener I/O error: " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }
}
