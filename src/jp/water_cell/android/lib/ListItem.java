package jp.water_cell.android.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class ListItem implements Parcelable {

	public static final String KEY = ListItem.class.getName() + "_key";

	private String id;
	private String title;

	public ListItem(String id, String title) {
		this.id = id;
		this.title = title;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	private ListItem(Parcel source) {
		this.id = source.readString();
		this.title = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {

		@Override
		public ListItem createFromParcel(Parcel source) {
			return new ListItem(source);
		}

		@Override
		public ListItem[] newArray(int size) {
			return new ListItem[size];
		}

	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
	}
}
