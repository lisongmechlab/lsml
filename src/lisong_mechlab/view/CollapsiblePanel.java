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
package lisong_mechlab.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This is a Swing panel that can be collapsed and expanded by the user.
 * 
 * @author Emily Björk
 */
public class CollapsiblePanel extends JPanel {
	private static final long	serialVersionUID	= 3493431469019201319L;
	private final JLabel		title				= new JLabel();
	private final Component		content;

	public CollapsiblePanel(String aTitle, Component aComponent, boolean aStartCollapsed) {
		super(new BorderLayout());
		content = aComponent;
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createEtchedBorder()));

		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(18.0f));
		add(title, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);

		title.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent aE) {
				if (aE.getClickCount() == 1) {
					content.setVisible(!content.isVisible());
				}
			}
		});

		if (aStartCollapsed)
			content.setVisible(false);

		setTitle(aTitle);
	}

	public void setTitle(String aTitle) {
		title.setText(aTitle);
	}
}
