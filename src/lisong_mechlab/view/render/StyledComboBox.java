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
package lisong_mechlab.view.render;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * Stolen and adapted from:
 * http://stackoverflow.com/questions/956003/how-can-i-change-the-width-of-a-jcombobox-dropdown-list
 * 
 * @author Li Song
 */
public class StyledComboBox implements PopupMenuListener {
	private boolean		scrollBarRequired	= true;
	private boolean		popupWider;
	private int			maximumWidth		= -1;
	private boolean		popupAbove;
	private JScrollPane	scrollPane;

	/**
	 * Convenience constructor to allow the display of a horizontal scrollbar when required.
	 */
	public StyledComboBox() {
		this(true, false, -1, false);
	}

	/**
	 * Convenience constructor that allows you to display the popup wider and/or above the combo box.
	 *
	 * @param aPopupWider
	 *            when true, popup width is based on the popup preferred width
	 * @param aPopupAbove
	 *            when true, popup is displayed above the combobox
	 */
	public StyledComboBox(boolean aPopupWider, boolean aPopupAbove) {
		this(false, aPopupWider, -1, aPopupAbove);
	}

	/**
	 * Convenience constructor that allows you to display the popup wider than the combo box and to specify the maximum
	 * width
	 *
	 * @param aMaximumWidth
	 *            the maximum width of the popup. The popupAbove value is set to "true".
	 */
	public StyledComboBox(int aMaximumWidth) {
		this(true, true, aMaximumWidth, false);
	}

	/**
	 * General purpose constructor to set all popup properties at once.
	 *
	 * @param aScrollBarRequired
	 *            display a horizontal scrollbar when the preferred width of popup is greater than width of scrollPane.
	 * @param aPopupWider
	 *            display the popup at its preferred with
	 * @param aMaximumWidth
	 *            limit the popup width to the value specified (minimum size will be the width of the combo box)
	 * @param aPopupAbove
	 *            display the popup above the combo box
	 */
	public StyledComboBox(boolean aScrollBarRequired, boolean aPopupWider, int aMaximumWidth, boolean aPopupAbove) {
		setScrollBarRequired(aScrollBarRequired);
		setPopupWider(aPopupWider);
		setMaximumWidth(aMaximumWidth);
		setPopupAbove(aPopupAbove);
	}

	/**
	 * Return the maximum width of the popup.
	 *
	 * @return the maximumWidth value
	 */
	public int getMaximumWidth() {
		return maximumWidth;
	}

	/**
	 * Set the maximum width for the popup. This value is only used when setPopupWider( true ) has been specified. A
	 * value of -1 indicates that there is no maximum.
	 *
	 * @param aMaximumWidth
	 *            the maximum width of the popup
	 */
	public void setMaximumWidth(int aMaximumWidth) {
		this.maximumWidth = aMaximumWidth;
	}

	/**
	 * Determine if the popup should be displayed above the combo box.
	 *
	 * @return the popupAbove value
	 */
	public boolean isPopupAbove() {
		return popupAbove;
	}

	/**
	 * Change the location of the popup relative to the combo box.
	 *
	 * @param aPopupAbove
	 *            true display popup above the combo box, false display popup below the combo box.
	 */
	public void setPopupAbove(boolean aPopupAbove) {
		this.popupAbove = aPopupAbove;
	}

	/**
	 * Determine if the popup might be displayed wider than the combo box
	 *
	 * @return the popupWider value
	 */
	public boolean isPopupWider() {
		return popupWider;
	}

	/**
	 * Change the width of the popup to be the greater of the width of the combo box or the preferred width of the
	 * popup. Normally the popup width is always the same size as the combo box width.
	 *
	 * @param aPopupWider
	 *            true adjust the width as required.
	 */
	public void setPopupWider(boolean aPopupWider) {
		this.popupWider = aPopupWider;
	}

	/**
	 * Determine if the horizontal scroll bar might be required for the popup
	 *
	 * @return the scrollBarRequired value
	 */
	public boolean isScrollBarRequired() {
		return scrollBarRequired;
	}

	/**
	 * For some reason the default implementation of the popup removes the horizontal scrollBar from the popup scroll
	 * pane which can result in the truncation of the rendered items in the popop. Adding a scrollBar back to the
	 * scrollPane will allow horizontal scrolling if necessary.
	 *
	 * @param aScrollBarRequired
	 *            true add horizontal scrollBar to scrollPane false remove the horizontal scrollBar
	 */
	public void setScrollBarRequired(boolean aScrollBarRequired) {
		this.scrollBarRequired = aScrollBarRequired;
	}

	/**
	 * Alter the bounds of the popup just before it is made visible.
	 */
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent aEvent) {
		JComboBox<?> comboBox = (JComboBox<?>) aEvent.getSource();

		if (comboBox.getItemCount() == 0)
			return;

		final Object child = comboBox.getAccessibleContext().getAccessibleChild(0);

		if (child instanceof BasicComboPopup) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					customizePopup((BasicComboPopup) child);
				}
			});
		}
	}

	protected void customizePopup(BasicComboPopup aPopup) {
		scrollPane = getScrollPane(aPopup);

		if (popupWider)
			popupWider(aPopup);

		checkHorizontalScrollBar(aPopup);

		// For some reason in JDK7 the popup will not display at its preferred
		// width unless its location has been changed from its default
		// (ie. for normal "pop down" shift the popup and reset)

		Component comboBox = aPopup.getInvoker();
		Point location = comboBox.getLocationOnScreen();

		if (popupAbove) {
			int height = aPopup.getPreferredSize().height;
			aPopup.setLocation(location.x, location.y - height);
		} else {
			int height = comboBox.getPreferredSize().height;
			aPopup.setLocation(location.x, location.y + height - 1);
			aPopup.setLocation(location.x, location.y + height);
		}
	}

	/*
	 * Adjust the width of the scrollpane used by the popup
	 */
	protected void popupWider(BasicComboPopup aPopup) {
		JList<?> list = aPopup.getList();

		// Determine the maximimum width to use:
		// a) determine the popup preferred width
		// b) limit width to the maximum if specified
		// c) ensure width is not less than the scroll pane width

		int popupWidth = list.getPreferredSize().width + 5 // make sure horizontal scrollbar doesn't appear
				+ getScrollBarWidth(aPopup, scrollPane);

		if (maximumWidth != -1) {
			popupWidth = Math.min(popupWidth, maximumWidth);
		}

		Dimension scrollPaneSize = scrollPane.getPreferredSize();
		popupWidth = Math.max(popupWidth, scrollPaneSize.width);

		Dimension scrollPaneMaxSize = scrollPane.getMaximumSize();

		// Adjust the width

		scrollPaneSize.width = popupWidth;
		scrollPaneMaxSize.width = popupWidth;
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMaximumSize(scrollPaneMaxSize);
	}

	/*
	 * This method is called every time: - to make sure the viewport is returned to its default position - to remove the
	 * horizontal scrollbar when it is not wanted
	 */
	private void checkHorizontalScrollBar(BasicComboPopup aPopup) {
		// Reset the viewport to the left

		JViewport viewport = scrollPane.getViewport();
		Point p = viewport.getViewPosition();
		p.x = 0;
		viewport.setViewPosition(p);

		// Remove the scrollbar so it is never painted

		if (!scrollBarRequired) {
			scrollPane.setHorizontalScrollBar(null);
			return;
		}

		// Make sure a horizontal scrollbar exists in the scrollpane

		JScrollBar horizontal = scrollPane.getHorizontalScrollBar();

		if (horizontal == null) {
			horizontal = new JScrollBar(Adjustable.HORIZONTAL);
			scrollPane.setHorizontalScrollBar(horizontal);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}

		// Potentially increase height of scroll pane to display the scrollbar

		if (horizontalScrollBarWillBeVisible(aPopup, scrollPane)) {
			Dimension scrollPaneSize = scrollPane.getPreferredSize();
			scrollPaneSize.height += horizontal.getPreferredSize().height;
			scrollPane.setPreferredSize(scrollPaneSize);
			scrollPane.setMaximumSize(scrollPaneSize);
			scrollPane.revalidate();
		}
	}

	/*
	 * Get the scroll pane used by the popup so its bounds can be adjusted
	 */
	protected JScrollPane getScrollPane(BasicComboPopup aPopup) {
		JList<?> list = aPopup.getList();
		Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, list);

		return (JScrollPane) c;
	}

	/*
	 * I can't find any property on the scrollBar to determine if it will be displayed or not so use brute force to
	 * determine this.
	 */
	protected int getScrollBarWidth(BasicComboPopup aPopup, JScrollPane aScrollPane) {
		int scrollBarWidth = 0;
		JComboBox<?> comboBox = (JComboBox<?>) aPopup.getInvoker();

		if (comboBox.getItemCount() > comboBox.getMaximumRowCount()) {
			JScrollBar vertical = aScrollPane.getVerticalScrollBar();
			scrollBarWidth = vertical.getPreferredSize().width;
		}

		return scrollBarWidth;
	}

	/*
	 * I can't find any property on the scrollBar to determine if it will be displayed or not so use brute force to
	 * determine this.
	 */
	protected boolean horizontalScrollBarWillBeVisible(BasicComboPopup aPopup, JScrollPane aScrollPane) {
		JList<?> list = aPopup.getList();
		int scrollBarWidth = getScrollBarWidth(aPopup, aScrollPane);
		int popupWidth = list.getPreferredSize().width + scrollBarWidth;

		return popupWidth > aScrollPane.getPreferredSize().width;
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent aEvent) {/* no-op */
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// In its normal state the scrollpane does not have a scrollbar

		if (scrollPane != null) {
			scrollPane.setHorizontalScrollBar(null);
		}
	}
}
