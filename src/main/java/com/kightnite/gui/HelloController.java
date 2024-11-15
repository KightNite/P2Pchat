package main.java.com.kightnite.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import main.java.com.kightnite.client.Client;
import main.java.com.kightnite.model.ClientConnection;

import java.net.SocketAddress;
import java.util.List;

public class HelloController {

    @FXML
    public ScrollPane scrollPane;

    @FXML
    private Label welcomeText;

//    @FXML
//    private GridPane gridConnections;


    Client client;

    @FXML
    protected void onRerfreshButton() {

        List<ClientConnection> connections = client.ping();

        for (ClientConnection connection : connections) {
            System.out.println(connection);
        }

        scrollPane.setContent(createConnectionGrid(connections));

        welcomeText.setText("Refreshing...");

    }

    @FXML
    protected void onConnectButtonClick(SocketAddress address) {
        client.connectToPeer(address);

    }

    private GridPane createConnectionGrid(List<ClientConnection> connections) {

        GridPane gridConnections = new GridPane();

        for(int i=0; i<connections.size(); i++){
            Button button = new Button();
            button.setText("Connect");
            int index = i;
            button.setOnAction(actionEvent -> onConnectButtonClick(connections.get(index).dataSocket.address));

            Label label = new Label();
            label.setText(connections.get(i).toString());


            //add them to the GridPane
            gridConnections.add(button, 0, i); //  (child, columnIndex, rowIndex)
            gridConnections.add(label , 1, i);

            // margins are up to your preference
            GridPane.setMargin(button, new Insets(5));
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