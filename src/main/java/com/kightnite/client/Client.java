package main.java.com.kightnite.client;

import main.java.com.kightnite.model.ClientData;
import main.java.com.kightnite.model.ServerData;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter writer;
    private ObjectInputStream objectInput;
    private Scanner scanner;
    private String name;
    private ServerSocket inviteServerSocket;
    public ClientListener clientListener;


    public static void main(String[] args) {
        Client client = new Client();
        client.name = "Unknown";
        client.run();
        client.console();
    }

    public void startClient(String name) {
        this.name = name;
        run();
    }

    public void run() {
        this.scanner = new Scanner(System.in);
        String hostname = "localhost";
        int port = 9090;

        try {
            this.socket = new Socket(hostname, port);

            this.name += " " + socket.getLocalPort();

            String result = null;

            //OUTCOMING MESSAGE
            OutputStream output = socket.getOutputStream();
            ObjectOutputStream outputObject = new ObjectOutputStream(output);
            this.writer = new PrintWriter(output, true);

            //INCOMING MESSAGE
            InputStream input = socket.getInputStream();
            this.objectInput = new ObjectInputStream(input);

            connectToServer(outputObject);

            startClientListener();


        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't get dataPool table: "+ e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void console() {
        String result = null;

        try {
            while(!Objects.equals(result, "bye")){
                System.out.println("ENTER TEXT: ");
                result = scanner.nextLine();
                writer.println(result);

                List<ServerData> dataPool = (List<ServerData>) objectInput.readObject();
                for (ServerData serverData : dataPool) {
                    System.out.println("DATA: " + serverData.address + " " + serverData.data);
                }
            }
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());

        } catch (ClassNotFoundException ex) {
            System.out.println("Couldn't get dataPool table: "+ ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private void connectToServer(ObjectOutputStream outputObject) throws IOException, ClassNotFoundException {
        inviteServerSocket = new ServerSocket(0);
        SocketAddress inviteAddress = inviteServerSocket.getLocalSocketAddress();

        outputObject.writeObject(new ServerData(inviteAddress, name));

        List<ServerData> dataPool = (List<ServerData>) objectInput.readObject();
        for (ServerData serverData : dataPool) {
            System.out.println("DATA: " + serverData.address + " " + serverData.data);
        }
    }

    private void startClientListener() {
        this.clientListener = new ClientListener(inviteServerSocket);
        clientListener.start();
    }

    /// FOR DEBUG
    public void connectToPeerTest(SocketAddress socketAddress) {

        try (Socket socket = new Socket()) {
            // Connect to client
            socket.connect(socketAddress);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send Data
            writer.println("REQUEST TEXT");
            System.out.println("SENT REQUEST...");

            // Receive Data
            System.out.println(reader.readLine());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToPeer(SocketAddress socketAddress) {
        clientListener.connectToPeer(socketAddress);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public List<ClientData> getClientDataFromServer() throws IOException, ClassNotFoundException {
        List<ServerData> serverData;
        List<ClientData> clientData = new ArrayList<>();
        serverData = (List<ServerData>) objectInput.readObject();

        serverData.forEach(x -> clientData.add(new ClientData(x,
                clientListener.getPendingConnections().containsKey(x.address))));
        return clientData;
    }

    public List<ClientData> ping() {
        writer.println("Hi");

        List<ClientData> result = null;

        try {
            // Get sockets from server and remove own
            result = getClientDataFromServer();
            result.removeIf(x -> x.address.equals(inviteServerSocket.getLocalSocketAddress()));

            return result;
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            close();
            return null;

        } catch (ClassNotFoundException ex) {
            System.out.println("Couldn't get dataPool table: "+ ex.getMessage());
            close();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
