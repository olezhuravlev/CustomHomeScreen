package pro.got4.customhomescreen;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	// public final static String SELECTED_PACKAGES_KEY = "selected_packages";
	public final static String SAVED_PACKAGES_KEY = "saved_packages";

	private final static int BUTTON_ADD_ID = 20;

	private final static int CONTENT_PAGE_ID = 100;
	private final static int ABOUT_PAGE_ID = 200;

	// Parental layout for the drawer.
	private DrawerLayout mDrawerLayout;

	// Left menu.
	private ListView mLeftDrawerList;

	// Left menu's items titles array.
	private String[] mLeftItemsTitles;

	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Activity and drawer has the same title.
		mTitle = mDrawerTitle = getTitle();

		mLeftItemsTitles = getResources().getStringArray(
				R.array.left_items_array);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Setting shadow on the edge of the drawer's panel.
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// This is a small icon on the top left between screen's edge and left
		// menu opening button.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Enables open side menu (drawer) by pressing the button.
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle performs connection between drawer and icon in
		// the action bar.
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			// Actions.
			public void onDrawerClosed(View view) {

				getActionBar().setTitle(mTitle);

				// Causes call to onCreateOptionsMenu and
				// onPrepareOptionsMenu() in order to hide/show action bar
				// buttons.
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {

				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};

		// Parental element listener.
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		SharedPreferences sPref = getPreferences(MODE_PRIVATE);
		String savedPackages = sPref.getString(SAVED_PACKAGES_KEY, "");

		if (savedInstanceState == null) {

			Bundle bundle = new Bundle();
			if (!savedPackages.isEmpty()) {

				try {

					JSONArray packagesArr = new JSONArray(savedPackages);
					int count = packagesArr.length();

					AppEntrySerializable[] arr = new AppEntrySerializable[count];
					PackageManager packageManager = getPackageManager();

					for (int i = 0; i < count; i++) {

						String packageName = packagesArr.get(i).toString();

						try {

							ApplicationInfo appInfo = packageManager
									.getApplicationInfo(packageName,
											PackageManager.GET_META_DATA);
							CharSequence label = appInfo
									.loadLabel(packageManager);
							Drawable icon = appInfo.loadIcon(packageManager);
							arr[i] = new AppEntrySerializable(packageName,
									label.toString(), icon);

						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
					}

					bundle.putSerializable(
							AppsListActivity.SELECTED_PACKAGES_KEY, arr);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			selectItem(CONTENT_PAGE_ID, bundle);
		}
	}

	private void saveData() {

		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragmentAbout = fragmentManager.findFragmentByTag(String
				.valueOf(ABOUT_PAGE_ID));

		if (fragmentAbout != null)
			return;

		String selectedPackages = getSelectedPackages();

		SharedPreferences sPref = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();

		editor.putString(SAVED_PACKAGES_KEY, selectedPackages);
		editor.commit();
	}

	private String getSelectedPackages() {

		FragmentManager fragmentManager = getFragmentManager();
		PageFragment contentFragment = (PageFragment) fragmentManager
				.findFragmentByTag(String.valueOf(CONTENT_PAGE_ID));

		if (contentFragment == null)
			return null;

		return contentFragment.getAdapterData();
	}

	@Override
	public void onPause() {
		super.onPause();
		saveData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Menu represents action bar.
		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragmentAbout = fragmentManager.findFragmentByTag(String
				.valueOf(ABOUT_PAGE_ID));

		// int aboutId;
		// if (existingAboutPage != null)
		// aboutId = existingAboutPage.getId();
		// existingAboutPage.getTag();
		//
		// int inStack = fragmentManager.getBackStackEntryCount();
		// int inStackId;
		// if (inStack > 0) {
		// BackStackEntry backStackEntry = fragmentManager
		// .getBackStackEntryAt(inStack - 1);
		// inStackId = backStackEntry.getId();
		// }
		// existingAboutPage.getId()

		if (fragmentAbout == null) {

			// If the drawer is opened, hide action items related to the content
			// view.
			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawerList);
			menu.findItem(R.id.action_about).setVisible(!drawerOpen);

			// Left menu's adapter.
			mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_item, mLeftItemsTitles));

			mLeftDrawerList
					.setOnItemClickListener(new DrawerItemClickListener());

		} else {

			// When "About" page is shown then "About" button isn't visible.
			menu.findItem(R.id.action_about).setVisible(false);

			String[] leftItemsTitles_about = getResources().getStringArray(
					R.array.left_items_array_about);
			mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_item, leftItemsTitles_about));

			mLeftDrawerList
					.setOnItemClickListener(new ListView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {

							getFragmentManager().popBackStack();

							mDrawerLayout.closeDrawer(mLeftDrawerList);
						}
					});
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action buttons.
		switch (item.getItemId()) {
		case R.id.action_about:

			// "About" page showing.
			selectItem(ABOUT_PAGE_ID, null);

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// Menu key opens the drawer.
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {

			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawerList);

			if (drawerOpen) {
				mDrawerLayout.closeDrawer(mLeftDrawerList);
			} else {
				mDrawerLayout.openDrawer(mLeftDrawerList);
			}

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case AppsListActivity.REQUEST_CODE:

			switch (resultCode) {

			case RESULT_OK:

				Serializable checkedPackages = data
						.getSerializableExtra(AppsListActivity.SELECTED_PACKAGES_KEY);

				Bundle bundle = new Bundle();
				bundle.putSerializable(AppsListActivity.SELECTED_PACKAGES_KEY,
						checkedPackages);

				selectItem(CONTENT_PAGE_ID, bundle);
			}

			break;

		default:

			break;
		}

	}

	/**
	 * The click listener for ListView in the navigation drawer.
	 * 
	 * @author programmer
	 *
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// We have the only button in the drawer, so just call page by id.
			selectItem(BUTTON_ADD_ID, null);
		}
	}

	/**
	 * Shows fragment according to the drawer's menu position.
	 * 
	 * @param itemId
	 */
	private void selectItem(int itemId, Bundle bundle) {

		Fragment fragment;
		FragmentManager fragmentManager;
		FragmentTransaction ft;

		// Update the main content by replacing fragments.
		switch (itemId) {

		case BUTTON_ADD_ID:

			Intent intent = new Intent(this, AppsListActivity.class);
			intent.putExtra(SAVED_PACKAGES_KEY, getSelectedPackages());
			startActivityForResult(intent, AppsListActivity.REQUEST_CODE);

			break;

		default:

			// When we open fragment we just pass the item id down to the
			// fragment.
			fragment = new PageFragment();

			if (bundle == null)
				bundle = new Bundle();

			bundle.putInt(PageFragment.ITEM_ID, itemId);
			fragment.setArguments(bundle);

			fragmentManager = getFragmentManager();

			ft = fragmentManager.beginTransaction();
			ft.replace(R.id.content_frame, fragment, String.valueOf(itemId));

			if (itemId == ABOUT_PAGE_ID)
				ft.addToBackStack(null);

			ft.commit();
		}

		mDrawerLayout.closeDrawer(mLeftDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/*
	 * When using the ActionBarDrawerToggle, we must call it during
	 * onPostCreate() and onConfigurationChanged()!
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {

		super.onPostCreate(savedInstanceState);

		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

		// Pass any configuration change to the drawer toggles.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment that appears in the frame.
	 */
	public static class PageFragment extends Fragment implements
			OnClickListener {

		public static final String ITEM_ID = "item_id";

		private int itemId;

		private Object[] checkedPackages;

		// This is the Adapter being used to display the list's data.
		private AppListAdapter mAdapter;

		public PageFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = null;

			itemId = getArguments().getInt(ITEM_ID);

			switch (itemId) {

			case ABOUT_PAGE_ID: // "About" page inflated from particular layout.

				rootView = inflater.inflate(R.layout.fragment_page_about,
						container, false);

				TextView tvEmail1 = (TextView) rootView
						.findViewById(R.id.tvEmail1);
				tvEmail1.setTag(getString(R.string.email));

				TextView tvEmail2 = (TextView) rootView
						.findViewById(R.id.tvEmail2);
				tvEmail2.setTag(getString(R.string.email));

				TextView tvEmail3 = (TextView) rootView
						.findViewById(R.id.tvEmail3);
				tvEmail3.setTag(getString(R.string.email));

				tvEmail1.setOnClickListener(this);
				tvEmail2.setOnClickListener(this);
				tvEmail3.setOnClickListener(this);

				Animation animation1 = AnimationUtils.loadAnimation(
						getActivity(), R.anim.anim1);
				Animation animation3 = AnimationUtils.loadAnimation(
						getActivity(), R.anim.anim3);
				tvEmail1.startAnimation(animation1);
				tvEmail3.startAnimation(animation3);

				getActivity().invalidateOptionsMenu();

				break;

			default:

				checkedPackages = (Object[]) getArguments().getSerializable(
						AppsListActivity.SELECTED_PACKAGES_KEY);

				rootView = inflater.inflate(R.layout.fragment_page_content,
						container, false);

				ListView listView = (ListView) rootView
						.findViewById(R.id.selectedAppsListView);

				// ListView's content adapter.
				mAdapter = new AppListAdapter(getActivity());

				List<Object> listPackages;
				if (checkedPackages == null)
					listPackages = new Vector<Object>();
				else
					listPackages = new Vector<Object>(
							Arrays.asList(checkedPackages));

				mAdapter.setData(listPackages);

				listView.setAdapter(mAdapter);
				listView.setOnItemClickListener(mAdapter);

				getActivity().invalidateOptionsMenu();

				break;
			}

			return rootView;
		}

		@Override
		public void onDestroyView() {

			super.onDestroyView();

			getActivity().invalidateOptionsMenu();
		}

		@Override
		public void onClick(View v) {

			String email = (String) v.getTag();
			String copy = "";
			String subject = getString(R.string.hello);
			String message = "";

			sendEmail(getActivity(), email, copy, subject, message);
		}

		/**
		 * Returns adapter's data as string presentation of JSONArray.
		 * 
		 * @return
		 */
		public String getAdapterData() {

			JSONArray packages = new JSONArray();
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				AppEntrySerializable appDescription = (AppEntrySerializable) mAdapter
						.getItem(i);
				packages.put(appDescription.getPackageName());
			}

			return packages.toString();
		}

		private class AppListAdapter extends ArrayAdapter<Object> implements
				OnItemClickListener {

			private final LayoutInflater mInflater;

			public AppListAdapter(Context context) {

				super(context, R.layout.apps_list_item_no_chb);

				mInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			public void setData(List<Object> data) {

				clear();

				if (data != null) {
					addAll(data);
				}
			}

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				AppEntrySerializable appDescription = (AppEntrySerializable) getItem(position);

				String packageName = appDescription.getPackageName();

				PackageManager packageManager = getContext()
						.getPackageManager();
				Intent intent = packageManager
						.getLaunchIntentForPackage(packageName);

				if (intent != null)
					startActivity(intent);

				// invalidateOptionsMenu();
			}

			/**
			 * Populate new items in the list.
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				View view = null;

				if (convertView == null) {
					view = mInflater.inflate(R.layout.apps_list_item_no_chb,
							parent, false);
				} else {
					view = convertView;
				}

				AppEntrySerializable appDescription = (AppEntrySerializable) getItem(position);

				((ImageView) view.findViewById(R.id.icon))
						.setImageDrawable(appDescription.getIcon());
				((TextView) view.findViewById(R.id.text))
						.setText(appDescription.getLabel());

				return view;
			}
		} // class AppListAdapter
	}

	/**
	 * Invokes certain app for sending e-mail.
	 * 
	 * @param context
	 * @param email
	 * @param copy
	 * @param subject
	 * @param message
	 */
	public static void sendEmail(Context context, String email, String copy,
			String subject, String message) {

		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setData(Uri.parse("mailto:"));
		intent.setType("text/html");

		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
		intent.putExtra(Intent.EXTRA_CC, new String[] { copy });
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT,
				Html.fromHtml(new StringBuilder().append(message).toString()));

		context.startActivity(Intent.createChooser(intent,
				"Choose email service"));
	}
}