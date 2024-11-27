package main.java.com.kightnite.model;

import java.util.Date;

public class ChatMessage {
    public String message;
    public Date time;
    public boolean isSender;

    public ChatMessage(String message) {
        this.message = message;
        this.time = new Date();
    }

    public ChatMessage(String message, boolean isSender) {
        this(message);
        this.isSender = isSender;
    }
}
