module cbtis239.front {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires com.zaxxer.hikari;

    opens cbtis239.front to javafx.fxml;
    exports cbtis239.front;
    exports cbtis239.front.ui.users;
    opens cbtis239.front.ui.users to javafx.fxml;
}
