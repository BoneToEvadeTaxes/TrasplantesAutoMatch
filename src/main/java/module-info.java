module com.trasplantes.trasplantes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.trasplantes.trasplantes to javafx.fxml;
    exports com.trasplantes.trasplantes;
}