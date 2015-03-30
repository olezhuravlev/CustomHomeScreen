package pro.got4.customhomescreen;

import java.io.File;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import pro.got4.customhomescreen.AppsListActivity.AppsListLoader.AppEntry;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Activity allows to select packages and returns the array of serializable
 * container classes.
 *
 */
public class AppsListActivity extends Activity implements
		LoaderManager.LoaderCallbacks<List<AppEntry>>, OnMenuItemClickListener {

	// Key for passing array of packages through intent's extras.
	public static final String SELECTED_PACKAGES_KEY = "pro.got4.customhomescreen.selected_packages";
	private final String CHECKED_KEY = "checked";

	// Some ids.
	public static final int REQUEST_CODE = 100;

	// ListView for showing installed packages.
	private ListView mListView;

	// This is the Adapter being used to display the list's data.
	private AppListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.apps_list);

		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(this);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		progressBar.setLayoutParams(lp);
		progressBar.setIndeterminate(true);

		mListView = (ListView) findViewById(R.id.lvMain);
		mListView.setEmptyView(progressBar);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		// ListView's content adapter.
		mAdapter = new AppListAdapter(this);

		if (savedInstanceState == null) {

			// Restoring packages selection from string JSON description.
			Bundle bundle = getIntent().getExtras();
			String selectedPackages = bundle
					.getString(MainActivity.SAVED_PACKAGES_KEY);

			if (selectedPackages != null) {
				mAdapter.setCheckedPackages(selectedPackages);
			}

		} else {

			// Restoring packages selection after reconfiguration from boolean
			// array.
			boolean[] checked = savedInstanceState.getBooleanArray(CHECKED_KEY);
			mAdapter.setCheckedArray(checked);
		}

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, savedInstanceState, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Place an action bar item for searching.
		// add(int groupId, int itemId, int order, int titleRes);
		MenuItem item = menu.add(0, 0, 0, R.string.ok);
		item.setIcon(android.R.drawable.ic_menu_save);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		item.setOnMenuItemClickListener(this);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Let's show, how mane items selected.
		MenuItem item = menu.findItem(0);

		boolean listIsEmpty = mAdapter.isEmpty();
		item.setVisible(!listIsEmpty);

		if (!listIsEmpty) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle(getString(R.string.appsSelected).concat(" ")
					.concat(String.valueOf(mAdapter.getCheckedCount())));
		}

		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		// Return serialized array of selected packages with their icons.
		Serializable[] checkedPackages = mAdapter.getCheckedPackagesArray();

		Intent data = new Intent();
		data.putExtra(SELECTED_PACKAGES_KEY, checkedPackages);

		setResult(RESULT_OK, data);

		finish();

		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		// Saving selected item's array for reconfiguration.
		boolean[] checked = mAdapter.getCheckedArray();
		outState.putBooleanArray(CHECKED_KEY, checked);

		super.onSaveInstanceState(outState);
	}

	/**
	 * Adapter that provides data for showing in ListView.
	 *
	 */
	public class AppListAdapter extends ArrayAdapter<AppEntry> implements
			OnItemClickListener {

		// Array of checked items.
		private boolean[] checked;

		// JSON description of checked packages.
		private String checkedPackages;

		private final LayoutInflater inflater;

		public AppListAdapter(Context context) {

			super(context, R.layout.apps_list_item);

			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/**
		 * Sets data for the adapter.
		 * 
		 * @param data
		 */
		public void setData(List<AppEntry> data) {

			clear();

			if (data == null) {

				checked = null;

			} else {

				addAll(data);

				// If we don't have checked items array so it's created.
				if (checkedPackages == null || checkedPackages.isEmpty()) {

					if ((checked == null) || (checked.length != data.size()))
						checked = new boolean[data.size()];

				} else {

					// If we have string of JSON description of selected
					// packages so we can recreate checked items array.
					checked = new boolean[data.size()];

					try {

						JSONArray packagesArr = new JSONArray(checkedPackages);
						for (int i = 0; i < data.size(); i++) {

							AppEntry appEntry = data.get(i);

							String packageName = appEntry.getPackageName();
							boolean nameEquals = false;
							for (int j = 0; j < packagesArr.length(); j++) {

								String packageNameJSON = (String) packagesArr
										.get(j);

								if (packageName.equals(packageNameJSON)) {
									nameEquals = true;
									break;
								}

							}
							checked[i] = nameEquals;
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// Clicking any place on item results checking item.
			checked[position] = !checked[position];
			((CheckBox) view.findViewById(R.id.checkBox))
					.setChecked(checked[position]);

			invalidateOptionsMenu();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Populate new items in the list.

			View view = null;

			if (convertView == null) {
				view = inflater.inflate(R.layout.apps_list_item, parent, false);
			} else {
				view = convertView;
			}

			AppEntry item = getItem(position);

			((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item
					.getIcon());
			((TextView) view.findViewById(R.id.text)).setText(item.getLabel());
			((CheckBox) view.findViewById(R.id.checkBox))
					.setChecked(checked[position]);

			return view;
		}

		/**
		 * Returns array of checked items positions.
		 * 
		 * @return
		 */
		public boolean[] getCheckedArray() {
			return checked;
		}

		/**
		 * Sets string JSON description of checked packages.
		 * 
		 * @param checkedPackages
		 */
		public void setCheckedPackages(String checkedPackages) {

			this.checkedPackages = checkedPackages;
		}

		/**
		 * Sets array of checked items position.
		 * 
		 * @param checked
		 */
		public void setCheckedArray(boolean[] checked) {
			this.checked = checked;
		}

		/**
		 * Return number of checked positions.
		 * 
		 * @return
		 */
		public int getCheckedCount() {

			int cntr = 0;
			for (int i = 0; i < checked.length; i++) {

				if (checked[i] == true)
					++cntr;
			}

			return cntr;
		}

		/**
		 * Return array of serializable objects, keeping description of selected
		 * packages.
		 * 
		 * @return
		 */
		public AppEntrySerializable[] getCheckedPackagesArray() {

			int checkedCount = getCheckedCount();
			AppEntrySerializable[] arr = new AppEntrySerializable[checkedCount];

			int currIdx = 0;
			for (int i = 0; i < checked.length; i++) {

				if (checked[i] == true) {

					AppEntry appEntry = getItem(i);

					arr[currIdx++] = new AppEntrySerializable(
							appEntry.getPackageName(), appEntry.getLabel(),
							appEntry.getIcon());
				}
			}

			return arr.clone();
		}
	} // public class AppListAdapter

	// Called after AsyncTaskLoader starts created.
	@Override
	public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		return new AppsListLoader(this);
	}

	// Called after AsyncTaskLoader finished its work (and after every
	// reconfiguration that happens later).
	@Override
	public void onLoadFinished(Loader<List<AppEntry>> loader,
			List<AppEntry> data) {

		// Set the new data in the adapter.
		mAdapter.setData(data);

		invalidateOptionsMenu();
	}

	// Called in case AsyncTaskLoader was interrupted.
	@Override
	public void onLoaderReset(Loader<List<AppEntry>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}

	/**
	 * Comparator for alphabetical comparison of application entry objects.
	 */
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {

		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {
			return sCollator.compare(object1.getLabel(), object2.getLabel());
		}
	};

	/**
	 * Asynchronous loader for getting installed applications info from
	 * PackageManager.
	 * 
	 * The feature is that some of functions called in UI thread, but
	 * loadInBackground() performs in working thread.
	 *
	 */
	public static class AppsListLoader extends AsyncTaskLoader<List<AppEntry>> {

		final PackageManager mPackageManager;

		private List<AppEntry> mApps;

		public AppsListLoader(Context context) {

			super(context);

			// Retrieve the package manager for later use.
			mPackageManager = getContext().getPackageManager();
		}

		// Called just before the loading starts. Performs in UI thread.
		@Override
		protected void onStartLoading() {

			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			boolean configChange = false;

			if (takeContentChanged() || mApps == null || configChange) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}

		}

		// This function is called in a background thread and should generate a
		// new set of data to be published by the loader.
		@Override
		public List<AppEntry> loadInBackground() {

			// Retrieve all known applications.
			List<ApplicationInfo> apps = mPackageManager
					.getInstalledApplications(PackageManager.GET_META_DATA);

			if (apps == null) {
				apps = new ArrayList<ApplicationInfo>();
			}

			final Context context = getContext();

			// Create corresponding array of entries and load their labels.
			List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
			for (int i = 0; i < apps.size(); i++) {

				// if (i > 20)
				// break;// TODO

				// Corresponds to information collected from the manifest.
				ApplicationInfo appInfo = apps.get(i);
				if (mPackageManager
						.getLaunchIntentForPackage(appInfo.packageName) != null) {

					AppEntry appEntry = new AppEntry(this, appInfo);
					appEntry.loadLabel(context);

					entries.add(appEntry);
				}
			}

			// Sorting the list.
			Collections.sort(entries, ALPHA_COMPARATOR);

			return entries;

		}

		// Called when there is new data to deliver to the client. Performs in
		// UI thread.
		@Override
		public void deliverResult(List<AppEntry> apps) {

			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					onReleaseResources(apps);
				}
			}
			List<AppEntry> oldApps = mApps;
			mApps = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}

			// At this point we can release the resources associated with
			// oldApps if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		// Called when the loader stops. Performs in UI thread.
		@Override
		protected void onStopLoading() {

			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		// Called when the loading cancelled. Performs in UI thread.
		@Override
		public void onCanceled(List<AppEntry> apps) {

			super.onCanceled(apps);

			// At this point we can release the resources associated with apps
			// if needed.
			onReleaseResources(apps);
		}

		// Called when the loader completely reseted. Performs in UI thread.
		@Override
		protected void onReset() {

			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with apps
			// if needed.
			if (mApps != null) {
				onReleaseResources(mApps);
				mApps = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<AppEntry> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}

		/**
		 * Container class for holding the per-item data in the Loader. Not
		 * intended for serialization.
		 */
		static class AppEntry {

			private final AppsListLoader mLoader;
			private final ApplicationInfo mAppInfo;
			private final File mApkFile;
			private String mLabel;
			private String mPackageName;
			private Drawable mIcon;
			private boolean mMounted;

			public AppEntry(AppsListLoader loader, ApplicationInfo info) {
				mLoader = loader;
				mAppInfo = info;
				mApkFile = new File(info.sourceDir);
			}

			// Return package's label.
			public String getLabel() {
				return mLabel;
			}

			// Return package's name.
			public String getPackageName() {
				return mPackageName;
			}

			// Return package's icon.
			public Drawable getIcon() {

				if (mIcon == null) {

					if (mApkFile.exists()) {
						mIcon = mAppInfo.loadIcon(mLoader.mPackageManager);
						return mIcon;
					} else {
						mMounted = false;
					}

				} else if (!mMounted) {
					// If the app wasn't mounted but is now mounted, reload
					// its icon.
					if (mApkFile.exists()) {
						mMounted = true;
						mIcon = mAppInfo.loadIcon(mLoader.mPackageManager);
						return mIcon;
					}
				} else {
					return mIcon;
				}

				return mLoader.getContext().getResources()
						.getDrawable(android.R.drawable.sym_def_app_icon);
			}

			// Loads the current item's label and package name.
			void loadLabel(Context context) {

				if (mLabel == null || !mMounted) {

					if (!mApkFile.exists()) {

						mMounted = false;
						mLabel = mAppInfo.packageName;
						mPackageName = mAppInfo.packageName;

					} else {

						mMounted = true;
						CharSequence label = mAppInfo.loadLabel(context
								.getPackageManager());
						mLabel = label != null ? label.toString()
								: mAppInfo.packageName;

						mPackageName = mAppInfo.packageName;
					}
				}
			}

			@Override
			public String toString() {
				return mLabel;
			}
		}
	}
}