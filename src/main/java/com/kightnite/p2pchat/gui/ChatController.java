package com.kightnite.p2pchat.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import com.kightnite.p2pchat.client.Client;
import com.kightnite.p2pchat.model.ChatMessage;
import com.kightnite.p2pchat.model.ClientData;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
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



    private Client client;

    private ClientData currentChat = null;

    private final HashSet<SocketAddress> favourites = new HashSet<>();

    @FXML
    protected void onRefreshButton() {
        List<ClientData> connections = client.ping();

        System.out.println("Client Connections:");
        for (ClientData connection : connections) {
            System.out.println(connection + " " + connection.isPending);
        }

        scrollPane.setContent(createConnectionVBox(connections));
    }

    protected void onFavouriteButtonClick(SocketAddress address) {
        if (favourites.contains(address)) {
            favourites.remove(address);
        } else {
            favourites.add(address);
        }

        onRefreshButton();
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

    @FXML
    protected void onCopyToClipboard(String data) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(data);
        clipboard.setContent(content);
    }

    private VBox createConnectionVBox(List<ClientData> connections) {
        VBox vbox = new VBox(5);

        connections.sort(Comparator.comparing(x -> !favourites.contains(x.address)));

        for (int i=0; i<connections.size(); i++) {
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

        // Create Context Menu for each connection
        MenuItem menuItem = new MenuItem(data.address.toString() + "\nCopy to Clipboard");
        menuItem.setOnAction(actionEvent -> onCopyToClipboard(data.address.toString()));
        ContextMenu contextMenu = createContextMenu(menuItem);
        hbox.setOnContextMenuRequested(e -> {
            contextMenu.show(hbox.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        // Create buttons based on current state with other connections
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

        // Client name label
        Label label = new Label();
        label.setText(data.data);
        label.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(label, Priority.ALWAYS);
        hbox.getChildren().add(label);

        // Create a region to act as a horizontal spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().add(spacer);

        // Favourite button
        Button button = createFavouriteButton(data.address);
        hbox.getChildren().add(button);

        return hbox;
    }

    private Button createButton(String text, EventHandler<ActionEvent> actionEvent) {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(actionEvent);
        button.getStyleClass().add("connection-button");

        return button;
    }

    private Button createConnectButton(String text, SocketAddress address) {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(actionEvent -> onConnectButtonClick(address, button));
        button.getStyleClass().add("connection-button");

        return button;
    }

    private Button createFavouriteButton(SocketAddress address) {
        Region icon = new Region();
        icon.getStyleClass().add("favourite-icon");
        Button button = new Button();
        button.getStyleClass().add("connection-button");
        button.setOnAction(actionEvent -> onFavouriteButtonClick(address));
        button.setGraphic(icon);

        if (favourites.contains(address)) {
            button.getStyleClass().add("unfavourite-button");
        } else {
            button.getStyleClass().add("favourite-button");
        }
        return button;
    }

    private ContextMenu createContextMenu(MenuItem... menuItems) {
        ContextMenu contextMenu = new ContextMenu(menuItems);

        return contextMenu;
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

        MenuItem menuItem = new MenuItem(client.getInviteSocketAddress().toString());
        this.welcomeText.setContextMenu(createContextMenu(menuItem));
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