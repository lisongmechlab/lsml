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
package org.lisoft.lsml.view_fx.controls;

import static javafx.beans.binding.Bindings.createObjectBinding;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * This is a {@link TreeItem} which can have a {@link Predicate} applied to the items to show as children.
 *
 * @author Li Song
 * @param <T>
 *            The type of the tree item.
 *
 */
public class FilterTreeItem<T> extends TreeItem<T> {

    private final ObservableList<TreeItem<T>> source;
    private final ObjectProperty<Predicate<TreeItem<T>>> predicate;

    public FilterTreeItem() {
        this((T) null);
    }

    public FilterTreeItem(Predicate<TreeItem<T>> aPredicate) {
        this(null, null, aPredicate);
    }

    public FilterTreeItem(T aValue) {
        this(aValue, (Node) null);
    }

    public FilterTreeItem(T aValue, Node aGraphic) {
        this(aValue, aGraphic, null);
    }

    public FilterTreeItem(T aValue, Node aGraphic, Predicate<TreeItem<T>> aPredicate) {
        super(aValue, aGraphic);
        source = FXCollections.observableArrayList(super.getChildren());

        predicate = new SimpleObjectProperty<>(aPredicate);

        FilteredList<TreeItem<T>> filtered = new FilteredList<>(source);
        filtered.predicateProperty().bind(createObjectBinding(() -> (aTreeItem) -> {
            final Predicate<TreeItem<T>> itemPredicate = predicate.get();
            if (aTreeItem instanceof FilterTreeItem) {
                final FilterTreeItem<T> filterTreeItem = (FilterTreeItem<T>) aTreeItem;
                filterTreeItem.setPredicate(itemPredicate);
            }

            if (itemPredicate == null) {
                return true;
            }

            if (!aTreeItem.getChildren().isEmpty()) {
                return true;
            }

            return itemPredicate.test(aTreeItem);
        }, predicate, Bindings.size(filtered)));

        setHiddenFieldChildren(filtered);
    }

    public FilterTreeItem(T aValue, Predicate<TreeItem<T>> aPredicate) {
        this(aValue, null, aPredicate);
    }

    public void add(TreeItem<T> aChild) {
        getChildrenRaw().add(aChild);
    }

    public ObservableList<TreeItem<T>> getChildrenRaw() {
        return source;
    }

    public Predicate<TreeItem<T>> getPredicate() {
        return predicate.get();
    }

    public ObjectProperty<Predicate<TreeItem<T>>> predicateProperty() {
        return predicate;
    }

    public void reEvaluatePredicate() {
        final Predicate<TreeItem<T>> p = predicate.get();
        predicate.set(null);
        predicate.set(p);
    }

    public void setPredicate(Predicate<TreeItem<T>> aPredicate) {
        predicate.set(aPredicate);
    }

    protected void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
        try {
            final Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
            childrenField.setAccessible(true);
            childrenField.set(this, list);

            final Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
            declaredField.setAccessible(true);
            @SuppressWarnings("unchecked")
            final ListChangeListener<? super TreeItem<T>> listener = (ListChangeListener<? super TreeItem<T>>) declaredField
                    .get(this);
            list.addListener(listener);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
        }
    }
}
