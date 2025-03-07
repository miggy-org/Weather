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

@SuppressWarnings("serial")
public class GetAirportMonthListServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GetAirportListServlet.class.getName());

    // constants
    private static final String versionStr = "VER1";
    private static final String delimStr = "|";
    private static final String delimSubStr = ";";

    // get arguments
    private static final String arg_aptid = "aptid";
    
    @SuppressWarnings("deprecation")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
    	// while the airport ID is required, we don't actually need it
		String airportID = req.getParameter(arg_aptid);
		log.info("Airport month list requested for " + airportID);
		resp.setContentType("text/plain");

		// get the configuration entity
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity cfgEntity = WxUtil.getConfigEntity(datastore);

		// get the first month for which we have data
		int month = 01;
		if (cfgEntity.hasProperty(WxConst.cfg_firstMonth))
			month = (int)cfgEntity.getProperty(WxConst.cfg_firstMonth);

		// get the first year for which we have data
		int year = 2014;
		if (cfgEntity.hasProperty(WxConst.cfg_firstYear))
			year = (int)cfgEntity.getProperty(WxConst.cfg_firstYear);
		
		// get the final month/year for which we have data
		Date now = new Date();
		int finalMonth = now.getMonth() + 1;
		int finalYear = now.getYear() + 1900;

		// compose a list of month/year combos (we assume we have data for all of them)
		String respStr = "";
		while (year < finalYear || (year == finalYear && month <= finalMonth))
		{
			if (respStr.length() > 0)
				respStr += delimStr;
			respStr += month + delimSubStr + year;
			
			month++;
			if (month > 12)
			{
				month = 1;
				year++;
			}
		}

		resp.getWriter().println(versionStr + delimStr + respStr);
	}
}
