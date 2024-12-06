package com.kightnite.p2pchat.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.kightnite.p2pchat.client.Client;
import com.kightnite.p2pchat.model.ChatMessage;
import com.kightnite.p2pchat.model.ClientData;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Instant;
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
    protected void onRefreshButton() {

        List<ClientData> connections = client.ping();

        System.out.println("Client Connections:");
        for (ClientData connection : connections) {
            System.out.println(connection + " " + connection.isPending);
        }

//        scrollPane.setContent(createConnectionGrid(connections));
        scrollPane.setContent(createConnectionVBox(connections));

//        welcomeText.setText("Refreshing...");

    }

    @FXML
    protected void onConnectButtonClick(SocketAddress address, Button button) {
        if (!client.connectToPeer(address)) {
            onRefreshButton();
            return;
        }
        button.setDisable(true);
        button.setText("Pending");
    }

    @FXML
    protected void onAcceptButtonClick(ClientData data) {
        client.clientConnection.acceptRequest(data);
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
            text.append(chatBox.get(i).toString()).append("\n");
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
        ChatMessage message = new ChatMessage(chatText.getText(), Instant.now(), client.name);
        client.clientConnection.connectedChats.get(currentChat.address).sendData(message);

        chatHistory.setText(chatHistory.getText() + message + "\n");
        chatText.setText("");
    }

    private VBox createConnectionVBox(List<ClientData> connections) {
        VBox vbox = new VBox(5);

        for(int i=0; i<connections.size(); i++) {
            ClientData data = connections.get(i);

            HBox hbox = createConnectionRowHBox(data);
            vbox.getChildren().add(hbox);
        }


        return vbox;
    }

    private HBox createConnectionRowHBox(ClientData data) {
        HBox hbox = new HBox(5);
        hbox.getStyleClass().add("connection");
        hbox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hbox, Priority.ALWAYS);

        Tooltip tooltip = new Tooltip(data.address.toString());
        tooltip.setShowDelay(Duration.seconds(0.5));
        Tooltip.install(hbox, tooltip);

        if (client.clientConnection.connectedChats.containsKey(data.address)) {
            if (client.clientConnection.connectedChats.get(data.address).isPending) {
                Button buttonConnect = createButton("Accept", actionEvent -> onAcceptButtonClick(data));
                Button buttonReject = createButton("Reject", actionEvent -> onRejectButtonClick(data.address));

                hbox.getChildren().addAll(buttonConnect, buttonReject);
            }
            else if (client.clientConnection.connectedChats.get(data.address).chatHistory.isEmpty()) {
                Button buttonConnect = createButton("Pending", actionEvent -> onAcceptButtonClick(data));
                buttonConnect.setDisable(true);

                hbox.getChildren().add(buttonConnect);
            } else {
                Button buttonConnect = createButton("Chat", actionEvent -> onChatButtonClick(data));
                Button buttonClose = createButton("Close", actionEvent -> onCloseButtonClick(data.address));

                hbox.getChildren().addAll(buttonConnect, buttonClose);
            }
        } else {
            Button buttonConnect = createConnectButton("Connect", data.address);

            hbox.getChildren().add(buttonConnect);
        }


        Label label = new Label();
        label.setText(data.data);
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
        this.welcomeText.setText(client.name);
    }

    protected List<ChatMessage> getChatHistory(SocketAddress address) {

        return client.clientConnection.connectedChats.get(address).chatHistory;
    }

    protected void onPendingConnection() {
        onRefreshButton();
    }

    protected void onNewMessage(SocketAddress senderAddress) {
        if (currentChat != null && Objects.equals(currentChat.address, senderAddress)) {
            onChatButtonClick(currentChat);
        }
        else {
            onRefreshButton();
        }
    }

    protected void onConnectionClose(SocketAddress senderAddress) {
        if (currentChat != null && Objects.equals(currentChat.address, senderAddress)) {
            endChatWindow();
        }
        onRefreshButton();
    }

    private void endChatWindow() {
        currentChat = null;
        chatHistory.setText(chatHistory.getText() + "CONNECTION CLOSED");
        chatSend.setDisable(true);
    }
}