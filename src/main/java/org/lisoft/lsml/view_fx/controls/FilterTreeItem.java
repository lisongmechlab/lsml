/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.view_fx.controls;

import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * This is a {@link TreeItem} which can have a {@link Predicate} applied to the items to show as children.
 * 
 * @author Emily Björk
 * @param <T>
 *            The type of the tree item.
 *
 */
public class FilterTreeItem<T> extends TreeItem<T> {

    private final FilteredList<TreeItem<T>> filtered;

    public FilterTreeItem() {
        this((T) null);
    }

    public FilterTreeItem(T aValue) {
        this(aValue, (Node) null);
    }

    public FilterTreeItem(Predicate<? super TreeItem<T>> predicate) {
        this(null, null, predicate);
    }

    public FilterTreeItem(T aValue, Predicate<? super TreeItem<T>> predicate) {
        this(aValue, null, predicate);
    }

    public FilterTreeItem(T aValue, Node aGraphic) {
        super(aValue, aGraphic);
        filtered = new FilteredList<>(super.getChildren());
    }

    public FilterTreeItem(T aValue, Node aGraphic, Predicate<? super TreeItem<T>> predicate) {
        super(aValue, aGraphic);
        filtered = new FilteredList<>(super.getChildren(), predicate);
    }

    public void setPredicate(Predicate<? super TreeItem<T>> predicate) {
        filtered.setPredicate(predicate);
    }

    public Predicate<? super TreeItem<T>> getPredicate() {
        return filtered.getPredicate();
    }

    public ObjectProperty<Predicate<? super TreeItem<T>>> predicateProperty() {
        return filtered.predicateProperty();
    }

    public void add(TreeItem<T> aChild) {
        super.getChildren().add(aChild);
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        return filtered;
    }

    public ObservableList<TreeItem<T>> getChildrenRaw() {
        return super.getChildren();
    }
}
