package com.kightnite.p2pchat.server;

import com.kightnite.p2pchat.model.ServerData;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerThread extends Thread {
    private Socket socket;
    private Hashtable<SocketAddress, ServerData> addressTable;
    private SocketAddress address;

    public ServerThread(Socket socket, Hashtable<SocketAddress, ServerData> addressTable, SocketAddress address){
        this.socket = socket;
        this.addressTable = addressTable;
        this.address = socket.getRemoteSocketAddress();
    }

    public void run() {
        try{
            InputStream input = socket.getInputStream();
            ObjectInputStream inputObject = new ObjectInputStream(input);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);

            // Get data (name for now) from client
            ServerData data = (ServerData) inputObject.readObject();

            // Add to online connections
//            addressTable.add(address);
            addressTable.put(address, data);

            // Display connections
            for (ServerData serverData : addressTable.values()) {
                System.out.println("DATA: " + serverData.address + " " + serverData.data);
            }

            // Send list of connections
            objectOutput.writeObject(new ArrayList<>(addressTable.values()));

            String text;

            // Server logic
            while (!data.equals("bye")) {
                text = reader.readLine();
                objectOutput.reset();
                objectOutput.writeObject(new ArrayList<>(addressTable.values()));
            }

            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
//            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);

        } finally {
            addressTable.remove(address);
        }
    }

}
