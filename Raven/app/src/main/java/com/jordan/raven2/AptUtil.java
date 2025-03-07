package com.jordan.raven2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class AptUtil
{
	// callback interface to receive the result string from an HTTP query
	public interface IAirportDataResult
	{
		public void onDataCallback(String data);
	}
	
	// class used to perform an HTTP query on an asynchronous tread
	static class GetHttpString extends AsyncTask<String, Integer, String>
	{
		IAirportDataResult resultInterface;
		
		GetHttpString(IAirportDataResult iface)
		{
			resultInterface = iface;
		}
		
		protected String doInBackground(String... urls)
		{
			String finalStr = "";

			HttpURLConnection http = null;
			try
			{
				URL url = new URL(urls[0]);
				http = (HttpURLConnection) url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
				String line;
				while ((line = in.readLine()) != null)
				{
					finalStr += line;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (http != null)
					http.disconnect();
			}
			
			return finalStr;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			if (resultInterface != null)
				resultInterface.onDataCallback(result);
		}
	}

	public static void getAirportListString(IAirportDataResult resultInterface)
	{
		GetHttpString getHttpString = new GetHttpString(resultInterface);
		getHttpString.execute(AptConst.uriGetAirportList);
	}
	
	public static void getAirportMonthListString(IAirportDataResult resultInterface, String aptID)
	{
		String uri = AptConst.uriGetAirportMonthList.replace("%1", aptID);
		GetHttpString getHttpString = new GetHttpString(resultInterface);
		getHttpString.execute(uri);
	}
	
	public static void getAirportMonthDataString(IAirportDataResult resultInterface, String aptID, int month, int year)
	{
		String uri = AptConst.uriGetAirportMonthData.replace("%1", aptID).replace("%2", Integer.toString(month)).replace("%3", Integer.toString(year));
		GetHttpString getHttpString = new GetHttpString(resultInterface);
		getHttpString.execute(uri);
	}
}
