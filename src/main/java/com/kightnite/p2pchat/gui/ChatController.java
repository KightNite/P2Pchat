package com.kightnite.p2pchat.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import com.kightnite.p2pchat.client.Client;
import com.kightnite.p2pchat.model.ChatMessage;
import com.kightnite.p2pchat.model.ClientData;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

        System.out.println("Client Connections:");
        for (ClientData connection : connections) {
            System.out.println(connection + " " + connection.isPending);
        }

//        scrollPane.setContent(createConnectionGrid(connections));
        scrollPane.setContent(createConnectionVBox(connections));

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

    private VBox createConnectionVBox(List<ClientData> connections) {
        VBox vbox = new VBox(5);

        for(int i=0; i<connections.size(); i++) {
            SocketAddress address = connections.get(i).address;
            ClientData data = connections.get(i);

            vbox.getChildren().add(createConnectionRowHBox(address, data));
        }


        return vbox;
    }

    private HBox createConnectionRowHBox(SocketAddress address, ClientData data) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);

        if (client.clientConnection.connectedChats.containsKey(address)) {
            if (client.clientConnection.connectedChats.get(address).isPending) {
                Button buttonConnect = createButton("Accept", actionEvent -> onAcceptButtonClick(data));
                Button buttonReject = createButton("Reject", actionEvent -> onRejectButtonClick(address));

                hbox.getChildren().addAll(buttonConnect, buttonReject);
            }
            else if (client.clientConnection.connectedChats.get(address).chatHistory.isEmpty()) {
                Button buttonConnect = createButton("Pending", actionEvent -> onAcceptButtonClick(data));
                buttonConnect.setDisable(true);

                hbox.getChildren().add(buttonConnect);
            } else {
                Button buttonConnect = createButton("Chat", actionEvent -> onChatButtonClick(data));
                Button buttonClose = createButton("Close", actionEvent -> onCloseButtonClick(address));

                hbox.getChildren().addAll(buttonConnect, buttonClose);
            }
        } else {
            Button buttonConnect = createConnectButton("Connect", address);

            hbox.getChildren().add(buttonConnect);
        }


        Label label = new Label();
        label.setText(data.toString());
        label.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(label, Priority.ALWAYS);

        hbox.getChildren().add(label);

        // margins are up to your preference
//        GridPane.setMargin(buttonConnect, new Insets(5));
//        GridPane.setMargin(label, new Insets(5));


        return hbox;
    }

    private Button createButton(String text, EventHandler<ActionEvent> actionEvent) {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(actionEvent);

        return button;
    }

    private Button createConnectButton(String text, SocketAddress address) {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(actionEvent -> onConnectButtonClick(address, button));

        return button;
    }

    private GridPane createConnectionGrid(List<ClientData> connections) {

        GridPane gridConnections = new GridPane();

        //TODO!!! Clean up
        for(int i=0; i<connections.size(); i++){
            SocketAddress address = connections.get(i).address;
            ClientData data = connections.get(i);

            Button buttonConnect = new Button();
            if (client.clientConnection.connectedChats.containsKey(address)) {
                if (client.clientConnection.connectedChats.get(address).isPending) {
                    buttonConnect.setText("Accept");
                    buttonConnect.setOnAction(actionEvent -> onAcceptButtonClick(data));

                    Button buttonReject = new Button();
                    buttonReject.setText("Reject");
                    buttonReject.setOnAction(actionEvent -> onRejectButtonClick(address));

                    gridConnections.add(buttonReject, 1, i);
                }
                else if (client.clientConnection.connectedChats.get(address).chatHistory.isEmpty()) {
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