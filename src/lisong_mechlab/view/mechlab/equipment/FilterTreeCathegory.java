package lisong_mechlab.view.mechlab.equipment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeModelEvent;

import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.preferences.UiPreferences;
import lisong_mechlab.view.preferences.UiPreferences.Message;

public abstract class FilterTreeCathegory<T> extends DefaultTreeCathegory<T> implements Reader {
	protected final GarageTree	garageTree;
	private String				nameFilter			= "";
	private boolean				wasExpandedBeforeFilter;
	private final List<T>		filteredChildren	= new ArrayList<>();
	private boolean				filterDirty			= true;

	public FilterTreeCathegory(MessageXBar aXBar, String aName, TreeCathegory aParent, GarageTreeModel aModel,
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
						} else if (aFilterBar.getText().isEmpty()) {
							// Stopping filtering
							if (!wasExpandedBeforeFilter) {
								garageTree.collapsePath(getPath());
							}
						} else {
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
	public void receive(MessageXBar.Message aMsg) {
		if (aMsg instanceof UiPreferences.Message) {
			UiPreferences.Message msg = (Message) aMsg;
			if (msg.attribute == UiPreferences.UI_HIDE_SPECIAL_MECHS) {
				filterDirty = true;
				getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
			}
		}
	}

}
