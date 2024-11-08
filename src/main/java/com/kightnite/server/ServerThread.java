package main.java.com.kightnite.server;

import main.java.com.kightnite.model.DataSocket;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerThread extends Thread {
    private Socket socket;
    private Hashtable<SocketAddress, DataSocket> addressTable;
    private SocketAddress address;

    public ServerThread(Socket socket, Hashtable<SocketAddress, DataSocket> addressTable, SocketAddress address){
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
            DataSocket data = (DataSocket) inputObject.readObject();

            // Add to online connections
//            addressTable.add(address);
            addressTable.put(address, data);

            // Display connections
            for (DataSocket dataSocket : addressTable.values()) {
                System.out.println("DATA: " + dataSocket.address + " " + dataSocket.data);
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
