package com.jordan.airportweather;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class WxUtil
{
    private static final Logger log = Logger.getLogger(WxUtil.class.getName());

    /////////////////////////////////////////////////////////////////////////
	// logging

    public static void info(String msg)
	{
		log.info(msg);
	}
	
	public static void warn(String msg)
	{
		log.warning(msg);
	}
	
    /////////////////////////////////////////////////////////////////////////
	// generic XML parsing
	
	public static String getTextAttribute(Element ele, String attrName)
	{
		String textVal = ele.getAttribute(attrName);
		return textVal;
	}
	
	public static int getIntAttribute(Element ele, String attrName, int defVal)
	{
		int retVal = defVal;
		try
		{
			retVal = Integer.parseInt(getTextAttribute(ele, attrName));
		}
		catch (NumberFormatException nfe)
		{
			// this is actually somewhat of a normal thing, so don't panic
			info(nfe.toString() + " caught trying to parse " + attrName);
		}
		return retVal;
	}
	
    /////////////////////////////////////////////////////////////////////////
	// configuration XML parsing

	public static Entity getConfigEntity(DatastoreService ds)
	{
		Entity cfg = null;
		
		Key cfgKey = KeyFactory.createKey(WxConst.kind_config, WxConst.kind_config);
		try
		{
			cfg = ds.get(cfgKey);
		}
		catch (Exception e)
		{
			warn(e.toString());
		}
		
		return cfg;
	}
	
	public static String[] getAirportList(DatastoreService ds)
	{
		String[] list = null;
		
		Entity cfg = getConfigEntity(ds);
		if (cfg != null)
		{
			String airports = (String) cfg.getProperty(WxConst.cfg_airports);
			list = airports.split(",");
		}
		
		return list;
	}
	
	public static String getURLFormat(DatastoreService ds)
	{
		String urlFormat = "";
		
		Entity cfg = getConfigEntity(ds);
		if (cfg != null)
		{
			urlFormat = (String) cfg.getProperty(WxConst.cfg_urlFormat);
		}
		
		return urlFormat;
	}
	
    /////////////////////////////////////////////////////////////////////////
	// time stuff

	public static boolean isInDST(Date dt)
	{
		return TimeZone.getTimeZone("PST").inDaylightTime(dt);
	}

    /////////////////////////////////////////////////////////////////////////
	// utilities related to the data store

	public static int getAirportMETARCount(DatastoreService ds, Key airportKey)
	{
		// note that is a real count, not the estimate, and can cause a big Datastore small ops hit
	    Query query = new Query(WxConst.kind_metar, airportKey);
	    return ds.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
	}

}
