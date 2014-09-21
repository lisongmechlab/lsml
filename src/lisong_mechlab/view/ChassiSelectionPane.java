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
package lisong_mechlab.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.OpLoadStock;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.metrics.TopSpeed;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.preferences.Preferences;
import lisong_mechlab.view.preferences.UiPreferences;
import lisong_mechlab.view.preferences.UiPreferences.Message;
import lisong_mechlab.view.render.ScrollablePanel;
import lisong_mechlab.view.render.StyleManager;

/**
 * Displays all available {@link ChassisStandard} in a pane.
 * 
 * @author Li Song
 */
public class ChassiSelectionPane extends JPanel implements MessageXBar.Reader {
	static class NameColumn extends AttributeTableColumn {
		private static final long	serialVersionUID	= -816217603635882304L;

		public NameColumn() {
			super("Chassi", 0);
		}

		@Override
		public String valueOf(Object aSourceRowObject) {
			return ((ChassisBase) aSourceRowObject).getName();
		}
	}

	static class TonsColumn extends AttributeTableColumn {
		private static final long	serialVersionUID	= -3845466109033447928L;

		public TonsColumn() {
			super("Tons", 0);
		}

		@Override
		public String valueOf(Object aSourceRowObject) {
			return Integer.toString(((ChassisBase) aSourceRowObject).getMassMax());
		}
	}

	static class JumpJetsColumn extends TableColumn {
		private static final long	serialVersionUID	= -3845466109033447928L;
		private final JPanel		panel				= new JPanel();
		private final JLabel		text				= new JLabel();

		public JumpJetsColumn() {
			super(0);
			setHeaderValue("Jump Jets");
			StyleManager.styleThinItem(text, ItemDB.lookup("JUMP JETS - CLASS V"));
		}

		@Override
		public TableCellRenderer getCellRenderer() {
			return new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
						boolean aHasFocus, int aRow, int aColumn) {
					ChassisBase chassis = (ChassisBase) aValue;
					panel.removeAll();

					final int jjsa;
					if (chassis instanceof ChassisStandard) {
						jjsa = ((ChassisStandard) chassis).getJumpJetsMax();
					} else {
						ChassisOmniMech omniMech = (ChassisOmniMech) chassis;
						int jjs = omniMech.getFixedJumpJets();
						for (OmniPod omniPod : OmniPodDB.lookupOriginal(omniMech)) {
							jjs += omniPod.getJumpJetsMax();
						}
						jjsa = jjs;
					}

					if (jjsa > 0) {
						text.setText(jjsa + " JJ");
						panel.add(text);
					}
					return panel;
				}
			};
		}
	}

	static class PilotModulesColumn extends TableColumn {
		private static final long	serialVersionUID	= -3845466109033447928L;
		private final JPanel		panel				= new JPanel();
		private final JLabel		text				= new JLabel();

		public PilotModulesColumn() {
			super(0);
			panel.add(text);
			setHeaderValue("Modules");
		}

		@Override
		public TableCellRenderer getCellRenderer() {
			return new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
						boolean aHasFocus, int aRow, int aColumn) {
					ChassisBase chassis = (ChassisBase) aValue;
					int modules = chassis.getMechModulesMax();
					if (chassis instanceof ChassisOmniMech) {
						ChassisOmniMech omniMech = (ChassisOmniMech) chassis;

						for (OmniPod omniPod : OmniPodDB.lookupOriginal(omniMech)) {
							modules += omniPod.getPilotModulesMax();
						}
					}

					text.setText(Integer.toString(modules));
					return panel;
				}
			};
		}
	}

	static class SpeedColumn extends AttributeTableColumn {
		private static final long	serialVersionUID	= -1453377097733119292L;
		DecimalFormat				df					= new DecimalFormat("###.#");

		public SpeedColumn() {
			super("Max Speed", 0);
		}

		@Override
		public String valueOf(Object aSourceRowObject) {
			if (aSourceRowObject instanceof ChassisStandard) {
				ChassisStandard chassis = (ChassisStandard) aSourceRowObject;

				Efficiencies efficiencies = new Efficiencies();
				efficiencies.setSpeedTweak(false, null);

				final double maxSpeed = TopSpeed.calculate(chassis.getEngineMax(), chassis.getMovementProfileBase(),
						chassis.getMassMax(), efficiencies.getSpeedModifier());

				efficiencies.setSpeedTweak(true, null);
				final double maxSpeedTweak = TopSpeed.calculate(chassis.getEngineMax(),
						chassis.getMovementProfileBase(), chassis.getMassMax(), efficiencies.getSpeedModifier());
				return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
			} else if (aSourceRowObject instanceof ChassisOmniMech) {
				ChassisOmniMech chassis = (ChassisOmniMech) aSourceRowObject;

				Efficiencies efficiencies = new Efficiencies();
				efficiencies.setSpeedTweak(false, null);

				final double maxSpeed = TopSpeed.calculate(chassis.getFixedEngine().getRating(),
						chassis.getMovementProfileStock(), chassis.getMassMax(), efficiencies.getSpeedModifier());

				efficiencies.setSpeedTweak(true, null);
				final double maxSpeedTweak = TopSpeed.calculate(chassis.getFixedEngine().getRating(),
						chassis.getMovementProfileStock(), chassis.getMassMax(), efficiencies.getSpeedModifier());
				return df.format(maxSpeed) + " kph (" + df.format(maxSpeedTweak) + " kph)";
			} else {
				throw new IllegalArgumentException("Unknown chassis type!");
			}
		}
	}

	static class PartColumn extends TableColumn {
		private static final long	serialVersionUID	= -6290392366218233232L;
		private final JPanel		panel				= new JPanel();
		private final JLabel		energy				= new JLabel();
		private final JLabel		ballistic			= new JLabel();
		private final JLabel		missile				= new JLabel();
		private final JLabel		ams					= new JLabel();
		private final JLabel		ecm					= new JLabel();
		private final Location		part;

		public PartColumn(Location aPart) {
			super(0);
			setHeaderValue(aPart.longName());
			StyleManager.styleThinItem(energy, HardPointType.ENERGY);
			StyleManager.styleThinItem(ballistic, HardPointType.BALLISTIC);
			StyleManager.styleThinItem(missile, HardPointType.MISSILE);
			StyleManager.styleThinItem(ams, HardPointType.AMS);
			StyleManager.styleThinItem(ecm, HardPointType.ECM);

			panel.add(energy);
			panel.add(ballistic);
			panel.add(missile);
			panel.add(ams);
			panel.add(ecm);
			part = aPart;
		}

		@Override
		public TableCellRenderer getCellRenderer() {
			return new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected,
						boolean aHasFocus, int aRow, int aColumn) {
					ChassisBase chassi = (ChassisBase) aValue;
					LoadoutBase<?> stock;
					if (aValue instanceof ChassisStandard) {
						stock = new LoadoutStandard((ChassisStandard) chassi);
						OperationStack stack = new OperationStack(0);
						stack.pushAndApply(new OpLoadStock(chassi, stock, null));
					} else if (aValue instanceof ChassisOmniMech) {
						stock = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech) chassi);
						OperationStack stack = new OperationStack(0);
						stack.pushAndApply(new OpLoadStock(chassi, stock, null));
					} else {
						throw new IllegalArgumentException("Expected a chassis type as value!");
					}
					StyleManager.styleHardpointLabel(energy, stock.getComponent(part), HardPointType.ENERGY);
					StyleManager.styleHardpointLabel(ballistic, stock.getComponent(part), HardPointType.BALLISTIC);
					StyleManager.styleHardpointLabel(missile, stock.getComponent(part), HardPointType.MISSILE);
					StyleManager.styleHardpointLabel(ams, stock.getComponent(part), HardPointType.AMS);
					StyleManager.styleHardpointLabel(ecm, stock.getComponent(part), HardPointType.ECM);
					return panel;
				}
			};
		}
	}

	private static final long	serialVersionUID	= -4134588793726908789L;
	private final List<JTable>	tables				= new ArrayList<>();
	private final JCheckBox		hideSpecials;
	private final Preferences	preferences;

	public ChassiSelectionPane(final Preferences aPreferences, MessageXBar aXBar) {
		super(new BorderLayout());
		aXBar.attach(this);

		preferences = aPreferences;
		{
			hideSpecials = new JCheckBox("Hide mech variations", preferences.uiPreferences.getHideSpecialMechs());
			hideSpecials
					.setToolTipText("<html>Will hide mech variations (champion, founders, phoenix, sarah, etc) from chassis lists.<br/>"
							+ "Stock loadouts are still available on the \"Load stock\" menu action on relevant loadouts</html>");
			hideSpecials.addActionListener(new AbstractAction() {
				private static final long	serialVersionUID	= -8136020916897237506L;

				@Override
				public void actionPerformed(ActionEvent aArg0) {
					aPreferences.uiPreferences.setHideSpecialMechs(hideSpecials.isSelected());
				}
			});
			add(hideSpecials, BorderLayout.NORTH);
		}

		JPanel tablesPanel = new ScrollablePanel();
		tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.PAGE_AXIS));

		for (Faction faction : new Faction[] { Faction.InnerSphere, Faction.Clan }) {
			for (ChassisClass chassisClass : ChassisClass.values()) {

				JTable table = new JTable(new ChassiTableModel(faction, chassisClass,
						aPreferences.uiPreferences.getHideSpecialMechs()));
				table.setRowHeight(30);
				table.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
							final JTable target = (JTable) e.getSource();
							final int row = target.getSelectedRow();
							final int column = target.getSelectedColumn();
							final Object cell = target.getValueAt(row, column);
							if (cell instanceof ChassisStandard) {
								ChassisStandard chassi = (ChassisStandard) cell;
								ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
								ProgramInit.lsml().mechLabPane.openLoadout(new LoadoutStandard(chassi));
							} else if (cell instanceof ChassisOmniMech) {
								ChassisOmniMech chassi = (ChassisOmniMech) cell;
								ProgramInit.lsml().tabbedPane.setSelectedComponent(ProgramInit.lsml().mechLabPane);
								ProgramInit.lsml().mechLabPane.openLoadout(new LoadoutOmniMech(ComponentBuilder
										.getOmniPodFactory(), chassi));

							}
						}
					}
				});

				table.removeColumn(table.getColumnModel().getColumn(0)); // Remove auto-generated column
				table.addColumn(new NameColumn());
				table.addColumn(new SpeedColumn());
				table.addColumn(new TonsColumn());
				table.addColumn(new PilotModulesColumn());
				for (Location part : Arrays.asList(Location.RightArm, Location.RightTorso, Location.CenterTorso,
						Location.LeftTorso, Location.LeftArm, Location.Head)) {
					table.addColumn(new PartColumn(part));
				}
				table.addColumn(new JumpJetsColumn());
				tables.add(table);

				JPanel tp = new JPanel(new BorderLayout());
				tp.add(table.getTableHeader(), BorderLayout.NORTH);
				tp.add(table, BorderLayout.CENTER);

				tablesPanel.add(new CollapsiblePanel(faction.toString() + " - " + chassisClass.toString(), tp, true));
			}
		}

		tablesPanel.add(Box.createVerticalGlue());

		JScrollPane js = new JScrollPane(tablesPanel);
		js.getVerticalScrollBar().setUnitIncrement(32);
		add(js, BorderLayout.CENTER);

		add(Box.createVerticalGlue(), BorderLayout.SOUTH);
	}

	@Override
	public void receive(MessageXBar.Message aMsg) {
		if (aMsg instanceof UiPreferences.Message) {
			UiPreferences.Message msg = (Message) aMsg;
			if (msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS) {
				hideSpecials.setSelected(preferences.uiPreferences.getHideSpecialMechs());

				for (JTable table : tables) {
					((ChassiTableModel) table.getModel()).recreate(hideSpecials.isSelected());
				}
			}
		}
	}

}
