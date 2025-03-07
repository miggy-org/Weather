package com.jordan.raven2;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ShowWeatherFragment extends Fragment implements AptUtil.IAirportDataResult, View.OnClickListener, AdapterView.OnItemClickListener
{
	private MonthEntry selectedMonth;
	private String aptID;
	
	public ShowWeatherFragment()
	{
		selectedMonth = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
		Button btn = rootView.findViewById(R.id.go_button);
		btn.setOnClickListener(this);
		return rootView;
	}

	class MonthListAdapter extends ArrayAdapter<MonthEntry>
	{
		private final Context context;
		private final List<MonthEntry> months;

		public MonthListAdapter(Context context, List<MonthEntry> objects)
		{
			super(context, R.layout.list_item_month, objects);
			this.context = context;
			this.months = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.list_item_month, parent, false);
			TextView idText = rowView.findViewById(R.id.monthID);
			MonthEntry entry = months.get(position);
			if (entry.Month == 1)
				idText.setText(entry.getName() + ", " + entry.Year);
			else
				idText.setText(entry.getName());

			ImageView imageView = rowView.findViewById(R.id.checkMark);
			if (selectedMonth.Month == entry.Month && selectedMonth.Year == entry.Year) {
				idText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				//idText.setTextSize(24);
				imageView.setVisibility(View.VISIBLE);
			} else {
				idText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				//idText.setTextSize(16);
				imageView.setVisibility(View.GONE);
			}

			return rowView;
		}
		
		public MonthEntry getMonth(int row)
		{
			return months.get(row);
		}
	}
	private MonthListAdapter listMonthAdapter;
	
	private void loadIntoMonthList(List<MonthEntry> listOfMonths)
	{
		listMonthAdapter = new MonthListAdapter(getActivity(), listOfMonths);
		ListView listView = getActivity().findViewById(R.id.listMonthView);
		listView.setAdapter(listMonthAdapter);
		listView.setOnItemClickListener(this);

		// auto-select the first item
		selectedMonth = listOfMonths.get(0);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();

		Bundle bundle = getArguments();
		if (bundle != null)
			loadData(bundle.getString(AptConst.keyID));
		else if (aptID != null)
			loadData(aptID);
	}

	public void loadData(String id)
	{
		aptID = id;
		if (isAdded() && aptID != null)
			AptUtil.getAirportMonthListString(this, aptID);
	}
	
	@Override
	public void onDataCallback(String resp)
	{
		ArrayList<MonthEntry> items = null;
		String error = "";
		
		try
		{
			String[] entries = resp.split("\\|");
			
			if (entries.length > 0 && entries.length <= 1000)
			{
				items = new ArrayList(entries.length);
				for (int i = 1; i < entries.length; i++)
				{
					String[] fields = entries[i].split(";");
					if (fields.length >= 2)
					{
						MonthEntry entry = new MonthEntry(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
	                    items.add(entry);
					}
				}
			}
			else
				error = "Too many months!";
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
			loadIntoMonthList(items);
		}
	}

	@Override
	public void onClick(View v)
	{
		if (selectedMonth != null)
		{
			Intent intent = new Intent(getActivity(), ShowGraphsActivity.class);
			intent.putExtra(AptConst.keyID, aptID);
			intent.putExtra(AptConst.keyMonth, selectedMonth.Month);
			intent.putExtra(AptConst.keyYear, selectedMonth.Year);
			intent.putExtra(AptConst.keyTime, 0);
			startActivity(intent);
		}
		else
			Toast.makeText(getActivity(), "Select a month and time", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (parent == getActivity().findViewById(R.id.listMonthView)) {
			selectedMonth = listMonthAdapter.getMonth(position);
			listMonthAdapter.notifyDataSetChanged();
		}
	}
}
