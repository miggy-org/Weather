package com.jordan.airportweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class UpdateAllServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(UpdateAllServlet.class.getName());
    private static boolean s_useSinglePing = true;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		resp.setContentType("text/plain");
		loadSettings();
		
		Date dtNow = new Date();
		boolean inDST = WxUtil.isInDST(dtNow);
		logHeader(inDST);
		
		// single-ping does it all w/ one hit to the METAR server
		if (s_useSinglePing)
			doSinglePing(dtNow, inDST, resp);
		else
			doMultiPing(dtNow, inDST, resp);
	}
	
	private void loadSettings()
	{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Entity cfg = WxUtil.getConfigEntity(datastore);
		if (cfg != null)
		{
			Object propSinglePing = cfg.getProperty(WxConst.cfg_singlePing);
			if (propSinglePing != null)
				s_useSinglePing = (((long) propSinglePing) > 0);
		}
	}
	
	private void doSinglePing(Date dtNow, boolean inDST, HttpServletResponse resp)
			throws IOException
	{
        // get the URL string that will be formatted into the final ping string
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String urlFormat = WxUtil.getURLFormat(datastore);

		// get the list of airport IDs
        String[] airports = WxUtil.getAirportList(datastore);
        String airportPingList = "";
        for (String item : airports)
        {
			// test to see if this airport can be updated
			Key parentKey = KeyFactory.createKey(WxConst.kind_airport, item);
			if (!UpdateUtil.canUpdate(datastore, parentKey, item, dtNow, inDST))
			{
		        resp.getWriter().println(item + " failed update check");
				continue;
			}

			if (airportPingList != "")
				airportPingList += "+";
			airportPingList += item;
        }
        
        if (airportPingList != "")
        {
	        // ping the aviation weather site for the airports weather
			String urlFinal = urlFormat.replace("%1", airportPingList);
			//URL url = new URL("http://www.aviationweather.gov/adds/metars/index.php?station_ids=" + airportID + "&std_trans=standard&chk_metars=on&hoursStr=most+recent+only&submitmet=Submit");
			URL url = new URL(urlFinal);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
	
			// parse the return page for a list of weather reports
	    	List<String> wxReports = UpdateUtil.findAndSpliceWeatherStrings(reader, airports);
	    	for (String report : wxReports)
	    	{
				try
				{
					// create a single METAR from each report
					Entity metar = UpdateUtil.CreateMetar(report, datastore);
					
					// put the METAR in the data store
					datastore.put(metar);
			        resp.getWriter().println("'" + report + "' update OK");
				}
				catch (Exception ex)
				{
					String ouch = ex.toString() + " (" + report + ")";
					resp.getWriter().println(ouch);
					log.warning(ouch);
					
					// correct the METAR count since it was already incremented
					String[] items = report.split(" ");
					String airportID = items[0];	// should be airport ID
					Key parentKey = KeyFactory.createKey(WxConst.kind_airport, airportID);
					UpdateUtil.decMetarCount(datastore, parentKey);
				}
	    	}
        }
	}
	
	private void doMultiPing(Date dtNow, boolean inDST, HttpServletResponse resp)
			throws IOException
	{
        // get the URL string that will be formatted into the final ping string
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String urlFormat = WxUtil.getURLFormat(datastore);

		// get the list of airport IDs
        String[] airports = WxUtil.getAirportList(datastore);
        for (String item : airports)
        {
			// test to see if the data store is tracking this airport
			Key parentKey = KeyFactory.createKey(WxConst.kind_airport, item);
			if (!UpdateUtil.canUpdate(datastore, parentKey, item, dtNow, inDST))
			{
		        resp.getWriter().println(item + " failed update check");
				continue;
			}
			
	        // ping the aviation weather site for the airports weather
			try
			{
				Entity metar = UpdateUtil.CreateMetar(urlFormat, item, parentKey);
				
				// put the METAR in the data store
				datastore.put(metar);
		        resp.getWriter().println(item + " update OK");
			}
			catch (Exception ex)
			{
				String ouch = ex.toString() + " (" + item + ")";
				resp.getWriter().println(ouch);
				log.warning(ouch);
				
				// correct the METAR count since it was already incremented
				UpdateUtil.decMetarCount(datastore, parentKey);
			}
        }
	}
	
	private void logHeader(boolean inDST)
	{
		log.info("Update all request received, " +
				"DST is " + (inDST ? "on" : "off") +
				", single ping is " + (s_useSinglePing ? "on" : "off"));
	}
	
	public static void updateSinglePingSetting(boolean newSinglePing)
	{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Entity cfg = WxUtil.getConfigEntity(datastore);
		if (cfg != null)
		{
			cfg.setProperty(WxConst.cfg_singlePing, (newSinglePing ? 1 : 0));
			s_useSinglePing = newSinglePing;
		}
    	log.info("Single ping is now " + (s_useSinglePing ? "on" : "off"));
	}
}
