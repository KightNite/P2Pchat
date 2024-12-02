package com.kightnite.p2pchat.server;

import com.kightnite.p2pchat.model.ServerData;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Hashtable;

public class ServerListener {

    public static void main(String[] args) {
        int port;
        if (args.length < 1) {
            port = 9090;
        } else {
            port = Integer.parseInt(args[0]);
        }

//        List<DataSocket> dataPools = new ArrayList<>();
        Hashtable<SocketAddress, ServerData> addressTable = new Hashtable<>();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("CONNECTION ESTABLISHED");
                SocketAddress address = socket.getRemoteSocketAddress();
//                System.out.println(address);

                new ServerThread(socket, addressTable, address).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
