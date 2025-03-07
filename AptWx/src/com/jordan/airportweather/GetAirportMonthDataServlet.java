package com.jordan.airportweather;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class GetAirportMonthDataServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GetAirportListServlet.class.getName());
    
    // constants
    private static final String versionStr = "VER1";
    private static final String delimStr = "|";
    private static final String delimMetarStr = ";";
    private static final String delimCloudStr = ",";
    
    // get arguments
    private static final String arg_aptid = "aptid";
    private static final String arg_month = "month";
    private static final String arg_year = "year";
    
    @SuppressWarnings("deprecation")
	private Date toStartDate(int month, int year)
    {
    	// argument is 1-12, but native Date format is 0-11
    	month--;
    	
    	return new Date(year - 1900, month, 1, 10, 0);
    }
    
    @SuppressWarnings("deprecation")
    private Date toEndDate(int month, int year)
    {
    	// argument is 1-12, but native Date format is 0-11
    	if (month == 12)
    	{
    		month = 0;
    		year++;
    	}
    	
    	return new Date(year - 1900, month, 1, 10, 0);
    }
    
    // returns a date token string
    @SuppressWarnings("deprecation")
	private static String getMETARDateString(Entity metar, long tzOffset)
    {
    	Date date = (Date) metar.getProperty(WxConst.prop_date);

    	// above date is in UTC, but we will send local time
    	boolean isInDST = WxUtil.isInDST(date);
    	int day = date.getDate();
    	int hour = date.getHours() - (int)tzOffset + (isInDST ? 1 : 0);
    	if (hour < 0)
    	{
    		// hour is in the previous day
    		hour += 24;
    		day--;
    		if (day < 1)
    		{
    			// day is in the previous month
    	    	int month = date.getMonth() - 1;
    			if (month < 0)
    				month = 11;
    			switch (month)
    			{
    			case 3: case 5: case 8: case 10: day = 30; break;
    			case 1: day = (date.getYear()%4 == 0 ? 29 : 28); break;
    			default: day = 31; break;
    			}
    		}
    	}

    	// format is "MMDDHHMM"
    	String dateStr = String.format("%1$02d", day);
    	dateStr += String.format("%1$02d", hour);
    	dateStr += String.format("%1$02d", date.getMinutes());
    	return dateStr;
    }
    
    // returns a wind token string
    private static String getMETARWindString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_windDirection))
		{
			long dir = (long) metar.getProperty(WxConst.prop_windDirection);
			if (dir != -1)
			{
				// 3 digits for the direction
				str = String.format("%1$03d", dir);

				// 2 digits for the strength
				long strength = (long) metar.getProperty(WxConst.prop_windStrength);
				if (strength > 99)
					strength = 99;
				str += String.format("%1$02d", strength);
				
				if (metar.hasProperty(WxConst.prop_windGust))
				{
					// 2 digits for gust, if present
					long gust = (long) metar.getProperty(WxConst.prop_windGust);
					if (gust > 99)
						gust = 99;
					str += String.format("%1$02d", gust);
				}
			}
			else
				str = "VRB";
		}
		return str;
	}

    // returns a visibility token string
    private static String getMETARVisibilityString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_visibility))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_visibility));
			//if (metar.hasProperty(WxConst.prop_visibilityModifier))
			//{
				//String mod = (String) metar.getProperty(WxConst.prop_visibilityModifier);
				//if (mod.length() > 0)
					//str += " (" + mod + ")";
			//}
		}
		return str;
	}

    // returns a cloud cover token string
    private static String getMETARCloudString(Entity metar)
	{
		String str = "--";
        for (int i = 1; true; i++)
        {
        	if (metar.hasProperty(WxConst.prop_cloudTypePrefix + i))
        	{
        		if (str != "--")
        			str += delimCloudStr;
        		else
        			str = "";
        		
        		// 1 char for the type
        		String type = (String) metar.getProperty(WxConst.prop_cloudTypePrefix + i);
        		if (type.compareToIgnoreCase("SKC") == 0 || type.compareToIgnoreCase("CLR") == 0)
        			str += "C";
        		else if (type.compareToIgnoreCase("FEW") == 0)
        			str += "F";
        		else if (type.compareToIgnoreCase("SCT") == 0)
        			str += "S";
        		else if (type.compareToIgnoreCase("BKN") == 0)
        			str += "B";
        		else if (type.compareToIgnoreCase("OVC") == 0)
        			str += "O";
        		else
        			str += "?";

        		// 3 digits for the floor, if present
                long floor = (long) metar.getProperty(WxConst.prop_cloudFloorPrefix + i);
                if (floor > -1)
					str += String.format("%1$03d", floor);
        	}
        	else
        		break;
        }
		return str;
	}

    // returns a temperature token string
    private static String getMETARTemperatureString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_temperature))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_temperature));
		}
		return str;
	}

    // returns a dew point token string
    private static String getMETARDewPointString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_dewPoint))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_dewPoint));
		}
		return str;
	}

    // returns a altimeter token string
    private static String getMETARAltimeterString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_altimeter))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_altimeter));
		}
		return str;
	}

    // returns a remarks token string
    private static String getMETARRemarksString(Entity metar)
	{
    	// for now, just return an empty remarks string
		String str = "--";
		//if (metar.hasProperty(WxConst.prop_remarks))
		//{
			//str = (String) metar.getProperty(WxConst.prop_remarks);
		//}
		return str;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		// parse arguments
		String airportID = req.getParameter(arg_aptid);
		int month = Integer.parseInt(req.getParameter(arg_month));
		int year = Integer.parseInt(req.getParameter(arg_year));
		log.info("Airport data requested for " + airportID + ", month=" + month + ", year=" + year);
		resp.setContentType("text/plain");

		// create the parent key for the given airport
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    Key airportKey = KeyFactory.createKey(WxConst.kind_airport, airportID);

	    // get the airport entity
	    Entity airport = null;
	    try
	    {
			airport = datastore.get(airportKey);
	    }
		catch (Exception e)
		{
			WxUtil.warn(e.toString());
		}
	    long tzOffset = (long)airport.getProperty(WxConst.prop_tzOffset);
		
	    // get the start and end dates for the query
	    Date startDate = toStartDate(month, year);
	    Date endDate = toEndDate(month, year);
		//log.info("Start date is " + startDate + ", end date is " + endDate);

		// create the query
	    Query query = new Query(WxConst.kind_metar, airportKey).addSort(WxConst.prop_date, Query.SortDirection.DESCENDING);
	    Query.Filter filter = CompositeFilterOperator.and(
	    		FilterOperator.GREATER_THAN_OR_EQUAL.of(WxConst.prop_date, startDate),
	    		FilterOperator.LESS_THAN.of(WxConst.prop_date, endDate));
	    query.setFilter(filter);

	    // process the query
	    List<Entity> metars = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		log.info("Query complete, " + metars.size() + " metar(s) found");

		String bodyStr = "";
		for (Entity metar : metars)
	    {
			// build a single metar string
	    	String metarStr = getMETARDateString(metar, tzOffset) + delimMetarStr;
	    	metarStr += getMETARWindString(metar) + delimMetarStr;
	    	metarStr += getMETARVisibilityString(metar) + delimMetarStr;
	    	metarStr += getMETARCloudString(metar) + delimMetarStr;
	    	metarStr += getMETARTemperatureString(metar) + delimMetarStr;
	    	metarStr += getMETARDewPointString(metar) + delimMetarStr;
	    	metarStr += getMETARAltimeterString(metar) + delimMetarStr;
	    	metarStr += getMETARRemarksString(metar);
	    	
	    	// add it to the body
	    	if (bodyStr.length() > 0)
	    		bodyStr += delimStr;
	    	bodyStr += metarStr;
	    }
		
		// compose the final result
		resp.getWriter().println(versionStr + delimStr + bodyStr);
	}
}
