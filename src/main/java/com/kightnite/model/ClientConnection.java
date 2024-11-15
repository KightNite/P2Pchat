package main.java.com.kightnite.model;

import java.net.SocketAddress;

public class ClientConnection {
    public DataSocket dataSocket;
    public boolean pending;

    public ClientConnection(DataSocket dataSocket) {
        this.dataSocket = dataSocket;
    }

    @Override
    public String toString() {

        return pending ? "[!] " + dataSocket.toString() : dataSocket.toString();
    }
}
