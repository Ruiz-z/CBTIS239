module cbtis239.front {
    requires javafx.controls;
    requires javafx.fxml;

    opens cbtis239.front to javafx.fxml;
    exports cbtis239.front;
}
