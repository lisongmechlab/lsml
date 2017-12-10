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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * This is a {@link TreeItem} which can have a {@link Predicate} applied to the
 * items to show as children.
 *
 * @author Emily Björk
 * @param <T>
 *            The type of the tree item.
 *
 */
public class FilterTreeItem<T> extends TreeItem<T> {
	private final ObservableList<TreeItem<T>> source;
	private Predicate<TreeItem<T>> predicate;

	public FilterTreeItem() {
		this(null, null);
	}

	public FilterTreeItem(T aValue) {
		this(aValue, null);
	}

	public FilterTreeItem(T aValue, Node aGraphic) {
		super(aValue, aGraphic);
		source = FXCollections.observableArrayList(super.getChildren());
	}

	public void add(TreeItem<T> aChild) {
		getChildrenRaw().add(aChild);
	}

	public ObservableList<TreeItem<T>> getChildrenRaw() {
		return source;
	}

	public void setPredicateRecursively(Predicate<TreeItem<T>> aPredicate) {
		for (TreeItem<T> child : source) {
			if (child instanceof FilterTreeItem) {
				FilterTreeItem<T> treeItem = (FilterTreeItem<T>) child;
				treeItem.setPredicateRecursively(aPredicate);
			}
		}
		predicate = aPredicate;
		updatePredicate();
	}

	public void updatePredicate() {
		for (TreeItem<T> child : source) {
			if (child instanceof FilterTreeItem) {
				((FilterTreeItem<?>) child).updatePredicate();
			}
		}

		// Java 9.0.1 seems to have a bug (?) where if you assign the the same contents
		// the the children of a TreeItem<> as it had previously it generates an empty
		// change message that then causes an IllegalStateException to be thrown.
		// :frownyface:
		FilteredList<TreeItem<T>> filtered = source.filtered(predicate);
		if (!getChildren().equals(filtered)) {
			getChildren().setAll(filtered);
		}
	}

}
