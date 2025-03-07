package com.jordan.airportweather;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class GetAirportListServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GetAirportListServlet.class.getName());
    
    private static final String versionStr = "VER1";
    private static final String delimStr = "|";
    private static final String delimSubStr = ";";
    private static final String defLastUpdateStr = "1000000000";
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		log.info("Airport list requested");
		resp.setContentType("text/plain");

        // get the root config entity
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity cfgEntity = WxUtil.getConfigEntity(datastore);

        // get the last update string
        String lastUpdate = defLastUpdateStr;
        if (cfgEntity.hasProperty(WxConst.cfg_lastUpdate))
        {
        	lastUpdate = String.valueOf((long) cfgEntity.getProperty(WxConst.cfg_lastUpdate));
        }
        
        String bodyStr = "";
        int airportCount = 0;
        String[] airports = WxUtil.getAirportList(datastore);
        for (String item : airports)
        {
    		// get the airport entity
    		Entity airport = null;
    		Key parentKey = KeyFactory.createKey(WxConst.kind_airport, item);
    		try
    		{
    			airport = datastore.get(parentKey);
    		}
            catch (EntityNotFoundException ex)
            {
            	log.warning("Airport ID " + item + " isn't tracked by this service");
            	continue;
            }

    		// compose the airport entry
    		if (bodyStr.length() > 0)
    			bodyStr += delimStr;
    		bodyStr += airport.getProperty(WxConst.prop_id) + delimSubStr;
    		bodyStr += airport.getProperty(WxConst.prop_name) + delimSubStr;
    		bodyStr += airport.getProperty(WxConst.prop_city) + delimSubStr;
    		bodyStr += airport.getProperty(WxConst.prop_latitude) + delimSubStr;
    		bodyStr += airport.getProperty(WxConst.prop_longitude) + delimSubStr;
    		bodyStr += airport.getProperty(WxConst.prop_tzOffset);
    		
    		// bump the real count
    		airportCount++;
        }
        
        // compose the final string
		String headerStr = versionStr + delimSubStr + lastUpdate + delimSubStr + airportCount;
        resp.getWriter().println(headerStr + delimStr + bodyStr);
		log.info("Sent " + airportCount + " airports, total response size is " + (headerStr.length() + bodyStr.length() + 1));
	}
}
