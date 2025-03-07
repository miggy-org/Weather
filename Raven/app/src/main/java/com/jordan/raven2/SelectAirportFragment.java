package com.jordan.raven2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectAirportFragment extends Fragment implements AptUtil.IAirportDataResult, View.OnClickListener, AdapterView.OnItemClickListener
{
	public interface ISelectAirportCallback
	{
		public void onAirportSelected(String id, String name, String city, double lat, double lon);
	}
	private ISelectAirportCallback callback;
	
	public SelectAirportFragment()
	{
	}

	public void setCallback(ISelectAirportCallback callback)
	{
		this.callback = callback;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_select_airport,
				container, false);
		return rootView;
	}

	class AirportListAdapter extends ArrayAdapter<AirportEntry>
	{
		private final Context context;
		private final List<AirportEntry> airports;

		public AirportListAdapter(Context context, List<AirportEntry> objects)
		{
			super(context, R.layout.list_item_airport, objects);
			this.context = context;
			this.airports = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.list_item_airport, parent, false);
			TextView idText = (TextView)rowView.findViewById(R.id.airportID);
			idText.setText(airports.get(position).ID);
			TextView nameText = (TextView)rowView.findViewById(R.id.airportName);
			nameText.setText(airports.get(position).Name);
			TextView tzText = (TextView)rowView.findViewById(R.id.airportTZ);
			tzText.setText(airports.get(position).getTZAbbreviation());
			return rowView;
		}
		
		public AirportEntry getAirport(int row)
		{
			return airports.get(row);
		}
		
		public void sortById()
		{
			Collections.sort(airports);
		}
		public void sortByCity()
		{
			Collections.sort(airports, new AirportEntry.OrderByCity());
		}
		public void sortByTz()
		{
			Collections.sort(airports, new AirportEntry.OrderByTz());
		}
	}
	private AirportListAdapter listAdapter;
	
	@Override
	public void onStart()
	{
		super.onStart();

		TextView tv = (TextView)getActivity().findViewById(R.id.idHeader);
		tv.setOnClickListener(this);
		tv = (TextView)getActivity().findViewById(R.id.cityHeader);
		tv.setOnClickListener(this);
		tv = (TextView)getActivity().findViewById(R.id.tzHeader);
		tv.setOnClickListener(this);
		
		ListView lv = (ListView)getActivity().findViewById(R.id.listView);
		lv.setOnItemClickListener(this);
		if (lv.getAdapter() == null)
			AptUtil.getAirportListString(this);
	}

	private void loadIntoAirportList(List<AirportEntry> listOfAirports)
	{
		listAdapter = new AirportListAdapter(getActivity(), listOfAirports);
		ListView listView = (ListView)getActivity().findViewById(R.id.listView);
		listView.setAdapter(listAdapter);
	}
	
	@Override
	public void onDataCallback(String resp)
	{
		ArrayList<AirportEntry> items = null;
		String error = "";
		
		try
		{
			String[] entries = resp.split("\\|");
			
			if (entries.length > 0 && entries.length <= 1000)
			{
				items = new ArrayList<AirportEntry>(entries.length);
				for (int i = 1; i < entries.length; i++)
				{
					String[] fields = entries[i].split(";");
					if (fields.length >= 6)
					{
						AirportEntry entry = new AirportEntry(fields[0], fields[1], fields[2], Double.parseDouble(fields[3]), Double.parseDouble(fields[4]), Integer.parseInt(fields[5]));
	                    items.add(entry);
					}
				}
			}
			else
				error = "Too many airports, or none at all!";
		}
		catch (Exception ex)
		{
			error = ex.toString();
		}
		
		if (error.length() > 0)
		{
			Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
			Log.e("AptUtil", error);
		}
		else
		{
			loadIntoAirportList(items);
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.idHeader)
			listAdapter.sortById();
		else if (v.getId() == R.id.cityHeader)
			listAdapter.sortByCity();
		else if (v.getId() == R.id.tzHeader)
			listAdapter.sortByTz();

		ListView listView = (ListView)getActivity().findViewById(R.id.listView);
		listView.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		AirportEntry apt = listAdapter.getAirport((int) id);

		if (callback == null)
		{
			Intent intent = new Intent(getActivity(), ShowAirportActivity.class);
			intent.putExtra(AptConst.keyID, apt.ID);
			intent.putExtra(AptConst.keyName, apt.Name);
			intent.putExtra(AptConst.keyCity, apt.City);
			intent.putExtra(AptConst.keyLatLon, Double.toString(apt.Lat) + "," + Double.toString(apt.Lon));
			startActivity(intent);
		}
		else
		{
			callback.onAirportSelected(apt.ID, apt.Name, apt.City, apt.Lat, apt.Lon);
		}
	}
}
