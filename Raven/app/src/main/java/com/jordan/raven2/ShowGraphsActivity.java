package com.jordan.raven2;

import java.time.Month;
import java.util.Locale;

import android.app.ActionBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

public class ShowGraphsActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_graphs);

		// Set up the action bar.
		//final ActionBar actionBar = getActionBar();
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(mViewPager);

		ImageView menuImage = findViewById(R.id.menu_icon);
		menuImage.setOnClickListener(v -> {
			PopupMenu popupMenu = new PopupMenu(ShowGraphsActivity.this, menuImage);
			popupMenu.getMenuInflater().inflate(R.menu.show_weather, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_morning:
						ShowGraphsActivity.this.mSectionsPagerAdapter.updateTimeOfDay(0);
						break;
					case R.id.action_noon:
						ShowGraphsActivity.this.mSectionsPagerAdapter.updateTimeOfDay(1);
						break;
					case R.id.action_afternoon:
						ShowGraphsActivity.this.mSectionsPagerAdapter.updateTimeOfDay(2);
						break;
					case R.id.action_evening:
						ShowGraphsActivity.this.mSectionsPagerAdapter.updateTimeOfDay(3);
						break;
				}
				ShowGraphsActivity.this.updateTitle();
				return true;
			});
			popupMenu.show();
		});

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		//mViewPager
		//		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
		//			@Override
		//			public void onPageSelected(int position) {
		//				actionBar.setSelectedNavigationItem(position);
		//			}
		//		});

		// For each of the sections in the app, add a tab to the action bar.
		//for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
		//	actionBar.addTab(actionBar.newTab()
		//			.setText(mSectionsPagerAdapter.getPageTitle(i))
		//			.setTabListener(this));
		//}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_weather, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	protected void onStart() {
		super.onStart();

		// get the message from the intent
		//Intent intent = getIntent();
		//mSectionsPagerAdapter.startDataAcquisition(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get the message from the intent
		Intent intent = getIntent();
		mSectionsPagerAdapter.startDataAcquisition(intent);

		updateTitle();
	}

	private void updateTitle() {
		TextView title = findViewById(R.id.title);
		title.setText(mSectionsPagerAdapter.getDataSetTitle());
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private final GraphManager graphManager;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

			graphManager = new GraphManager();
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			switch (position)
			{
			case 1: return graphManager.fragmentVisibility;
			case 2: return graphManager.fragmentClouds;
			case 3: return graphManager.fragmentTemperature;
			case 4: return graphManager.fragmentDewPoint;
			case 5: return graphManager.fragmentAltimeter;
			}
			return graphManager.fragmentWinds;
		}

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position)
			{
			case 0: 
				return getString(R.string.title_graph_winds).toUpperCase(l);
			case 1: 
				return getString(R.string.title_graph_visibility).toUpperCase(l);
			case 2: 
				return getString(R.string.title_graph_clouds).toUpperCase(l);
			case 3: 
				return getString(R.string.title_graph_temperature).toUpperCase(l);
			case 4: 
				return getString(R.string.title_graph_dewpoint).toUpperCase(l);
			case 5: 
				return getString(R.string.title_graph_altimeter).toUpperCase(l);
			}
			return null;
		}

		public void startDataAcquisition(Intent intent)
		{
			if (!graphManager.isDataAcquired())
			{
				// defaults
				String id = "KDCA";
				int month = 1;
				int year = 2020;
				int timeOfDay = 0;

				if (intent.hasExtra(AptConst.keyID)) {
					id = intent.getStringExtra(AptConst.keyID);
					month = intent.getIntExtra(AptConst.keyMonth, month);
					year = intent.getIntExtra(AptConst.keyYear, year);
					timeOfDay = intent.getIntExtra(AptConst.keyTime, timeOfDay);
				}
				graphManager.startDataAcquisition(id, month, year, timeOfDay);
			}
		}

		public void updateTimeOfDay(int newTimeOfDay) {
			graphManager.updateTimeOfDay(newTimeOfDay);
		}

		public String getDataSetTitle() {
			return graphManager.getDataSetTitle();
		}
	}
}
