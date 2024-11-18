package main.java.com.kightnite.model;

import java.io.Serializable;
import java.net.SocketAddress;

public class DataSocket implements Serializable {

    public SocketAddress address;
    public String data;


    public DataSocket(SocketAddress address) {
        this.address = address;
    }

    public DataSocket(SocketAddress address, String data) {
        this.address = address;
        this.data = data;
    }


    @Override
    public String toString() {
        return data + " | " + address;
    }
}
