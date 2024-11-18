package main.java.com.kightnite.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.kightnite.client.Client;

import java.io.IOException;

public class HelloApplication extends Application {
    private Client client;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Client
        this.client = new Client();
        client.startClient("App");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/kightnite/P2Pchat/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);
        scene.getStylesheets().add("/com/kightnite/P2Pchat/stylesheet.css");

        HelloController controller = fxmlLoader.getController();
        controller.setClient(this.client);

        // Add listener
        client.clientListener.addPendingConnectionListener(controller::onPendingConnection);

        stage.setTitle("P2PChat");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // Close all threads
        if (client.clientListener != null) {
            client.clientListener.listenerServerSocket.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}