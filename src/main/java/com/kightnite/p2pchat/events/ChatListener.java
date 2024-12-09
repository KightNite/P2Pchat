package com.kightnite.p2pchat.events;

import java.net.SocketAddress;

public interface ChatListener {
    void onNewConnection();

    void onNewMessage(SocketAddress senderAddress);

    void onConnectionClose(SocketAddress senderAddress);
}
