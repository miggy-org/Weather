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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class DataStoreInfoServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(UpdateAirportServlet.class.getName());

    // local definitions
    private static final String arg_aptid = "aptid";
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws IOException
	{
		String airportID = req.getParameter(arg_aptid);
		log.info("Status request received (" + (airportID == null ? "global" : airportID) + ")");
		resp.setContentType("text/plain");
		
		// dump either all airports, or the given argument
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		if (airportID == null)
		{
			int totalCount = 0;
	        String[] airports = WxUtil.getAirportList(datastore);
	        for (String item : airports)
	        {
				// dump pertinent info to the browser
	    		totalCount += dumpAirportInfo(resp, datastore, item);
	        }
	
	        resp.getWriter().println(totalCount + " entries found");
		}
		else
		{
			// dump pertinent info to the browser
    		dumpAirportInfo(resp, datastore, airportID);
		}
	}

	private int dumpAirportInfo(HttpServletResponse resp, DatastoreService datastore, String airportID)
			throws IOException
	{
		// get the airport entity
		Entity airport = null;
		Key parentKey = KeyFactory.createKey(WxConst.kind_airport, airportID);
		try
		{
			airport = datastore.get(parentKey);
		}
        catch (EntityNotFoundException ex)
        {
        	log.warning("Airport ID " + airportID + " isn't tracked by this service");
			return 0;
        }

		// run the query that counts the entities found
	    Query query = new Query(WxConst.kind_metar, parentKey);
	    int count = datastore.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
        resp.getWriter().println("Airport " + airportID + " has " + count + " entries in the data store");
        resp.getWriter().println("");

        // update the estimated METAR count to the result, since we have it now
        airport.setProperty(WxConst.prop_metarCount, count);
        datastore.put(airport);

        // pull the latest METAR
        if (count > 0)
        {
	        query = new Query(WxConst.kind_metar, parentKey).addSort(WxConst.prop_date, Query.SortDirection.DESCENDING);
		    List<Entity> metars = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
		    if (metars.size() == 1)
		    {
		    	dumpMetarEntity(resp, metars.get(0));
		        resp.getWriter().println("");
		    }
        }

        resp.getWriter().println("");
        return count;
	}
	
	private void dumpMetarEntity(HttpServletResponse resp, Entity metar)
			throws IOException
	{
		resp.getWriter().println("Most recent report follows below");
		
		Date date = (Date) metar.getProperty(WxConst.prop_date);
        resp.getWriter().println("Date = " + date.toString());
        
        if (metar.getProperty(WxConst.prop_auto) != null)
            resp.getWriter().println("Auto = true");

        resp.getWriter().println("Wind direction = " + InfoUtil.getMETARWindString(metar));
        resp.getWriter().println("Visibility = " + InfoUtil.getMETARVisibilityString(metar));
        resp.getWriter().println("Clouds = " + InfoUtil.getMETARCloudString(metar));
        resp.getWriter().println("Temperature = " + InfoUtil.getMETARTemperatureString(metar));
        resp.getWriter().println("Dew point = " + InfoUtil.getMETARDewPointString(metar));
        resp.getWriter().println("Altimeter = " + InfoUtil.getMETARAltimeterString(metar));
        resp.getWriter().println("Remarks = " + InfoUtil.getMETARRemarksString(metar));
	}
}
