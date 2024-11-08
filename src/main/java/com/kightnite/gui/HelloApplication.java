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
        this.client = new Client();
        client.startClient("App Client");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/kightnite/P2Pchat/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add("/com/kightnite/P2Pchat/stylesheet.css");

        HelloController controller = fxmlLoader.getController();
        controller.setClient(this.client);

        stage.setTitle("P2PChat");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // Close all threads
        client.clientListener.listenerServerSocket.close();
    }

    public static void main(String[] args) {
        launch();
    }
}