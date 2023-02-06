open module org.lisoft.lsml {
  requires java.desktop; // awt
  requires javafx.base;
  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.fxml;
  /*
  opens org.lisoft.lsml.model.item to javafx.base, xstream;
  opens org.lisoft.lsml.view_fx.controllers to javafx.fxml;
  opens org.lisoft.lsml.view_fx.controllers.mainwindow to javafx.fxml;
  opens org.lisoft.lsml.view_fx.controllers.loadoutwindow to javafx.fxml;

  opens org.lisoft.lsml.model to xstream;
  opens org.lisoft.lsml.model.garage to xstream;
  opens org.lisoft.lsml.model.database to xstream;
  opens org.lisoft.lsml.model.chassi to xstream;
  opens org.lisoft.lsml.model.loadout to xstream;
  opens org.lisoft.lsml.model.upgrades to xstream;
  opens org.lisoft.lsml.model.modifiers to xstream;
  opens org.lisoft.lsml.model.environment to xstream;
  opens org.lisoft.lsml.model.database.gamedata to xstream;
  opens org.lisoft.lsml.model.database.gamedata.helpers to xstream;*/

  requires javax.inject;
  requires xstream;
  requires dagger;

  exports org.lisoft.lsml.view_fx;
  exports org.lisoft.lsml.command to
      JUnitParams;
  exports org.lisoft.mwo_data to
      JUnitParams;
  exports org.lisoft.mwo_data.equipment to
      JUnitParams;
  exports org.lisoft.mwo_data.mechs to
      JUnitParams;
  exports org.lisoft.lsml.model.loadout to
      JUnitParams;
  exports org.lisoft.mwo_data.modifiers to
      JUnitParams;
  exports org.lisoft.mwo_data.mwo_parser to
      JUnitParams;
  exports org.lisoft.lsml.model to
      JUnitParams;
}
