package com.jordan.airportweather;

import java.io.IOException;
import java.util.Date;
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
public class UpdateAirportServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(UpdateAirportServlet.class.getName());

    // local definitions
    private static final String arg_aptid = "aptid";
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		String airportID = req.getParameter(arg_aptid);
		log.info("Update request for " + airportID);
		resp.setContentType("text/plain");
		
		// make sure the airport ID is a valid one that this service is tracking
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key parentKey = KeyFactory.createKey(WxConst.kind_airport, airportID);
		if (!UpdateUtil.canUpdate(datastore, parentKey, airportID, new Date(), WxUtil.isInDST(new Date())))
		{
			resp.getWriter().println(airportID + " was updated too recently");
			return;
		}

        // ping the aviation weather site for the airports weather
		String urlFormat = WxUtil.getURLFormat(datastore);
		try
		{
			Entity metar = UpdateUtil.CreateMetar(urlFormat, airportID, parentKey);
			
			// put the METAR in the data store
			datastore.put(metar);
	        resp.getWriter().println("OK");
		}
		catch (Exception ex)
		{
			String ouch = ex.toString() + " (" + airportID + ")";
			resp.getWriter().println(ouch);
			log.warning(ouch);
		}
	}
}
