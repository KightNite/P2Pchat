package main.java.com.kightnite.events;

import java.net.SocketAddress;

public interface ChatListener {
    public void onNewConnection();

    public void onNewMessage(SocketAddress senderAddress);

    public void onConnectionClose(SocketAddress senderAddress);
}
