package jp.water_cell.android.lib;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EditableListFragment extends ListFragment {

	/**
	 * リストのレイアウトを{@link Bundle#putInt(String, int)}で指定するためのキー<br/>
	 * カスタムレイアウトを用意する場合、必ず{@link TextView}のidをandroid.R.id.text1にすること<br/>
	 * デフォルト値：android.R.layout.simple_list_item_1
	 */
	public static final String KEY_LIST_LAYOUT_ID = EditableListFragment.class.getName() + "list_layout_id";

	private static final String LISTITEM_ID_PLUSONE = EditableListFragment.class.getName() + "plus_one";

	List<ListItem> mItems;

	EditableListItemAdapter mAdapter;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAdapter == null) {
			Bundle args = getArguments();
			mItems = args.getParcelableArrayList(ListItem.KEY);
			mItems.add(new ListItem(LISTITEM_ID_PLUSONE, getString(R.string.plus_one)));

			int listLayoutId = args.getInt(KEY_LIST_LAYOUT_ID, 0);

			mAdapter = new EditableListItemAdapter(getActivity(), listLayoutId == 0 ? android.R.layout.simple_list_item_1 : listLayoutId, mItems);

			setListAdapter(mAdapter);
		}

	}

	class EditableListItemAdapter extends ArrayAdapter<ListItem> {

		public EditableListItemAdapter(Context context, int textViewResourceId, List<ListItem> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ListItem item = getItem(position);
			final String id = item.getId();

			View view = super.getView(position, convertView, parent);

			final String title = item.getTitle();

			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			text1.setText(TextUtils.isEmpty(title) ? "" : title);

			if (TextUtils.equals(id, LISTITEM_ID_PLUSONE)) {
				Drawable plus = getContext().getResources().getDrawable(android.R.drawable.ic_input_add);
				text1.setCompoundDrawablesWithIntrinsicBounds(plus, null, null, null);
			} else {
				text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}

			return view;
		}
	}
}
