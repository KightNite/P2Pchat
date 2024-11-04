package main.java.com.kightnite.server;

import main.java.com.kightnite.model.DataSocket;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

public class ServerThread extends Thread {
    private Socket socket;
    private List<DataSocket> dataPool;
    private DataSocket address;

    public ServerThread(Socket socket, List<DataSocket> dataPool, SocketAddress address){
        this.socket = socket;
        this.dataPool = dataPool;
        this.address = new DataSocket(address);
    }

    public void run() {
        try{
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);

            String text;

            // Add Data to address
            text = reader.readLine();
            address.data = text;

            // Add to online connections
            dataPool.add(address);

            // Display connections
            for (DataSocket dataSocket : dataPool) {
                System.out.println("DATA: " + dataSocket.address + " " + dataSocket.data);
            }

            // Send list of connections
            objectOutput.writeObject(dataPool);

            // Server logic
            while (!text.equals("bye")) {
                text = reader.readLine();
                objectOutput.reset();
                objectOutput.writeObject(dataPool);
            }

            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
//            ex.printStackTrace();
        } finally {
            dataPool.remove(address);
        }
    }

}
