package jp.water_cell.android.lib;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class EditableListFragment extends ListFragment implements OnItemClickListener {

	public interface OnListChangedListener {
		void onListChanged(List<ListItem> items, String tag);
	}

	/**
	 * リストのレイアウトを{@link Bundle#putInt(String, int)}で指定するためのキー<br/>
	 * カスタムレイアウトを用意する場合、必ず{@link TextView}のidをandroid.R.id.text1にすること<br/>
	 * デフォルト値：android.R.layout.simple_list_item_1
	 */
	public static final String KEY_LIST_LAYOUT_ID = EditableListFragment.class.getName() + "list_layout_id";

	private static final String LISTITEM_ID_PLUSONE = EditableListFragment.class.getName() + "plus_one";

	List<ListItem> mItems;

	EditableListItemAdapter mAdapter;

	OnListChangedListener mListener;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAdapter == null) {
			Bundle args = getArguments();
			mItems = args.getParcelableArrayList(ListItem.KEY);
			mItems.add(new ListItem(LISTITEM_ID_PLUSONE, getString(R.string.add_item)));

			int listLayoutId = args.getInt(KEY_LIST_LAYOUT_ID, 0);

			mAdapter = new EditableListItemAdapter(getActivity(), listLayoutId == 0 ? android.R.layout.simple_list_item_1 : listLayoutId, mItems);

			setListAdapter(mAdapter);
			getListView().setOnItemClickListener(this);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		final ListItem item = (ListItem) parent.getItemAtPosition(position);
		final String itemId = item.getId();

		if (TextUtils.equals(itemId, LISTITEM_ID_PLUSONE)) {
			onClickPlusOne();
		} else {
			onClickItem(position);
		}

	}

	private void onClickPlusOne() {
		new AlertDialog.Builder(getActivity()).setTitle(R.string.add_item).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	private void onClickItem(int position) {
		final ListItem original = mItems.get(position);

		new AlertDialog.Builder(getActivity()).setItems(R.array.edit_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					onClickEdit(original);
					break;
				case 1:
					onClickDelete(original);
					break;
				}
			}
		}).show();
	}

	private void onClickEdit(ListItem _item) {
		final ListItem item = _item;
		String title = item.getTitle();
		String dialogTitle = getString(R.string.edit_title, (TextUtils.isEmpty(title) ? "" : title));

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.input_dialog, null);
		final EditText etInput = (EditText) view.findViewById(R.id.et_input);
		etInput.setText((TextUtils.isEmpty(title) ? "" : title));

		new AlertDialog.Builder(getActivity()).setTitle(dialogTitle).setView(view)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						item.setTitle(etInput.getText().toString());
						mAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void onClickDelete(ListItem item) {
		// TODO Auto-generated method stub

	}

	public void setOnListChangedListener(OnListChangedListener listener) {
		mListener = listener;
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
