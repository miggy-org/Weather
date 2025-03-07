package com.jordan.airportweather;

import java.io.IOException;
import java.util.ArrayList;
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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@SuppressWarnings("serial")
public class PurgeDataServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(PurgeDataServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		// get the datastore service
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// compute the cutoff date
		Date dtCutoff = getCutoffDate(datastore);
		
		// get the list of airport IDs
		int metarsDeleted = 0;
        String[] airports = WxUtil.getAirportList(datastore);
        for (String item : airports)
        {
        	// for each airport, purge old METARs
        	metarsDeleted += purgeOldAirportData(datastore, item, dtCutoff);
        }
        
		resp.setContentType("text/plain");
        resp.getWriter().println(metarsDeleted + " total METAR(s) deleted");
       	log.info(metarsDeleted + " total METAR(s) deleted");
	}
	
	private Date getCutoffDate(DatastoreService ds)
	{
		// get age in days for the cutoff
		long days = 4*365 + 1;  // default of 4 years
		Entity cfg = WxUtil.getConfigEntity(ds);
		if (cfg.hasProperty(WxConst.cfg_purgeDays))
			days = (long) cfg.getProperty(WxConst.cfg_purgeDays);
		log.info("Data purge requested, cut-off is " + days + " days");
		
		// compute the cutoff date
		Date date = new Date();		// now
		long ms = date.getTime();	// converted to milliseconds
		ms -= days*24*60*60*1000;	// decremented by days
		date.setTime(ms);			// converted back to Date
		return date;
	}
	
	private int purgeOldAirportData(DatastoreService ds, String airportID, Date dtCutoff)
	{
		// create a query for METARs
		Key parentKey = KeyFactory.createKey(WxConst.kind_airport, airportID);
	    Query query = new Query(WxConst.kind_metar, parentKey);

		// filter based upon the cutoff date
		FilterPredicate fp = new FilterPredicate(
				WxConst.prop_date,
				FilterOperator.LESS_THAN,
				dtCutoff);
	    query.setFilter(fp);

	    // run the query and return a list of entities to delete
	    List<Entity> oldMetars = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
	    if (oldMetars.isEmpty())
	    {
	    	//log.info("No METARs to delete for " + airportID);
	    	return 0;
	    }

	    // prepare a list of all of the keys that reference these METARs
	    ArrayList<Key> keysToDelete = new ArrayList<Key>();
	    for (Entity toDie : oldMetars)
	    {
	    	Key key = toDie.getKey();
	    	if (key != null)
	    		keysToDelete.add(key);
	    }

	    // delete them
	    try
	    {
	    	ds.delete(keysToDelete);

	    	// update the estimated METAR count
	    	Entity airport = ds.get(parentKey);
	    	if (airport.hasProperty(WxConst.prop_metarCount))
	    	{
	    		long metarCount = (long) airport.getProperty(WxConst.prop_metarCount);
	    		metarCount -= oldMetars.size();
	    		if (metarCount < 0)
	    			metarCount = 0;
		    	airport.setProperty(WxConst.prop_metarCount, metarCount);
		    	ds.put(airport);
	    	}
	    }
	    catch (Exception e)
	    {
	    	log.warning(e.toString() + " (" + airportID + ")");
	    	return 0;
	    }
	    
    	log.info(keysToDelete.size() + " METARs deleted from " + airportID);
	    return keysToDelete.size();
	}
}
