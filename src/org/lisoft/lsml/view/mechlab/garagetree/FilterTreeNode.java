package org.lisoft.lsml.view.mechlab.garagetree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeModelEvent;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.view.preferences.UiPreferences;

public abstract class FilterTreeNode<T> extends DefaultTreeNode<T> implements MessageReceiver {
    protected final GarageTree garageTree;
    private String             nameFilter       = "";
    private boolean            wasExpandedBeforeFilter;
    private final List<T>      filteredChildren = new ArrayList<>();
    private boolean            filterDirty      = true;

    public FilterTreeNode(MessageXBar aXBar, String aName, TreeNode aParent, GarageTreeModel aModel,
            final JTextField aFilterBar, GarageTree aGarageTree) {
        super(aName, aParent, aModel);
        aXBar.attach(this);
        garageTree = aGarageTree;
        wasExpandedBeforeFilter = garageTree.isExpanded(getPath());
        if (aFilterBar != null) {
            aFilterBar.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent aArg0) {
                    if (!nameFilter.equals(aFilterBar.getText())) {
                        if (nameFilter.isEmpty()) {
                            // Starting filtering
                            wasExpandedBeforeFilter = garageTree.isExpanded(getPath());
                        }
                        else if (aFilterBar.getText().isEmpty()) {
                            // Stopping filtering
                            if (!wasExpandedBeforeFilter) {
                                garageTree.collapsePath(getPath());
                            }
                        }
                        else {
                            // Ongoing filtering
                            garageTree.expandPath(getPath());
                        }

                        nameFilter = aFilterBar.getText().toLowerCase();
                        filterDirty = true;
                        getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
                    }
                }
            });
        }
        filterDirty = true;
    }

    public void setDirtyBit() {
        filterDirty = true;
    }

    protected String getFilterString() {
        return nameFilter;
    }

    private List<T> filterList() {
        if (filterDirty) {
            filteredChildren.clear();
            for (T t : children) {
                if (filter(t))
                    filteredChildren.add(t);
            }
            filterDirty = false;
        }
        return filteredChildren;
    }

    /**
     * @param t
     * @return <code>true</code> if the argument should be visible.
     */
    abstract protected boolean filter(T t);

    @Override
    public int getChildCount() {
        return filterList().size();
    }

    @Override
    public int getIndex(Object aChild) {
        return filterList().indexOf(aChild);
    }

    @Override
    public Object getChild(int anIndex) {
        return filterList().get(anIndex);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof UiPreferences.PreferencesMessage) {
            UiPreferences.PreferencesMessage msg = (UiPreferences.PreferencesMessage) aMsg;
            if (msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS) {
                filterDirty = true;
                getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
            }
        }
    }

}
