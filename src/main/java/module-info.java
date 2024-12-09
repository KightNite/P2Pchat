module com.kightnight.p2pchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.kightnite.p2pchat.gui to javafx.fxml;
    exports com.kightnite.p2pchat.client;
    exports com.kightnite.p2pchat.events;
    exports com.kightnite.p2pchat.gui;
    exports com.kightnite.p2pchat.model;
    exports com.kightnite.p2pchat.server;

}