package com.jordan.raven2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.util.Log;

public class GraphManager implements AptUtil.IAirportDataResult
{
	public GraphFragment fragmentWinds;
	public GraphFragment fragmentVisibility;
	public GraphFragment fragmentClouds;
	public GraphFragment fragmentTemperature;
	public GraphFragment fragmentDewPoint;
	public GraphFragment fragmentAltimeter;

	private List<Metar> listMorning;
	private List<Metar> listNoon;
	private List<Metar> listAfternoon;
	private List<Metar> listEvening;
	
	private int month;
	private int year;
	private int timeOfDay;
	private boolean dataAcquired;

	public GraphManager()
	{
		fragmentWinds = new GraphFragment.GraphWindFragment();
		fragmentVisibility = new GraphFragment.GraphVisibilityFragment();
		fragmentClouds = new GraphFragment.GraphCloudsFragment();
		fragmentTemperature = new GraphFragment.GraphTemperatureFragment();
		fragmentDewPoint = new GraphFragment.GraphDewPointFragment();
		fragmentAltimeter = new GraphFragment.GraphAltimeterFragment();

		listMorning = new ArrayList<Metar>();
		listNoon = new ArrayList<Metar>();
		listAfternoon = new ArrayList<Metar>();
		listEvening = new ArrayList<Metar>();
	}

	public boolean isDataAcquired()
	{
		return dataAcquired;
	}
	
	public void startDataAcquisition(String id, int m, int y, int t)
	{
		month = m;
		year = y;
		timeOfDay = t;
		dataAcquired = false;
		
		AptUtil.getAirportMonthDataString(this, id, m, y);
	}

	public void updateTimeOfDay(int newTimeOfDay)
	{
		if (isDataAcquired()) {
			timeOfDay = newTimeOfDay;
			plotData();
		}
	}

	private String getTimeOfDayString() {
		switch (timeOfDay)
		{
			case 1: return "Noon";
			case 2: return "Afternoon";
			case 3: return "Evening";
		}
		return "Morning";
	}

	public String getDataSetTitle()
	{
		return String.format(Locale.getDefault(), "%1s, %2d (%3s)",
				new MonthEntry(month, year).getName(), year, getTimeOfDayString());
	}

    private void addToMetarListIfNewDay(List<Metar> listOfMetars, Metar newMetar)
    {
    	if (listOfMetars.size() == 0)
            listOfMetars.add(newMetar);
        else
    	{
    		Metar metar = listOfMetars.get(listOfMetars.size() - 1);
    		if (metar.Time.get(Calendar.DAY_OF_MONTH) != newMetar.Time.get(Calendar.DAY_OF_MONTH))
                listOfMetars.add(newMetar);
    	}
    }

    private List<Metar> getListByTimeOfDay()
    {
    	switch (timeOfDay)
    	{
    	case 1: return listNoon;
    	case 2: return listAfternoon;
    	case 3: return listEvening;
    	}
    	return listMorning;
    }
    
    private class WindConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            return metar.WindStrength;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public String getGraphPointDetail(Metar metar) {
            String detail = String.format(Locale.US, "%03d @ %d knots", metar.WindDirection, metar.WindStrength);
            if (metar.WindGust > 0)
                detail += " (gusts to " + Integer.toString(metar.WindGust) + ")";
            return detail;
		}
    }
    
    private class VisibilityConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            return metar.Visibility;
		}

		@Override
		public String getGraphPointDetail(Metar metar) {
            return Integer.toString(metar.Visibility) + " SM";
		}
    }
    
    private class CloudsConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            int lowest = 250;  // 100s of feet
            for (int i = 0; i < metar.Clouds.size(); i++)
            {
                // ignore few clouds
            	Metar.Cloud cloud = metar.Clouds.get(i);
                if (cloud.Type != Metar.Cloud.CloudType.FEW && cloud.Floor < lowest)
                    lowest = cloud.Floor;
            }
            return lowest;
		}

		@Override
		public String getGraphPointDetail(Metar metar) {
            if (metar.Clouds.size() == 0)
                return "Clear";

            String detail = "";
            for (int i = 0; i < metar.Clouds.size(); i++)
            {
            	Metar.Cloud cloud = metar.Clouds.get(i);
                if (detail.length() > 0)
                    detail += ", ";
                detail += cloud.toString();
            }
            return detail;
		}
    }
    
    private class TemperatureConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            return metar.Temperature;
		}

		@Override
		public String getGraphPointDetail(Metar metar) {
            return Integer.toString(metar.Temperature) + " C";
		}
    }
    
    private class DewPointConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            return metar.DewPoint;
		}

		@Override
		public String getGraphPointDetail(Metar metar) {
            return Integer.toString(metar.DewPoint) + " C";
		}
    }
    
    private class AltimeterConvertor implements GraphFragment.IGraphPoints
    {
		@Override
		public int getGraphPoint(Metar metar) {
            return metar.Altimeter;
		}

		@Override
		public String getGraphPointDetail(Metar metar) {
            return Double.toString(metar.Altimeter / 100.0);
		}
    }

	private void plotData()
	{
		int lastDay = new MonthEntry(month, year).getLastDay();
		List<Metar> listOfData = getListByTimeOfDay();
		fragmentWinds.graphData(listOfData, new WindConvertor(), lastDay);
		fragmentVisibility.graphData(listOfData, new VisibilityConvertor(), lastDay);
		fragmentClouds.graphData(listOfData, new CloudsConvertor(), lastDay);
		fragmentTemperature.graphData(listOfData, new TemperatureConvertor(), lastDay);
		fragmentDewPoint.graphData(listOfData, new DewPointConvertor(), lastDay);
		fragmentAltimeter.graphData(listOfData, new AltimeterConvertor(), lastDay);
	}

	@Override
	public void onDataCallback(String data)
	{
		String error = "";

		try
		{
			String[] entries = data.split("\\|");
			
			if (entries.length > 0 && entries.length <= 128)
			{
                // response comes in descending order, we want ascending
				for (int i = entries.length - 1; i > 0; i--)
				{
					try
					{
                        Metar metar = new Metar(entries[i], month, year);
						if (metar.Time.get(Calendar.HOUR_OF_DAY) < 10)
                            addToMetarListIfNewDay(listMorning, metar);
						else if (metar.Time.get(Calendar.HOUR_OF_DAY) < 14)
                            addToMetarListIfNewDay(listNoon, metar);
						else if (metar.Time.get(Calendar.HOUR_OF_DAY) < 18)
                            addToMetarListIfNewDay(listAfternoon, metar);
						else
                            addToMetarListIfNewDay(listEvening, metar);
					}
					catch (Exception ex)
					{
						error = "Error while processing " + entries[i] + "\n\n" + ex.toString();
						break;
					}
				}
			}
			else
				error = "Too many entries, or none at all!";
		}
		catch (Exception e)
		{
			error = e.toString();
		}
		
		if (error.length() == 0)
		{
			plotData();

			dataAcquired = true;
		}
		else
			Log.e("ShowGraphsActivity", error);
	}
}
