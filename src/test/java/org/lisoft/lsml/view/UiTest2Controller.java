/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//@formatter:on
package org.lisoft.lsml.view;

import java.io.IOException;
import java.net.URL;

import com.sun.javafx.scene.control.skin.TreeTableRowSkin;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * @author Li Song
 *
 */
public class UiTest2Controller extends Application {
    public static class xSkin<T> extends TreeTableRowSkin<T> {

        /**
         * @param aControl
         */
        public xSkin(TreeTableRow<T> aControl) {
            super(aControl);
        }

        @Override
        protected boolean isIndentationRequired() {
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    ComboBox<String> box;
    private Stage stage;

    @FXML
    TreeTableView<String> tree;

    @SuppressWarnings("unchecked")
    @FXML
    public void initialize() {
        box.getItems().setAll("Foo", "Bar", "Baz", "Foba");

        final TreeItem<String> a = new TreeItem<String>("foo");
        final TreeItem<String> b = new TreeItem<String>("bar");
        final TreeItem<String> c = new TreeItem<String>("baz");

        final TreeItem<String> c0 = new TreeItem<String>("Cat0");
        c0.getChildren().setAll(a, b);
        final TreeItem<String> c1 = new TreeItem<String>("Cat0");
        c1.getChildren().setAll(c);

        final TreeItem<String> root = new TreeItem<>();
        root.getChildren().setAll(c0, c1);
        tree.setRoot(root);

        final TreeTableColumn<String, String> first = new TreeTableColumn<>("F");
        final TreeTableColumn<String, String> second = new TreeTableColumn<>("Fb");
        first.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().getValue()));
        second.setCellValueFactory(features -> new ReadOnlyStringWrapper("xxx" + features.getValue().getValue()));

        // tree.setRowFactory(table -> {
        // final TreeTableRow<String> row = new TreeTableRow<String>() {
        // @Override
        // protected Skin<?> createDefaultSkin() {
        // final TreeTableRowSkin<String> skin = new TreeTableRowSkin<String>(this);
        // skin.setIndent(-20);
        // return skin;
        // }
        // };
        // return row;
        // });
        tree.getColumns().setAll(first, second);
        tree.setShowRoot(false);

    }

    @FXML
    public void reload() {
        final URL url = getClass().getClassLoader().getResource("view/UiTest2.fxml");
        final FXMLLoader loader = new FXMLLoader(url);
        try {
            stage.setScene(new Scene(loader.load()));
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage aPrimaryStage) throws Exception {
        stage = aPrimaryStage;
        reload();
        aPrimaryStage.show();
    }
}
