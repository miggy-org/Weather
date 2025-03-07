package com.jordan.raven2;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ShowMapFragment extends Fragment
{
	MapView theMap;

	public ShowMapFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);

		// create the MapView
		theMap = (MapView) rootView.findViewById(R.id.airport_map);
		theMap.onCreate(savedInstanceState);

		try
		{
			// set map initial settings
			theMap.getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap gMap) {
					gMap.getUiSettings().setMyLocationButtonEnabled(false);
					try {
						gMap.setMyLocationEnabled(false);
					} catch(SecurityException e) {
						// TODO
					}
				}
			});

			// initialize the map and it's associated services
			MapsInitializer.initialize(getActivity());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return rootView;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// get the airport coordinates
		Bundle bundle = getArguments();
		if (bundle != null)
		{
			String latlonStr = bundle.getString(AptConst.keyLatLon);
			if (latlonStr != null)
			{
				String[] latlon = latlonStr.split(",");
				double lat = Double.parseDouble(latlon[0]);
				double lon = Double.parseDouble(latlon[1]);
				loadData(lat, lon);
			}
		}
	}

	public void loadData(final double lat, final double lon)
	{
		// update the location and zoom of the MapView
		theMap.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap gMap) {
				if (gMap != null)
				{
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12);
					gMap.animateCamera(cameraUpdate);
					gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				}
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (theMap != null)
			theMap.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (theMap != null)
			theMap.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (theMap != null)
			theMap.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (theMap != null)
			theMap.onPause();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (theMap != null)
			theMap.onLowMemory();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (theMap != null)
			theMap.onDestroy();
	}

}
