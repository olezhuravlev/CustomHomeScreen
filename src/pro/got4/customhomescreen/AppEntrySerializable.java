package pro.got4.customhomescreen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Container class for holding data, describing some app.
 */
public class AppEntrySerializable implements Serializable {

	private static final long serialVersionUID = -6196766338783421212L;

	private String mPackageName;
	private String mLabel;
	private byte[] mIcon;

	public AppEntrySerializable(String packageName, String label, Drawable icon) {

		mPackageName = packageName;
		mLabel = label;

		Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		mIcon = stream.toByteArray();

	}

	/**
	 * Returns package's label.
	 * 
	 * @return
	 */
	public String getLabel() {
		return mLabel;
	}

	/**
	 * Returns package's name.
	 * 
	 * @return
	 */
	public String getPackageName() {
		return mPackageName;
	}

	/**
	 * Returns package's icon.
	 * 
	 * @return
	 */
	public Drawable getIcon() {

		ByteArrayInputStream is = new ByteArrayInputStream(mIcon);
		Drawable icon = Drawable.createFromStream(is, "drawable");

		return icon;
	}

	@Override
	public String toString() {
		return mPackageName;
	}
}