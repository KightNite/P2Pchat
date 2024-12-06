package com.kightnite.p2pchat.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record ChatMessage(String message, Instant time, String id) implements Serializable {
    @Override
    public String toString() {
        String sender = !Objects.equals(id, "") ? "[" + id + "]: " : "";
        return "[" + time.toString() + "]" + sender + message;
    }
}
