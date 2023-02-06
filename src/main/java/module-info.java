open module org.lisoft.lsml {
  requires java.desktop; // awt
  requires javafx.base;
  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.fxml;
  requires javax.inject;
  requires xstream;
  requires dagger;

  /* The public API of mwo_data. TODO: Create own module for mwo_data to maintain separation of concerns. */
  exports org.lisoft.mwo_data.equipment;
  exports org.lisoft.mwo_data.mechs;
  exports org.lisoft.mwo_data.modifiers;
  exports org.lisoft.mwo_data;
}
