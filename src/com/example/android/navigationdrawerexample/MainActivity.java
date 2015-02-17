/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigationdrawerexample;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This example illustrates a common usage of the DrawerLayout widget in the
 * Android support library.
 * <p/>
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect
 * presses of the action bar's Up affordance as a signal to open and close the
 * navigation drawer. The ActionBarDrawerToggle facilitates this behavior. Items
 * within the drawer should fall into one of two categories:
 * </p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic
 * policies as list or tab navigation in that a view switch does not create
 * navigation history. This pattern should only be used at the root activity of
 * a task, leaving some form of Up navigation active for activities further down
 * the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an
 * alternate parent for Up navigation. This allows a user to jump across an
 * app's navigation hierarchy at will. The application should treat this as it
 * treats Up navigation from a different task, replacing the current task stack
 * using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * <p/>
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows
 * the pattern established by the Action Bar that navigation should be to the
 * left and actions to the right. An action should be an operation performed on
 * the current contents of the window, for example enabling or disabling a data
 * overlay on top of the current content.
 * </p>
 */
public class MainActivity extends Activity {

	// Работает как контейнер верхнего уровня, позволяющий вью выдвижного меню
	// вытаскиваться с края окна.
	// Позиция и лайаут выдвижного меню контролируются с помощью атрибута
	// android:layout_gravity подчиненных вью, соответствующих стороне вью, с
	// которой должно появляться меню: left или right (или start/end на тех
	// версиях платформы, которые поддерживают такое направление лайаута).
	// Чтобы использовать DrawerLayout расположи вью основного контента в
	// качестве первого подчиненного с высотой и шириной установленными как
	// match_parent. Добавь выдвижные элементы в качестве подчиненных вью после
	// вью основного контента и установи значение layout_gravity соответствующим
	// образом. Обычно для выдвижных элементов используются значение
	// match_parent для высоты и фиксированная ширина.

	// В соответствии с руководством по дизайну Андроид любые выдвижные
	// элементы, позиционированных left/start должны всегда использоваться для
	// навигации по приложению, в то время как любые выдвижные элементы,
	// расположенные right/end должны всегда содержать действия, выполняемые над
	// текущим содержимым. Это сохраняет такую же навигация слева действия
	// справа структуру, представленную в Action Bar и в других местах.

	// DrawerLayout.DrawerListener может использоваться для отслеживания
	// состояния и движения выдвижных вью. Избегайте дорогих операций, таких как
	// размещение элементов в течение анимации, т.к. это может привести к
	// задержкам; старайтеся выполнять дорогие операции в течении состояния
	// STATE_IDLE.
	// DrawerLayout.SimpleDrawerListener предоставляет определенные по умолчанию
	// безоперационные реализации каждого метода обратного вызова.
	private DrawerLayout mDrawerLayout;

	// Левое меню.
	private ListView mLeftDrawerList;

	// Правое меню.
	private ListView mRightDrawerList;

	// Связывает работу DrawerLayout и ActionBar.
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mPlanetTitles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Заголовок активности.
		mTitle = mDrawerTitle = getTitle();

		// Массив названий планет.
		mPlanetTitles = getResources().getStringArray(R.array.planets_array);

		// Родительский выдвижной элемент.
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Списки для левого и правого меню.
		mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);
		mRightDrawerList = (ListView) findViewById(R.id.right_drawer);

		// Тень по краю панели.
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_1,
				GravityCompat.START);

		// Адаптер для левого меню.
		mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));

		// Адаптер для правого меню.
		mRightDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));

		// Слушатели для элементов меню.
		mLeftDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mRightDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Отображение маленькой иконки в виде слева экрана между краем экрана и
		// кнопкой открытия бокового меню.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Разрешает по нажатию кнопки открывать боковое меню.
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle связывает соответствующие действия между
		// скользящим элементом и иконкой в ActionBar.
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* Родительский выдвижной элемент */
		R.drawable.ic_drawer_2, /* Изображение для замены знака вставки. */
		R.string.drawer_open, /* Описание открытого меню */
		R.string.drawer_close /* Описане закрытого меню */
		) {
			// События выдвижного элемента.
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		// Установка слушателя родительского выдвижного элемента.
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawerList);
		menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available,
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = new PlanetFragment();
		Bundle args = new Bundle();
		args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mLeftDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mLeftDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
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
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment that appears in the "content_frame", shows a planet
	 */
	public static class PlanetFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "planet_number";

		public PlanetFragment() {
			// Empty constructor required for fragment subclasses
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_planet,
					container, false);
			int i = getArguments().getInt(ARG_PLANET_NUMBER);
			String planet = getResources()
					.getStringArray(R.array.planets_array)[i];

			int imageId = getResources().getIdentifier(
					planet.toLowerCase(Locale.getDefault()), "drawable",
					getActivity().getPackageName());
			((ImageView) rootView.findViewById(R.id.image))
					.setImageResource(imageId);
			getActivity().setTitle(planet);
			return rootView;
		}
	}
}