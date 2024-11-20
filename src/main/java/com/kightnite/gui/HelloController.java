package main.java.com.kightnite.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import main.java.com.kightnite.client.Client;
import main.java.com.kightnite.model.ClientData;

import java.net.SocketAddress;
import java.util.List;

public class HelloController {

    @FXML
    public ScrollPane scrollPane;

    @FXML
    private Label welcomeText;

    Client client;

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
    protected void onConnectButtonClick(SocketAddress address) {
        client.connectToPeer(address);

    }

    @FXML
    protected void onAcceptButtonClick(SocketAddress address) {
        client.clientListener.acceptRequest(address);
    }

    @FXML
    protected void onRejectButtonClick(SocketAddress address) {
        client.clientListener.rejectRequest(address);
    }

    private GridPane createConnectionGrid(List<ClientData> connections) {

        GridPane gridConnections = new GridPane();

        for(int i=0; i<connections.size(); i++){
            SocketAddress address = connections.get(i).address;

            Button buttonConnect = new Button();
            buttonConnect.setText("Connect");
            buttonConnect.setOnAction(actionEvent -> onConnectButtonClick(address));

            Label label = new Label();
            label.setText(connections.get(i).toString());


            //add them to the GridPane
            gridConnections.add(buttonConnect, 0, i); //  (child, columnIndex, rowIndex)
            gridConnections.add(label , 3, i);

            if (connections.get(i).isPending) {
                Button buttonAccept = new Button();
                buttonAccept.setText("Accept");
                buttonAccept.setOnAction(actionEvent -> onAcceptButtonClick(address));

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

    protected void onPendingConnection() {
        onRerfreshButton();
    }
}