module org.lisoft.lsml {
    requires java.base;
    requires java.logging;
    requires java.desktop; // awt

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    opens org.lisoft.lsml.view_fx.controllers to javafx.fxml;

    requires javax.inject;

    exports org.lisoft.lsml.view_fx;
}
