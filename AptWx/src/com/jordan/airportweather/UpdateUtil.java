package com.jordan.airportweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class UpdateUtil
{
    private static final Logger log = Logger.getLogger(UpdateUtil.class.getName());

    private static long s_startHour = -1;
    private static long s_stopHour = -1;
    private static long s_updateFreq = -1;
    private static long s_minElapsedTime = WxConst.minElapsedTime;
    
    // loads variables needed for time based filtering (these never change)
    public static boolean loadTimeFilterVars(DatastoreService datastore)
    {
    	if (s_startHour == -1)
    	{
    		Key cfgKey = KeyFactory.createKey(WxConst.kind_config, WxConst.kind_config);
    		try
    		{
    			Entity cfg = datastore.get(cfgKey);
    			s_startHour = (long) cfg.getProperty(WxConst.cfg_startHour);
    			s_stopHour = (long) cfg.getProperty(WxConst.cfg_stopHour);
    			s_updateFreq = (long) cfg.getProperty(WxConst.cfg_updateFreq);
    			if (cfg.hasProperty(WxConst.cfg_minElapsedTime))
    				s_minElapsedTime = (long) cfg.getProperty(WxConst.cfg_minElapsedTime);
    		}
    		catch (Exception e)
    		{
    			log.warning(e.toString());
    			return false;
    		}
    	}
    	
    	return (s_startHour > -1 && s_stopHour > -1 && s_updateFreq > -1);
    }
    
    // externally updates the time based filtering variables
    public static void updateTimeFilterVars(long newUpdateFreq)
    {
    	if (newUpdateFreq >= 1 && newUpdateFreq <= 4)
    	{
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    		Entity cfg = WxUtil.getConfigEntity(datastore);
    		if (cfg != null)
    		{
    			cfg.setProperty(WxConst.cfg_updateFreq, newUpdateFreq);
    			s_updateFreq = newUpdateFreq;
    		}
    	}
    	log.info("Update frequency is now " + s_updateFreq);
    }
    
    // externally updates the minimum elapsed time between updates control (arg is in seconds)
    public static void updateMinElapsedTime(long newElapsedTime)
    {
    	if (newElapsedTime >= 0 && newElapsedTime <= 55*60)
    	{
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    		Entity cfg = WxUtil.getConfigEntity(datastore);
    		if (cfg != null)
    		{
    			newElapsedTime *= 1000;
    			
    			cfg.setProperty(WxConst.cfg_minElapsedTime, newElapsedTime);
    			s_minElapsedTime = newElapsedTime;
    		}
    	}
    	log.info("Minimum elapsed time is now " + (s_minElapsedTime/1000) + " seconds");
    }
    
    // determines if a given airport can be updated at this time
    @SuppressWarnings("deprecation")
    public static boolean canUpdate(DatastoreService datastore, Key parentKey, String airportID, Date dtNow, boolean isDST)
    {
		try
		{
			// first, check to see if the airport even exists
			Entity airport = datastore.get(parentKey);

			if (loadTimeFilterVars(datastore))
			{
				// get the current hour in UTC, and adjust early morning hours relative to the previous day
				int localTZOffset = dtNow.getTimezoneOffset() / 60;
				int currHourUTC = dtNow.getHours() + localTZOffset;
				if (currHourUTC < 12)
					currHourUTC += 24;
				//log.warning("Current hour is " + dtNow.getHours() + ", offset is " + localTZOffset + ", UTC hour is " + currHourUTC);
				
				long tzOffset = (long) airport.getProperty(WxConst.prop_tzOffset);
				long startHourUTC = s_startHour + tzOffset - (isDST ? 1 : 0);
				if (currHourUTC < startHourUTC)
				{
					//log.info("Too early to track airport ID " + airportID);
					return false;
				}
				long stopHourUTC = s_stopHour + tzOffset - (isDST ? 1 : 0);
				if (currHourUTC > stopHourUTC)
				{
					//log.info("Too late to track airport ID " + airportID);
					return false;
				}
				
				long modHour = (currHourUTC - startHourUTC) % s_updateFreq;
				if (modHour > 0)
				{
					//log.info("Frequency check failed for airport ID " + airportID);
					return false;
				}

				// then check to see if sufficient time has elapsed
				Object prop = airport.getProperty(WxConst.prop_lastUpdate);
				if (prop != null)
				{
					Date dtLastUpdate = (Date) prop;
					if (dtNow.getTime() - dtLastUpdate.getTime() < s_minElapsedTime)
					{
						log.warning("Airport ID " + airportID + " was updated too recently (min elapsed time is " + s_minElapsedTime/1000 + " seconds)");
						return false;
					}
				}
			}
			
			// go ahead and set the update time to now
			airport.setProperty(WxConst.prop_lastUpdate, dtNow);

			// increment the airport metar count
			long metarCount = 0;
			if (airport.hasProperty(WxConst.prop_metarCount))
				metarCount = (long) airport.getProperty(WxConst.prop_metarCount);
			airport.setProperty(WxConst.prop_metarCount, ++metarCount);
			
			datastore.put(airport);
		}
        catch (EntityNotFoundException ex)
        {
			log.warning("Airport ID " + airportID + " isn't tracked by this service");
			return false;
        }
		catch (Exception e)
		{
			log.warning(e.toString());
			return false;
		}

		return true;
    }
    
    // decrements the METAR count for a given airport entity
    public static void decMetarCount(DatastoreService datastore, Key airportKey)
    {
		try
		{
			// first, check to see if the airport even exists
			Entity airport = datastore.get(airportKey);

			// increment the airport metar count
			long metarCount = 0;
			if (airport.hasProperty(WxConst.prop_metarCount))
				metarCount = (long) airport.getProperty(WxConst.prop_metarCount);
			if (metarCount > 0)
				metarCount--;
			airport.setProperty(WxConst.prop_metarCount, metarCount);
			
			datastore.put(airport);
		}
		catch (Exception e)
		{
			log.warning(e.toString());
		}
    }
    
    private static Entity createMetar(String[] items, Key parentKey)
    {
		// we will start w/ the second element, since the first is the airport ID 
    	int cur = 1;
    	
		// create the METAR entity
		Entity metar = new Entity(WxConst.kind_metar, parentKey);
		
		// time string is next
		parseTimeString(items[cur++], metar);
		
		// the next might be the "AUTO" extension
		if (items[cur].compareToIgnoreCase("AUTO") == 0)
		{
			metar.setProperty(WxConst.prop_auto, true);
			cur++;
		}
		
		// the next should be the winds, or visibility if none
		while (!isWindString(items[cur]) && !isVisString(items[cur]))
		{
			cur++;
		}
		if (isWindString(items[cur]))
			parseWindString(items[cur++], metar);
		
		// the next should be the visibility (required)
		while (!isVisString(items[cur]))
		{
			cur++;
		}
		parseVisString(items[cur++], metar);
		
        // the next might be an optional visibility modifier string
		String visMod = "";
		while (!isCloudString(items[cur]) && !isTempDewString(items[cur]) && !isAltimeterString(items[cur]))
		{
			if (visMod != "")
				visMod += " ";
			visMod += items[cur++];
		}
		if (visMod != "")
			metar.setProperty(WxConst.prop_visibilityModifier, visMod);
		
        // the next should be a block of strings which are cloud covers
        int cloudCnt = 0;
        while (!isTempDewString(items[cur + cloudCnt]) && !isAltimeterString(items[cur + cloudCnt]))
            cloudCnt++;
        for (int i = 0; i < cloudCnt; i++)
        {
        	parseCloudString(items[cur++], metar, i + 1);
        }
        
        // the next should be the temp/dew point (contains a '/')
        if (isTempDewString(items[cur]))
        	parseTempDewString(items[cur++], metar);
        
        // the next should be the altimeter
        if (isAltimeterString(items[cur]))
        	parseAltimeterString(items[cur++], metar);
        
        // remarks at the end
        if (cur < items.length && items[cur++].compareToIgnoreCase("RMK") == 0)
        {
			String remarks = "";
        	while (cur < items.length)
        	{
        		if (remarks != "")
        			remarks += " ";
        		remarks += items[cur++];
        	}
        	metar.setProperty(WxConst.prop_remarks, remarks);
        }
    	
    	return metar;
    }

    // single-ping version
    public static Entity CreateMetar(String report, DatastoreService ds)
    		throws IOException
    {
        // split the report into individual strings
		String[] items = report.split(" ");

		// first string should be the airport ID, check it
		String airportID = items[0];
		Key parentKey = KeyFactory.createKey(WxConst.kind_airport, airportID);
		try
		{
			ds.get(parentKey);
		}
		catch (EntityNotFoundException e)
		{
			throw new IOException("Airport ID " + airportID + " not found");
		}
		log.info("Weather string for " + airportID + " is '" + report + "'");
		
		return createMetar(items, parentKey);
    }
    
    // multi-ping version
    public static Entity CreateMetar(String urlFormat, String airportID, Key parentKey)
    		throws IOException
    {
        // ping the aviation weather site for the airports weather
		String urlFinal = urlFormat.replace("%1", airportID);
		//URL url = new URL("http://www.aviationweather.gov/adds/metars/index.php?station_ids=" + airportID + "&std_trans=standard&chk_metars=on&hoursStr=most+recent+only&submitmet=Submit");
		URL url = new URL(urlFinal);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

		// find the line that contains the real data (has the ID in it)
		String line = findAndSpliceWeatherString(reader, airportID);
		if (line == null)
			throw new IOException("Failed, could not locate weather string");
		log.info("Weather string for " + airportID + " is '" + line + "'");

        // split the result into individual strings
		String[] items = line.split(" ");

		// first string should match the airport ID
		if (items[0].compareToIgnoreCase(airportID) != 0)
			throw new IOException("Airport ID string doesn't match");
		
		return createMetar(items, parentKey);
    }

    private static String containsAirportID(String line, String[] ids)
    {
    	for (String item : ids)
    	{
			if (line.indexOf(item) != -1)
				return item;
    	}
    	return "";
    }
    
	public static List<String> findAndSpliceWeatherStrings(BufferedReader reader, String[] ids)
	{
		List<String> wxReports = new ArrayList<String>();

		try
		{
			// find the line that contains the real data (has the ID in it)
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				String id = containsAirportID(line, ids);
				if (id != "")
				{
					// trim off stuff before the ID, and any thing after the end of the interesting part
					line = line.substring(line.indexOf(id));
					int end = line.indexOf("</FONT>");
					if (end != -1)
					{
						line = line.substring(0, end);
					}
					else
					{
						// read one more line only
						String nextLine = reader.readLine();
						if (nextLine != null)
						{
							end = nextLine.indexOf("</FONT>");
							if (end != -1)
							{
								line += nextLine.substring(0, end);
							}
						}
					}
						
					wxReports.add(line);
				}
			}
		}
		catch (IOException ex)
		{
			log.warning(ex.toString() + "(parsing WX strings)");
		}
		
		return wxReports;
	}
	
	private static String findAndSpliceWeatherString(BufferedReader reader, String id)
	{
		String line = null;

		try
		{
			// find the line that contains the real data (has the ID in it)
			while ((line = reader.readLine()) != null)
			{
				if (line.indexOf(id) != -1)
				{
					break;
				}
			}
	
			// trim off stuff before the ID, and any thing after the end of the interesting part
			if (line != null)
			{
				line = line.substring(line.indexOf(id));
				int end = line.indexOf("</FONT>");
				if (end != -1)
				{
					line = line.substring(0, end);
				}
				else
				{
					// read one more line only
					String nextLine = reader.readLine();
					if (nextLine != null)
					{
						end = nextLine.indexOf("</FONT>");
						if (end != -1)
						{
							line += nextLine.substring(0, end);
						}
					}
				}
			}
		}
		catch (IOException ex)
		{
			log.warning(ex.toString() + "(parsing WX string for " + id + ")");
		}
		
		return line;
	}
	
	// TODO: Calendar isn't supported by AppEngine yet (can't be set as a property of an Entity), update when that changes
    @SuppressWarnings("deprecation")
	private static void parseTimeString(String item, Entity metar)
		throws IllegalArgumentException
	{
		if (!item.endsWith("Z"))
			throw new IllegalArgumentException("Time string (" + item + ") did not end with 'Z'");

		// parse the day/hour/minutes fields of the report (time is UTC)
		int lday = Integer.parseInt(item.substring(0, 2));
		int hour = Integer.parseInt(item.substring(2, 4));
		int min = Integer.parseInt(item.substring(4, 6));

		// get the current time
		//Calendar now = Calendar.getInstance();
		Date now = new Date();
		//int year = now.get(Calendar.YEAR);
		//int month = now.get(Calendar.MONTH);
		//int day = now.get(Calendar.DAY_OF_MONTH);
		int year = now.getYear();
		int month = now.getMonth();
		int day = now.getDate();
		
        // special case - if the day of the report is a new month, be careful
        if (day == 1 && lday > 1)
        {
            month--;
            if (month < 0)
            {
                year--;
                month = 11;
            }
        }
        
        // create a calendar for the time of the actual report
        //Calendar dateOfReport = new GregorianCalendar();
        //dateOfReport.set(year, month, lday, hour, min);
		Date dateOfReport = new Date(year, month, lday, hour, min);
        metar.setProperty(WxConst.prop_date, dateOfReport);
	}
	
    private static boolean isWindString(String item)
    {
		return item.endsWith("KT");
    }
    
	private static void parseWindString(String item, Entity metar)
		throws IllegalArgumentException
	{
		// the first 3 chars are either the dir or 'VRB'
		String dir = item.substring(0, 3);
		if (dir.compareToIgnoreCase("VRB") == 0)
		{
			metar.setProperty(WxConst.prop_windDirection, -1);
		}
		else
		{
			int tmp = Integer.parseInt(dir);
			metar.setProperty(WxConst.prop_windDirection, tmp);
		}
		
		// next 2 are the strength
		int str = Integer.parseInt(item.substring(3, 5));
		metar.setProperty(WxConst.prop_windStrength, str);
		
        // if a gust factor is present, then next 3 chars are 'G' and the gust
		if (item.length() == 10 && item.substring(5, 6).compareToIgnoreCase("G") == 0)
		{
			int gust = Integer.parseInt(item.substring(6, 8));
			metar.setProperty(WxConst.prop_windGust, gust);
		}
	}

    private static boolean isVisString(String item)
    {
		return item.endsWith("SM");
    }
    
	private static void parseVisString(String item, Entity metar)
		throws IllegalArgumentException
	{
		int end = item.indexOf("SM");
		if (end == -1)
			throw new IllegalArgumentException("Visibility string (" + item + ") did not end with 'SM'");
		
		// this one is easy (unless it's a fraction)
		int vis;
		if (item.indexOf('/') != -1)
			vis = 1;  // what a hack!
		else
			vis = Integer.parseInt(item.substring(0, end));
		metar.setProperty(WxConst.prop_visibility, vis);
	}
		
	private static boolean isCloudString(String item)
	{
        // the first 3 chars of a cloud string are one of the following
        if (item.length() < 3)
            return false;
        item = item.substring(0, 3);
        if (item.compareToIgnoreCase("CLR") == 0 ||
        	item.compareToIgnoreCase("SKC") == 0 ||
        	item.compareToIgnoreCase("FEW") == 0 ||
        	item.compareToIgnoreCase("SCT") == 0 ||
        	item.compareToIgnoreCase("BKN") == 0 ||
        	item.compareToIgnoreCase("OVC") == 0)
        	return true;
        return false;
	}
	
	private static void parseCloudString(String item, Entity metar, int index)
		throws IllegalArgumentException
	{
		// the first 3 characters are the cloud cover type
		String type = item.substring(0, 3);
		metar.setProperty(WxConst.prop_cloudTypePrefix + index, type);

		if (type.compareToIgnoreCase("CLR") == 0 || type.compareToIgnoreCase("SKC") == 0)
		{
			// clear skies
			metar.setProperty(WxConst.prop_cloudFloorPrefix + index, -1);
			return;
		}
		
        // the last 3 chars is the altitude
		int alt = Integer.parseInt(item.substring(3, 6));
		metar.setProperty(WxConst.prop_cloudFloorPrefix + index, alt);
	}
	
	private static boolean isTempDewString(String item)
	{
		// the length checks against some visibility modifier strings that have the '/' char as well, but are longer
		return (item.indexOf("/") != -1 && item.length() < 8);
	}
	
	private static void parseTempDewString(String item, Entity metar)
		throws IllegalArgumentException
	{
		int index = item.indexOf("/");
		if (index == -1)
			throw new IllegalArgumentException("Temp/dew string (" + item + ") did not contain '/'");
		
		// the '/' splits the temp and dew point
		String tempStr = item.substring(0, index);
		if (tempStr.indexOf("M") != -1)
			metar.setProperty(WxConst.prop_temperature, -Integer.parseInt(tempStr.substring(1)));
		else
			metar.setProperty(WxConst.prop_temperature, Integer.parseInt(tempStr));
		String dewStr = item.substring(index + 1);
		if (!dewStr.isEmpty())
		{
			if (dewStr.indexOf("M") != -1)
				metar.setProperty(WxConst.prop_dewPoint, -Integer.parseInt(dewStr.substring(1)));
			else
				metar.setProperty(WxConst.prop_dewPoint, Integer.parseInt(dewStr));
		}
	}
	
	private static boolean isAltimeterString(String item)
	{
		return item.startsWith("A");
	}
	
	private static void parseAltimeterString(String item, Entity metar)
		throws IllegalArgumentException
	{
		if (!item.startsWith("A"))
			throw new IllegalArgumentException("Altimeter string (" + item + ") did not start with 'A'");
		
        // another easy one
        int alt = Integer.parseInt(item.substring(1, 5));
        metar.setProperty(WxConst.prop_altimeter, alt);
	}			
}
