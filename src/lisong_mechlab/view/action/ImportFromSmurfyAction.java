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
package lisong_mechlab.view.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import lisong_mechlab.model.garage.OpAddToGarage;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.model.loadout.export.SmurfyImportExport;
import lisong_mechlab.util.SwingHelpers;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.preferences.SmurfyPreferences;

/**
 * This action opens a dialog to perform import from Smurfy.
 * 
 * @author Li Song
 */
public class ImportFromSmurfyAction extends AbstractAction {
    private static final long        serialVersionUID = -9002912389559075628L;
    protected static final String    EMPTY_API_KEY    = "0000000000000000000000000000000000000000";
    private final Window             parent;
    private final Base64LoadoutCoder decoder;

    private final DefaultTableModel  model            = new DefaultTableModel(null,
                                                              new String[] { "Import", "Loadout" }) {
                                                          private static final long serialVersionUID = 1L;

                                                          @Override
                                                          public boolean isCellEditable(int rowIndex, int columnIndex) {
                                                              return columnIndex == 0;
                                                          }

                                                          @Override
                                                          public Class<?> getColumnClass(int c) {
                                                              if (c == 0)
                                                                  return Boolean.class;
                                                              return super.getColumnClass(c);
                                                          }
                                                      };

    private void clearModel() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    /**
     * @param aWindow
     *            The parent window for the dialog that is shown.
     * @param aDecoder
     *            The decoder to use to decode the incoming load outs.
     */
    public ImportFromSmurfyAction(Window aWindow, Base64LoadoutCoder aDecoder) {
        super("Import from Smurfy...");
        parent = aWindow;
        decoder = aDecoder;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        clearModel();

        final SmurfyPreferences preferences = ProgramInit.lsml().preferences.smurfyPreferences;
        final JPanel topPanel = new JPanel(new BorderLayout());

        final JLabel apiKeyLabel = new JLabel("API-Key: ");
        final JTextField textApiKey = new JTextField(preferences.shouldRememberAPIKey() ? preferences.getApiKey()
                : EMPTY_API_KEY);
        final JPanel apiPanel = new JPanel(new BorderLayout());
        final JCheckBox rememberKey = new JCheckBox("Remember API key", preferences.shouldRememberAPIKey());
        apiPanel.add(apiKeyLabel, BorderLayout.WEST);
        apiPanel.add(textApiKey, BorderLayout.CENTER);
        apiPanel.add(rememberKey, BorderLayout.EAST);
        topPanel.add(apiPanel, BorderLayout.NORTH);

        final JCheckBox checkAllBox = new JCheckBox("Select all");
        final JLabel apiLink = new JLabel();
        topPanel.add(checkAllBox, BorderLayout.WEST);
        topPanel.add(apiLink, BorderLayout.EAST);

        final JTable mechs = new JTable(model);
        model.setColumnCount(2);
        mechs.getColumnModel().getColumn(0).setPreferredWidth(50);
        mechs.getColumnModel().getColumn(1).setPreferredWidth(500);
        mechs.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        final JButton cancelButton = new JButton("Cancel");
        final JButton importButton = new JButton("Import");
        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);

        final JPanel importPanel = new JPanel(new BorderLayout());
        importPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        importPanel.add(topPanel, BorderLayout.NORTH);
        importPanel.add(new JScrollPane(mechs), BorderLayout.CENTER);
        importPanel.add(buttonPanel, BorderLayout.SOUTH);

        final JDialog dialog = new JDialog(parent, "Smurfy import", ModalityType.APPLICATION_MODAL);
        dialog.setAutoRequestFocus(true);
        dialog.setContentPane(importPanel);
        dialog.setIconImage(ProgramInit.programIcon);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        textApiKey.setForeground(Color.GRAY);
        textApiKey.addCaretListener(new CaretListener() {
            String lastKey;

            @Override
            public void caretUpdate(CaretEvent carret) {
                if (textApiKey.getText().isEmpty() || textApiKey.getText().equals(EMPTY_API_KEY)) {
                    textApiKey.setForeground(Color.GRAY);
                }
                else {
                    if (SmurfyImportExport.isValidApiKey(textApiKey.getText())) {
                        textApiKey.setForeground(Color.GREEN.darker());
                        if (textApiKey.getText().equals(lastKey))
                            return;
                        lastKey = textApiKey.getText();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                SmurfyImportExport action = null;
                                try {
                                    action = new SmurfyImportExport(textApiKey.getText(), decoder);
                                    List<LoadoutBase<?>> mechbay = action.listMechBay(ProgramInit.lsml().xBar);

                                    if (rememberKey.isSelected()) {
                                        preferences.remeberAPIKey(textApiKey.getText());
                                    }

                                    clearModel();
                                    for (LoadoutBase<?> loadout : mechbay) {
                                        model.addRow(new Object[] { false, loadout });
                                    }
                                    model.fireTableDataChanged();
                                    dialog.pack();
                                    dialog.setVisible(true);
                                }
                                catch (IOException e) {
                                    JOptionPane.showMessageDialog(parent,
                                            "Unable to retrieve mechbay from Smurfy's.\nError: " + e.getMessage());
                                }
                            }
                        });
                    }
                    else {
                        textApiKey.setForeground(Color.RED);
                    }
                }
            }
        });
        textApiKey.setCaretPosition(textApiKey.getText().length());

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent aArg0) {
                int selected = 0;
                for (int row = 0; row < model.getRowCount(); ++row) {
                    if ((boolean) model.getValueAt(row, 0)) {
                        selected++;
                    }
                }
                if (selected == model.getRowCount()) {
                    checkAllBox.setSelected(true);
                }
                else if (selected == 0) {
                    checkAllBox.setSelected(false);
                }
            }
        });

        checkAllBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                for (int row = 0; row < model.getRowCount(); row++) {
                    model.setValueAt(checkAllBox.isSelected(), row, 0);
                }
                model.fireTableDataChanged();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                dialog.dispose();
            }
        });

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                for (int i = 0; i < model.getRowCount(); ++i) {
                    if ((boolean) model.getValueAt(i, 0)) {
                        ProgramInit.lsml().garageOperationStack.pushAndApply(new OpAddToGarage(ProgramInit.lsml()
                                .getGarage(), (LoadoutStandard) model.getValueAt(i, 1)));
                    }
                }
                dialog.dispose();
            }
        });

        rememberKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aE) {
                if (rememberKey.isSelected()) {
                    int ans = JOptionPane
                            .showConfirmDialog(parent,
                                    "Your API key will be stored in plain text on your computer.\n"
                                            + "Any one with access to your files can read it.\n\n"
                                            + "Do you wish to continue?", "Remember API Key?",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ans != JOptionPane.YES_OPTION) {
                        rememberKey.setSelected(false);
                    }
                    else {
                        preferences.remeberAPIKey(textApiKey.getText());
                    }
                }
                else {
                    preferences.remeberAPIKey(null);
                }
            }
        });

        SwingHelpers.hypertextLink(apiLink, SmurfyImportExport.CREATE_API_KEY_URL, "Click here to get your API key!");
        dialog.pack();
        dialog.setVisible(true);
    }
}
