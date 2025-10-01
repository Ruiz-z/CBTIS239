module cbtis239.front {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    opens cbtis239.front to javafx.fxml;
    exports cbtis239.front;
}
