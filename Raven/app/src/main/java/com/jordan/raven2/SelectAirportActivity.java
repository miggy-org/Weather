package com.jordan.raven2;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class SelectAirportActivity extends FragmentActivity implements ActionBar.TabListener, SelectAirportFragment.ISelectAirportCallback {

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
        setContentView(R.layout.activity_select_airport);

        // the view pager will only be available in landscape mode on tablets
        mViewPager = (ViewPager) findViewById(R.id.pager);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null)
        {
            SelectAirportFragment frag = new SelectAirportFragment();
            if (mViewPager != null)
                frag.setCallback(this);
            fm.beginTransaction().add(R.id.container, frag).commit();
        }

        if (mViewPager != null)
        {
            // Set up the action bar.
            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // get the message from the intent
            //Intent intent = getIntent();
            //String id = intent.getStringExtra(AptConst.keyID);
            //String name = intent.getStringExtra(AptConst.keyName);
            //String city = intent.getStringExtra(AptConst.keyCity);
            //String latlon = intent.getStringExtra(AptConst.keyLatLon);

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            // When swiping between different sections, select the corresponding
            // tab. We can also use ActionBar.Tab#select() to do this if we have
            // a reference to the Tab.
            mViewPager
                    .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            actionBar.setSelectedNavigationItem(position);
                        }
                    });

            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(actionBar.newTab()
                        .setText(mSectionsPagerAdapter.getPageTitle(i))
                        .setTabListener(this));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_airport, menu);
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
    public void onAirportSelected(String id, String name, String city, double lat, double lon) {
        mSectionsPagerAdapter.onAirportSelected(id, name, city, lat, lon);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        ShowDetailsFragment fragmentDetails;
        ShowMapFragment fragmentMap;
        ShowWeatherFragment fragmentWeather;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            fragmentDetails = new ShowDetailsFragment();
            fragmentMap = new ShowMapFragment();
            fragmentWeather = new ShowWeatherFragment();
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

        public void onAirportSelected(String id, String name, String city, double lat, double lon) {
            fragmentDetails.loadData(id, name, city);
            fragmentMap.loadData(lat, lon);
            fragmentWeather.loadData(id);
        }
    }
}
