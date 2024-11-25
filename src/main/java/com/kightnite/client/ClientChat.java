package main.java.com.kightnite.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Hashtable;

public class ClientChat extends Thread{
    //TODO!!! Close thread if socket closes (connection refused or chat closed)
    public Socket socket;
    public SocketAddress socketAddress;
    public BufferedReader reader;
    public PrintWriter writer;
    public ObjectOutputStream objectOutput;
    public Hashtable<SocketAddress, ClientChat> connectedChats;

    public ClientChat(Socket socket, SocketAddress socketAddress) throws IOException {
        this.socket = socket;
        this.socketAddress = socketAddress;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        objectOutput = new ObjectOutputStream(socket.getOutputStream());
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
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                System.out.println("ClientChat RECEIVED: " + message);
            }
            System.out.println("ClientChat Stream Terminated");
        } catch (IOException e) {
            System.out.println("ClientPeerChat I/O error: " + e.getMessage());
        } finally {
            System.out.println("----------------------------");
            for (SocketAddress address : connectedChats.keySet()) {
                System.out.println(address);
            }
            connectedChats.remove(socketAddress);
            System.out.println("----------------------------");
            for (SocketAddress address : connectedChats.keySet()) {
                System.out.println(address);
            }
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public void sendData(String data) {
        System.out.println("ClientChat SENT: " + data);
        writer.println(data);
    }

    public void sendObjectData(Object data) throws IOException {
        objectOutput.writeObject(data);
    }
}
