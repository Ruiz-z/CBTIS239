module cbtis239.front {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    opens cbtis239.front to javafx.fxml;
    opens cbtis239.front.ui.users to javafx.fxml;
    opens cbtis239.front.api.dto to com.fasterxml.jackson.databind;

    exports cbtis239.front;
}
