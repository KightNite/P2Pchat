package com.kightnite.p2pchat.model;

import java.io.Serializable;
import java.net.SocketAddress;

public class ServerData implements Serializable {

    public SocketAddress address;
    public String data;


    public ServerData(SocketAddress address) {
        this.address = address;
    }

    public ServerData(SocketAddress address, String data) {
        this.address = address;
        this.data = data;
    }


    @Override
    public String toString() {
        return data + " | " + address;
    }
}
