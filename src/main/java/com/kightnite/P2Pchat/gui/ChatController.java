package com.kightnite.p2pchat.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import com.kightnite.p2pchat.client.Client;
import com.kightnite.p2pchat.model.ChatMessage;
import com.kightnite.p2pchat.model.ClientData;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;

public class ChatController {

    @FXML
    public ScrollPane scrollPane;
    @FXML
    private Label welcomeText;

    @FXML
    public Label chatLabel;
    @FXML
    public TextArea chatHistory;
    @FXML
    public TextArea chatText;
    @FXML
    public Button chatSend;



    Client client;

    ClientData currentChat = null;

    @FXML
    protected void onRerfreshButton() {

        List<ClientData> connections = client.ping();

        for (ClientData connection : connections) {
            System.out.println(connection);
        }

        scrollPane.setContent(createConnectionGrid(connections));

        welcomeText.setText("Refreshing...");

    }

    @FXML
    protected void onConnectButtonClick(SocketAddress address, Button button) {
        if (!client.connectToPeer(address)) {
            onRerfreshButton();
            return;
        }
        button.setDisable(true);
        button.setText("Pending");
    }

    @FXML
    protected void onAcceptButtonClick(ClientData data) {
        client.clientConnection.acceptRequest(data.address);
        onChatButtonClick(data);
    }

    @FXML
    protected void onRejectButtonClick(SocketAddress address) {
        client.clientConnection.rejectRequest(address);
    }

    @FXML
    protected void onChatButtonClick(ClientData data) {
        //TODO!!!
        currentChat = data;

        // Set name of user you chat with
        chatLabel.setText("Chat with " + data.data);

        // Get chat history and display it
        List<ChatMessage> chatBox = getChatHistory(data.address);

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < chatBox.size(); i++) {
            String sender = chatBox.get(i).isSender ? "You" : data.data;
            text    .append("[")
                    .append(chatBox.get(i).time.toString())
                    .append("] ")
                    .append(sender)
                    .append(": ")
                    .append(chatBox.get(i).message)
                    .append("\n");
        }

        chatSend.setDisable(false);
        chatHistory.setText(text.toString());
    }

    @FXML
    protected void onCloseButtonClick(SocketAddress address) {
        try {
            client.clientConnection.connectedChats.get(address).close();
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    @FXML
    public void onSendButton() {
        if (chatText.getText().isEmpty()) { return; }
        ChatMessage message = client.clientConnection.connectedChats.get(currentChat.address).sendData(chatText.getText());

        StringBuilder text = new StringBuilder();
            text    .append("[")
                    .append(message.time.toString())
                    .append("] ")
                    .append("You")
                    .append(": ")
                    .append(message.message)
                    .append("\n");

        chatHistory.setText(chatHistory.getText() + text);
        chatText.setText("");
    }

    private GridPane createConnectionGrid(List<ClientData> connections) {

        GridPane gridConnections = new GridPane();

        //TODO!!! Clean up
        for(int i=0; i<connections.size(); i++){
            SocketAddress address = connections.get(i).address;
            ClientData data = connections.get(i);

            Button buttonConnect = new Button();
            if (client.clientConnection.connectedChats.containsKey(address)) {
                if (client.clientConnection.connectedChats.get(address).chatHistory.isEmpty()) {
                    buttonConnect.setText("Pending");
                    buttonConnect.setDisable(true);
                } else {
                    buttonConnect.setText("Chat");
                    buttonConnect.setOnAction(actionEvent -> onChatButtonClick(data));

                    Button buttonClose = new Button();
                    buttonClose.setText("Close");
                    buttonClose.setOnAction(actionEvent -> onCloseButtonClick(address));
                    gridConnections.add(buttonClose, 2, i);
                }
            } else {
                buttonConnect.setText("Connect");
                buttonConnect.setOnAction(actionEvent -> onConnectButtonClick(address, buttonConnect));
            }


            Label label = new Label();
            label.setText(connections.get(i).toString());


            //add them to the GridPane
            gridConnections.add(buttonConnect, 0, i); //  (child, columnIndex, rowIndex)
            gridConnections.add(label , 3, i);

            if (connections.get(i).isPending) {
                Button buttonAccept = new Button();
                buttonAccept.setText("Accept");
                buttonAccept.setOnAction(actionEvent -> onAcceptButtonClick(data));

                Button buttonReject = new Button();
                buttonReject.setText("Reject");
                buttonReject.setOnAction(actionEvent -> onRejectButtonClick(address));

                gridConnections.add(buttonAccept, 1, i);
                gridConnections.add(buttonReject, 2, i);
            }

            // margins are up to your preference
            GridPane.setMargin(buttonConnect, new Insets(5));
            GridPane.setMargin(label, new Insets(5));
        }

        return gridConnections;
    }

    protected void setClient(Client client) {
        this.client = client;
    }

    protected List<ChatMessage> getChatHistory(SocketAddress address) {

        return client.clientConnection.connectedChats.get(address).chatHistory;
    }

    protected void onPendingConnection() {
        onRerfreshButton();
    }

    protected void onNewMessage(SocketAddress senderAddress) {
        if (currentChat != null && Objects.equals(currentChat.address, senderAddress)) {
            onChatButtonClick(currentChat);
        }
        else {
            onRerfreshButton();
        }
    }

    protected void onConnectionClose(SocketAddress senderAddress) {
        if (currentChat != null && Objects.equals(currentChat.address, senderAddress)) {
            endChatWindow();
        }
        onRerfreshButton();
    }

    private void endChatWindow() {
        currentChat = null;
        chatHistory.setText(chatHistory.getText() + "CONNECTION CLOSED");
        chatSend.setDisable(true);
    }
}