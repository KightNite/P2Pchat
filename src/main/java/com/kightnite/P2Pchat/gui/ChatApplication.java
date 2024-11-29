package com.kightnite.p2pchat.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.kightnite.p2pchat.client.Client;
import com.kightnite.p2pchat.events.ChatListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Properties;

public class ChatApplication extends Application {
    private Client client;

    @Override
    public void start(Stage stage) throws IOException {
        // Load properties

        String resourceName = "com/kightnite/p2pchat/client.properties"; // could also be a constant
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/com/kightnite/p2pchat/client.properties"));

        String hostname = properties.getProperty("hostname");
        int port = Integer.parseInt(properties.getProperty("port"));

        System.out.println(hostname + " " + port);

//        String hostname = "localhost";
//        int port = 9090;

        // Initialize Client
        this.client = new Client();
        client.startClient("App", hostname, port);

        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("/com/kightnite/p2pchat/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);
        scene.getStylesheets().add(getClass().getResource("/com/kightnite/p2pchat/stylesheet.css").toExternalForm());

        ChatController controller = fxmlLoader.getController();
        controller.setClient(this.client);

        // Add listener
//        client.clientListener.addPendingConnectionListener(controller::onPendingConnection);

        client.clientConnection.addPendingConnectionListener(new ChatListener() {
            @Override
            public void onNewConnection() {
                controller.onPendingConnection();
            }

            @Override
            public void onNewMessage(SocketAddress senderAddress) {
                controller.onNewMessage(senderAddress);
            }

            @Override
            public void onConnectionClose(SocketAddress senderAddress) {
                controller.onConnectionClose(senderAddress);
            }
        });

        stage.setTitle("p2pchat");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // Close all threads
        if (client.clientConnection != null) {
            client.clientConnection.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}