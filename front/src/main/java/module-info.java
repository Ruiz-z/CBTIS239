module cbtis239.front {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens cbtis239.front to javafx.fxml;
    exports cbtis239.front;
}