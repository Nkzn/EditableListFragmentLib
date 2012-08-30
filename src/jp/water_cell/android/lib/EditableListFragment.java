package jp.water_cell.android.lib;

import java.util.ArrayList;
import java.util.List;

import jp.ne.hatena.d.shogo0809.widget.SortableListView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * 編集可能なリスト。<br>
 * {@link SimpleListItem}のリストを{@link #setArguments(Bundle)}(key: {@link SimpleListItem#KEY} )経由で渡すことで使用を開始する。<br>
 * 初期化時にargsで{@link #KEY_LIST_LAYOUT_ID}でレイアウトのIDを渡せば、{@link ArrayAdapter} のレイアウトとして利用される。<br>
 * 
 * @see https://raw.github.com/Nkzn/EditableListFragmentSample/master/src/jp/water_cell/android/app/sample/MainActivity.java
 * 
 * @author nakagawa
 * 
 */
public class EditableListFragment extends SherlockListFragment implements OnItemClickListener, OnClickListener {

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
		 *            更新の種類。{@link EditableListFragment#ADD}、 {@link EditableListFragment#EDIT}、 {@link EditableListFragment#DEL}、
		 *            {@link EditableListFragment#SORT}のいずれか
		 * @param modifiedItem
		 *            変更があった項目(SORT時はnull)
		 */
		void onListChanged(List<SimpleListItem> items, String tag, int editType, SimpleListItem modifiedItem);
	}

	/**
	 * リストのレイアウトを{@link Bundle#putInt(String, int)}で指定するためのキー<br/>
	 * カスタムレイアウトを用意する場合、必ず{@link TextView}のidをandroid.R.id.text1にすること<br/>
	 * デフォルト値：android.R.layout.simple_list_item_1
	 */
	public static final String KEY_LIST_LAYOUT_ID = EditableListFragment.class.getName() + "list_layout_id";

	/** 追加 */
	public static final int ADD = 0;

	/** 編集 */
	public static final int EDIT = 1;

	/** 削除 */
	public static final int DEL = 2;

	/** 並べ替え */
	public static final int SORT = 3;

	/** ListViewカスタマイズ用 */
	private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;

	/** ListViewカスタマイズ用 */
	private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

	List<SimpleListItem> mItems;

	List<SimpleListItem> mCachedItems;

	EditableListItemAdapter mAdapter;

	OnListChangedListener mListener;

	String mTag;

	int mDraggingPosition = -1;

	/**
	 * コンストラクタ
	 */
	public EditableListFragment() {
		super();
		mCachedItems = new ArrayList<SimpleListItem>();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// SupportLibraryのListFragmentを快適に使うためのおまじない。
		// 参考：http://blog.nkzn.net/entry/2012/06/14/160706
		View view = inflater.inflate(R.layout.simple_sortable_list, container, false);

		ProgressBar pBar = (ProgressBar) view.findViewById(android.R.id.progress);
		LinearLayout pframe = (LinearLayout) pBar.getParent();
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);

		SortableListView listView = (SortableListView) view.findViewById(android.R.id.list);
		listView.setItemsCanFocus(false);
		RelativeLayout lFrame = (RelativeLayout) listView.getParent();
		lFrame.setId(INTERNAL_LIST_CONTAINER_ID);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();

		if (mAdapter == null && args != null) {

			int listLayoutId = args.getInt(KEY_LIST_LAYOUT_ID, 0);
			mItems = args.getParcelableArrayList(SimpleListItem.KEY);

			if (mItems == null) {
				mItems = new ArrayList<SimpleListItem>();
			}

			mAdapter = new EditableListItemAdapter(getActivity(), listLayoutId == 0 ? android.R.layout.simple_list_item_1 : listLayoutId, mItems);

			setListAdapter(mAdapter);
		}

		SortableListView listView = (SortableListView) getListView();
		listView.setOnItemClickListener(this);
		listView.setDragListener(new DragListener());
		listView.setSortable(true);

		Button btnAdd = (Button) view.findViewById(R.id.btn_add);
		btnAdd.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btn_add) {
			onClickPlusOne();
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

	/**
	 * {@link OnListChangedListener}をセットする
	 * 
	 * @param listener
	 */
	public void setOnListChangedListener(OnListChangedListener listener) {
		mListener = listener;
	}

	/**
	 * オレオレタグをセットする（{@link Fragment#getTag()}で取れるものとは別）
	 * 
	 * @param tag
	 */
	public void setTag(String tag) {
		mTag = tag;
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
		replaceItems(mCachedItems != null ? mCachedItems : new ArrayList<SimpleListItem>());
	}

	private void replaceItems(List<SimpleListItem> items) {
		mItems.clear();
		mItems.addAll(items);

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

						String text = etInput.getText() == null ? "" : etInput.getText().toString();

						SimpleListItem addItem = new SimpleListItem(null, text);

						mItems.add(addItem);

						if (mListener != null) {
							mListener.onListChanged(mItems, getAvailableTag(), ADD, addItem);
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

						String text = etInput.getText() == null ? "" : etInput.getText().toString();

						SimpleListItem editedItem = new SimpleListItem(item.getId(), text);

						mItems.set(position, editedItem);

						if (mListener != null) {
							mListener.onListChanged(mItems, getAvailableTag(), EDIT, editedItem);
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
							mListener.onListChanged(mItems, getAvailableTag(), DEL, item);
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

	public String getAvailableTag() {
		return mTag != null ? mTag : getTag(); // オレオレタグがなければ本家を返す
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

			View view = super.getView(position, convertView, parent);

			final String title = item.getTitle();

			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			text1.setText(TextUtils.isEmpty(title) ? "" : title);

			return view;
		}
	}

	/**
	 * {@link SortableListView}のドラッグ操作を受け取るリスナクラス
	 * 
	 * @author shogogg
	 * 
	 */
	class DragListener extends SortableListView.SimpleDragListener {
		@Override
		public int onStartDrag(int position) {
			mCachedItems = new ArrayList<SimpleListItem>(mItems); // キャッシュを保存

			mDraggingPosition = position;
			getListView().invalidateViews();
			return position;
		}

		@Override
		public int onDuringDrag(int positionFrom, int positionTo) {
			if (positionFrom < 0 || positionTo < 0 || positionFrom == positionTo) {
				return positionFrom;
			}
			int i;
			if (positionFrom < positionTo) {
				final int min = positionFrom;
				final int max = positionTo;
				final SimpleListItem data = mItems.get(min);
				i = min;
				while (i < max) {
					mItems.set(i, mItems.get(++i));
				}
				mItems.set(max, data);
			} else if (positionFrom > positionTo) {
				final int min = positionTo;
				final int max = positionFrom;
				final SimpleListItem data = mItems.get(max);
				i = max;
				while (i > min) {
					mItems.set(i, mItems.get(--i));
				}
				mItems.set(min, data);
			}
			mDraggingPosition = positionTo;
			getListView().invalidateViews();
			return positionTo;
		}

		@Override
		public boolean onStopDrag(int positionFrom, int positionTo) {

			if (mListener != null) {
				mListener.onListChanged(mItems, getAvailableTag(), SORT, null);
			}

			mDraggingPosition = -1;
			getListView().invalidateViews();
			return super.onStopDrag(positionFrom, positionTo);
		}
	}
}
