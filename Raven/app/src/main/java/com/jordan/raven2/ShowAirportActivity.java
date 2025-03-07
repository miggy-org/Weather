package com.jordan.raven2;

import java.util.Locale;

import android.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import androidx.fragment.app.FragmentPagerAdapter;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

public class ShowAirportActivity extends FragmentActivity implements
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
		setContentView(R.layout.activity_show_airport);

		// Set up the action bar.
		//final ActionBar actionBar = getActionBar();
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// testing values
		String id = "KDCA";
		String name = "Ronald Reagan Washington National Airport";
		String city = "Washington, DC";
		String latlon = "38.8519163,-77.0376989";

		// get the message from the intent
		Intent intent = getIntent();
		if (intent.hasExtra(AptConst.keyID)) {
			id = intent.getStringExtra(AptConst.keyID);
			name = intent.getStringExtra(AptConst.keyName);
			city = intent.getStringExtra(AptConst.keyCity);
			latlon = intent.getStringExtra(AptConst.keyLatLon);
		}

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), id, name, city, latlon);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(mViewPager);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		//mViewPager
		//		.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
		getMenuInflater().inflate(R.menu.show_airport, menu);
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		ShowDetailsFragment fragmentDetails;
		ShowMapFragment fragmentMap;
		ShowWeatherFragment fragmentWeather;

		public SectionsPagerAdapter(FragmentManager fm, String id, String name, String city, String latlon) {
			super(fm);
			
			fragmentDetails = new ShowDetailsFragment();
			fragmentMap = new ShowMapFragment();
			fragmentWeather = new ShowWeatherFragment();

			Bundle args = new Bundle();
			args.putString(AptConst.keyID, id);
			args.putString(AptConst.keyName, name);
			args.putString(AptConst.keyCity, city);
			fragmentDetails.setArguments(args);

			args = new Bundle();
			args.putString(AptConst.keyLatLon, latlon);
			fragmentMap.setArguments(args);

			args = new Bundle();
			args.putString(AptConst.keyID, id);
			fragmentWeather.setArguments(args);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			switch (position) {
			case 0:
				return fragmentDetails;
			case 1:
				return fragmentMap;
			case 2:
				return fragmentWeather;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section_details).toUpperCase(l);
			case 1:
				return getString(R.string.title_section_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_section_weather).toUpperCase(l);
			}
			return null;
		}
	}

}
