module com.example.multimedia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.multimedia to javafx.fxml;
    exports com.example.multimedia;
}