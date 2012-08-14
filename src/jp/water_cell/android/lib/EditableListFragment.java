package jp.water_cell.android.lib;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 編集可能なリスト。<br>
 * {@link SimpleListItem}のリストを{@link #setArguments(Bundle)}(key:
 * {@link SimpleListItem#KEY} )経由で渡すことで使用を開始する。<br>
 * 初期化時にargsで{@link #KEY_LIST_LAYOUT_ID}でレイアウトのIDを渡せば、{@link ArrayAdapter}
 * のレイアウトとして利用される。
 * 
 * @author nakagawa
 * 
 */
public class EditableListFragment extends ListFragment implements OnItemClickListener {

	/**
	 * リストへの変更を通知するリスナ
	 * 
	 * @author nakagawa
	 * 
	 */
	public interface OnListChangedListener {
		/**
		 * 
		 * @param items
		 *            更新後の{@link SimpleListItem}のリスト
		 * @param tag
		 *            どのリストか識別する用の文字列（中に入る値は{@link Fragment#getTag()}などを想定）
		 * @param editType
		 *            更新の種類。{@link EditableListFragment#ADD}、
		 *            {@link EditableListFragment#EDIT}、
		 *            {@link EditableListFragment#DEL}、
		 *            {@link EditableListFragment#SORT}のいずれか
		 */
		void onListChanged(List<SimpleListItem> items, String tag, int editType);
	}

	/**
	 * リストのレイアウトを{@link Bundle#putInt(String, int)}で指定するためのキー<br/>
	 * カスタムレイアウトを用意する場合、必ず{@link TextView}のidをandroid.R.id.text1にすること<br/>
	 * デフォルト値：android.R.layout.simple_list_item_1
	 */
	public static final String KEY_LIST_LAYOUT_ID = EditableListFragment.class.getName() + "list_layout_id";

	/**
	 * 追加
	 */
	public static final int ADD = 0;

	/**
	 * 編集
	 */
	public static final int EDIT = 1;

	/**
	 * 削除
	 */
	public static final int DEL = 2;

	/**
	 * 並べ替え
	 */
	public static final int SORT = 3;

	/**
	 * 「項目を追加」ボタン専用の{@link SimpleListItem}用ID
	 */
	private static final String LISTITEM_ID_PLUSONE = EditableListFragment.class.getName() + "plus_one";

	List<SimpleListItem> mItems;

	List<SimpleListItem> mCachedItems;

	EditableListItemAdapter mAdapter;

	OnListChangedListener mListener;

	/**
	 * コンストラクタ
	 */
	public EditableListFragment() {
		super();
		mCachedItems = new ArrayList<SimpleListItem>();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAdapter == null) {
			Bundle args = getArguments();
			mItems = args.getParcelableArrayList(SimpleListItem.KEY);
			mItems.add(new SimpleListItem(LISTITEM_ID_PLUSONE, getString(R.string.add_item)));

			int listLayoutId = args.getInt(KEY_LIST_LAYOUT_ID, 0);

			mAdapter = new EditableListItemAdapter(getActivity(), listLayoutId == 0 ? android.R.layout.simple_list_item_1 : listLayoutId, mItems);

			setListAdapter(mAdapter);
			getListView().setOnItemClickListener(this);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		final SimpleListItem item = (SimpleListItem) parent.getItemAtPosition(position);
		final String itemId = item.getId();

		if (TextUtils.equals(itemId, LISTITEM_ID_PLUSONE)) {
			onClickPlusOne();
		} else {
			onClickItem(position);
		}

	}

	/**
	 * {@link OnListChangedListener}をセットする
	 * 
	 * @param listener
	 */
	public void setOnListChangedListener(OnListChangedListener listener) {
		mListener = listener;
	}

	/**
	 * Activity側でModelの処理が成功した場合に呼ばれ、保存済みの項目が反映される
	 * 
	 * @param items
	 *            成功後の全項目
	 */
	public void performed(List<SimpleListItem> items) {
		Log.d("list", "performed(" + items + ")");
		replaceItems(items);
	}

	/**
	 * Activity側でModelの処理が失敗した場合に呼ばれ、リストの内容が元に戻る
	 */
	public void canceled() {
		Log.d("list", "canceled()");
		Log.d("list", "\tbefore:");
		Log.d("list", "\t\tmItems -> " + mItems);
		Log.d("list", "\t\tcachedItems -> " + mCachedItems);
		replaceItems(mCachedItems != null ? mCachedItems : new ArrayList<SimpleListItem>());
		Log.d("list", "\tafter:");
		Log.d("list", "\t\tmItems -> " + mItems);
		Log.d("list", "\t\tcachedItems -> " + mCachedItems);
	}

	private void replaceItems(List<SimpleListItem> items) {
		mItems.clear();
		mItems.addAll(items);
		mItems.add(new SimpleListItem(LISTITEM_ID_PLUSONE, getString(R.string.add_item)));

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void onClickPlusOne() {

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.input_dialog, null);
		final EditText etInput = (EditText) view.findViewById(R.id.et_input);

		new AlertDialog.Builder(getActivity()).setTitle(R.string.add_item).setView(view)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCachedItems = new ArrayList<SimpleListItem>(mItems);

						mItems.add(mItems.size() - 1, new SimpleListItem(null, etInput.getText().toString()));

						if (mListener != null) {
							mItems.remove(mItems.size() - 1);
							mListener.onListChanged(mItems, getTag(), ADD);
						}

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void onClickItem(int position) {
		final SimpleListItem original = mItems.get(position);

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

	private void onClickEdit(SimpleListItem _item) {
		Log.d("list", "onClickEdit");
		final SimpleListItem item = _item;
		final int position = mItems.indexOf(item);
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
						mCachedItems = new ArrayList<SimpleListItem>(mItems); // キャッシュを保存

						mItems.set(position, new SimpleListItem(item.getId(), etInput.getText().toString()));

						if (mListener != null) {
							mItems.remove(mItems.size() - 1);
							mListener.onListChanged(mItems, getTag(), EDIT);
						}

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void onClickDelete(SimpleListItem _item) {
		final SimpleListItem item = _item;

		new AlertDialog.Builder(getActivity()).setTitle(R.string.delete_confirm).setMessage(item.getTitle())
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCachedItems = new ArrayList<SimpleListItem>(mItems); // キャッシュを保存

						mItems.remove(item);

						if (mListener != null) {
							mItems.remove(mItems.size() - 1);
							mListener.onListChanged(mItems, getTag(), DEL);
						}

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	/**
	 * {@link SimpleListItem}のtitleフィールドを表示する{@link ArrayAdapter}
	 * 
	 * @author nakagawa
	 * 
	 */
	class EditableListItemAdapter extends ArrayAdapter<SimpleListItem> {

		public EditableListItemAdapter(Context context, int textViewResourceId, List<SimpleListItem> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final SimpleListItem item = getItem(position);
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
