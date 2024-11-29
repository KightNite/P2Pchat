package com.kightnite.p2pchat.model;

import java.net.SocketAddress;

public class ClientData extends ServerData {
    public boolean isPending;

    public ClientData(SocketAddress address) {
        super(address);
    }

    public ClientData(SocketAddress address, String data) {
        super(address, data);
    }

    public ClientData(ServerData serverData) {
        super(serverData.address, serverData.data);
    }

    public ClientData(ServerData serverData, boolean isPending) {
        super(serverData.address, serverData.data);
        this.isPending = isPending;
    }
}
