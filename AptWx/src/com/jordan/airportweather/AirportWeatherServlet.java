package com.jordan.airportweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class AirportWeatherServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(AirportWeatherServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String airport = req.getParameter("aptid");
		log.info("Request for " + airport);
		
		// ping the aviation weather site for the airports weather
		URL url = new URL("http://www.aviationweather.gov/adds/metars/index.php?station_ids=" + airport + "&std_trans=standard&chk_metars=on&hoursStr=most+recent+only&submitmet=Submit");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

		// find the line that contains the real data (has the ID in it)
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (line.indexOf(airport) != -1)
			{
				break;
			}
		}

		// trim off stuff before the ID, and any thing after the end of the interesting part
		if (line != null)
		{
			line = line.substring(line.indexOf(airport));
			int end = line.indexOf("</FONT>");
			if (end != -1)
			{
				line = line.substring(0, end);
			}
		}
		
		resp.setContentType("text/plain");
		if (line != null)
		{
			log.info("Raw weather string found - " + line);
			resp.getWriter().println("Source weather string is '" + line + "'");
			resp.getWriter().println("<br><br>");
		}
		else
		{
			log.info("Raw weather string not found");
			resp.getWriter().println("Weather not found for " + airport);
			return;
		}
		
		String[] items = line.split(" ");
		int cur = 0;

		// first string should match the airport ID
		if (items[cur++].compareToIgnoreCase(airport) != 0)
		{
			resp.getWriter().println("Airport ID string doesn't match");
			return;
		}
		
		// time string is next
		//  TODO: need to get current year/month/day, since the time string is local
		parseTimeString(items[cur++], resp);
		
		// the next might be the "AUTO" extension
		if (items[cur].compareToIgnoreCase("AUTO") == 0)
		{
			resp.getWriter().println("This is an AUTO generated weather report<br>");
			cur++;
		}
		
		// the next should be the winds
		while (!items[cur].endsWith("KT"))
		{
			cur++;
		}
		parseWindString(items[cur++], resp);
		
		// the next should be the visibility
		while (!items[cur].endsWith("SM"))
		{
			cur++;
		}
		parseVisString(items[cur++], resp);
		
        // the next might be an optional visibility modifier string
		while (!isCloudString(items[cur]))
		{
			resp.getWriter().println("Visibility modifier string (" + items[cur] + ") found<br>");
			cur++;
		}
		
        // the next should be a block of strings which are cloud covers
        int cloudCnt = 0;
        while (items[cur + cloudCnt].indexOf('/') == -1)
            cloudCnt++;
        for (int i = 0; i < cloudCnt; i++)
        {
        	parseCloudString(items[cur++], resp);
        }
        
        // the next should be the temp/dew point (contains a '/')
        parseTempDewString(items[cur++], resp);
        
        // the next should be the altimeter
        parseAltimeterString(items[cur++], resp);
        
        // remarks at the end
        if (cur < items.length && items[cur++].compareToIgnoreCase("RMK") == 0)
        {
        	while (cur < items.length)
        	{
    			resp.getWriter().println("Remark found (" + items[cur] + ")<br>");
    			cur++;
        	}
        }
	}
	
	// TODO: try out exception handling
	private void parseTimeString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
	{
		if (!item.endsWith("Z"))
			throw new IllegalArgumentException("Time string (" + item + ") did not end with 'Z'");

		int lday = Integer.parseInt(item.substring(0, 2));
		int hour = Integer.parseInt(item.substring(2, 4));
		int min = Integer.parseInt(item.substring(4, 6));
		
        // special case - if the day of the report is a new month, be careful
        //if (day == 1 && lday > 1)
        //{
        //    month--;
        //    if (month == 0)
        //    {
        //        year--;
        //        month = 12;
        //    }
        //}
        //data.timeEpoch = (int) (new DateTime(year, month, day, hour, min, 0) - dtEpoch).TotalSeconds;
		resp.getWriter().println("Local day is " + lday + ", hour is " + hour + ", min is " + min + "<br>");
	}
	
	private void parseWindString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
	{
		if (!item.endsWith("KT"))
			throw new IllegalArgumentException("Wind string (" + item + ") did not end with 'KT'");
		
		// the first 3 chars are either the dir or 'VRB'
		String dir = item.substring(0, 3);
		if (dir.compareToIgnoreCase("VRB") == 0)
		{
			resp.getWriter().println("Winds are variable<br>");
		}
		else
		{
			int tmp = Integer.parseInt(dir);
			resp.getWriter().println("Wind direction is " + tmp + "<br>");
		}
		
		// next 2 are the strength
		int str = Integer.parseInt(item.substring(3, 5));
		resp.getWriter().println("Wind strength is " + str + "<br>");
		
        // if a gust factor is present, then next 3 chars are 'G' and the gust
		if (item.length() == 10 && item.substring(5, 6).compareToIgnoreCase("G") == 0)
		{
			int gust = Integer.parseInt(item.substring(6, 8));
			resp.getWriter().println("Winds are gusting to " + gust + "<br>");
		}
	}
	
	private void parseVisString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
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
		resp.getWriter().println("Visibility is " + vis + "<br>");
	}
	
	private boolean isCloudString(String item)
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
	
	private void parseCloudString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
	{
		// the first 3 characters are the cloud cover type
		String type = item.substring(0, 3);
		if (type.compareToIgnoreCase("CLR") == 0 || type.compareToIgnoreCase("SKC") == 0)
		{
			// clear skies
			resp.getWriter().println("Clear skies<br>");
			return;
		}
		
        // the last 3 chars is the altitude
		int alt = Integer.parseInt(item.substring(3, 6));
		resp.getWriter().println("Cloud type is " + type + ", altitude is " + alt + "<br>");
	}
	
	private void parseTempDewString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
	{
		int index = item.indexOf("/");
		if (index == -1)
			throw new IllegalArgumentException("Temp/dew string (" + item + ") did not contain '/'");
		
		// the '/' splits the temp and dew point
		String tempStr = item.substring(0, index);
		if (tempStr.indexOf("M") != -1)
			resp.getWriter().println("Temperature is -" + tempStr.substring(1) + "<br>");
		else
			resp.getWriter().println("Temperature is " + tempStr + "<br>");
		String dewStr = item.substring(index + 1);
		if (dewStr.isEmpty())
			resp.getWriter().println("Dew point is empty<br>");
		else if (dewStr.indexOf("M") != -1)
			resp.getWriter().println("Dew point is -" + dewStr.substring(1) + "<br>");
		else
			resp.getWriter().println("Dew point is " + dewStr + "<br>");
	}
	
	private void parseAltimeterString(String item, HttpServletResponse resp)
		throws IllegalArgumentException, IOException
	{
		if (!item.startsWith("A"))
			throw new IllegalArgumentException("Altimeter string (" + item + ") did not start with 'A'");
		
        // another easy one
        int alt = Integer.parseInt(item.substring(1, 5));
		resp.getWriter().println("Altimeter is " + alt + "<br>");
	}			
}
