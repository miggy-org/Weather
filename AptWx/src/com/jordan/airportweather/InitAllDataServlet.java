package com.jordan.airportweather;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class InitAllDataServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(InitAllDataServlet.class.getName());
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		log.info("Initialization of the data store requested");
		resp.setContentType("text/plain");

		// copy the config.xml into an entity w/ the necessary information
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		createConfigEntity(datastore);

        // load the configuration from config.xml
		Element aptsEl = getAirportsFromXML();
        try
        {
			// get the list of airports
			NodeList nl = aptsEl.getElementsByTagName(WxConst.cfg_airport);
			if (nl != null && nl.getLength() > 0)
			{
				for (int i = 0; i < nl.getLength(); i++)
				{
					// load necessary params
					Element aptEl = (Element) nl.item(i);
					String id = WxUtil.getTextAttribute(aptEl, WxConst.cfg_id);
					String name = WxUtil.getTextAttribute(aptEl, WxConst.cfg_name);
					String city = WxUtil.getTextAttribute(aptEl, WxConst.cfg_city);
					int tzOffset = WxUtil.getIntAttribute(aptEl, WxConst.cfg_tzOffset, 8);

					// the lat/lon coords are packed into a single string
					String coords = WxUtil.getTextAttribute(aptEl, WxConst.cfg_coords);
					String[] elements = coords.split(",");
					double lat = Double.parseDouble(elements[0]);
					double lon = Double.parseDouble(elements[1]);

					// add the airport to the data store
					addAirport(id, name, city, tzOffset, lat, lon, datastore);
				}
			}

	        resp.getWriter().println("OK");
		}
        catch (Exception e)
		{
        	log.warning(e.toString() + " while trying to parse the config XML");
            resp.getWriter().println("FAILED");
		}
	}

    private void addAirport(String id, String name, String city, int tzOffset, double lat, double lon, DatastoreService datastore)
    {
        // create the key that will uniquely identify this airport in the data store
        Key key = KeyFactory.createKey(WxConst.kind_airport, id);
        
        Entity apt;
        try
        {
        	// if this succeeds, then it exists already
            apt = datastore.get(key);
            log.info("Entity for " + id + " already exists, will update if necessary");
        }
        catch (EntityNotFoundException ex)
        {
        	// didn't exist so we need to add a new one
            apt = new Entity(key);
            log.info("Entity for " + id + " does not exist, creating a new one");
        }

        // even if the entity already exists, set the properties in case we've changed them
        apt.setProperty(WxConst.prop_id, id);
		apt.setProperty(WxConst.prop_name, name);
		apt.setProperty(WxConst.prop_city, city);
		apt.setProperty(WxConst.prop_tzOffset, tzOffset);
		apt.setProperty(WxConst.prop_latitude, lat);
		apt.setProperty(WxConst.prop_longitude, lon);
		if (!apt.hasProperty(WxConst.prop_metarCount))
			apt.setProperty(WxConst.prop_metarCount, 0);
		datastore.put(apt);
    }
    
    @SuppressWarnings("deprecation")
    private void createConfigEntity(DatastoreService datastore)
    {
        // create the entity
        Key key = KeyFactory.createKey(WxConst.kind_config, "config");
        Entity cfgEntity = new Entity(key);
        
        Element el = getConfigFromXML();
		String urlFormat = el.getAttribute(WxConst.cfg_urlFormat);
		cfgEntity.setProperty(WxConst.cfg_urlFormat, urlFormat);
		int startHour = Integer.parseInt(el.getAttribute(WxConst.cfg_startHour));
		cfgEntity.setProperty(WxConst.cfg_startHour, startHour);
		int stopHour = Integer.parseInt(el.getAttribute(WxConst.cfg_stopHour));
		cfgEntity.setProperty(WxConst.cfg_stopHour, stopHour);
		int updateFreq = Integer.parseInt(el.getAttribute(WxConst.cfg_updateFreq));
		cfgEntity.setProperty(WxConst.cfg_updateFreq, updateFreq);
		int singlePing = Integer.parseInt(el.getAttribute(WxConst.cfg_singlePing));
		cfgEntity.setProperty(WxConst.cfg_singlePing, singlePing);
		int purgeDays = Integer.parseInt(el.getAttribute(WxConst.cfg_purgeDays));
		cfgEntity.setProperty(WxConst.cfg_purgeDays, purgeDays);
		cfgEntity.setProperty(WxConst.cfg_minElapsedTime, WxConst.minElapsedTime);
		String urlMoreInfoFormat = el.getAttribute(WxConst.cfg_urlMoreInfoFormat);
		cfgEntity.setProperty(WxConst.cfg_urlMoreInfoFormat, urlMoreInfoFormat);

		// get the list of airports
		Element aptsEl = getAirportsFromXML();
		NodeList nl = aptsEl.getElementsByTagName(WxConst.cfg_airport);
		if (nl != null)
		{
			String airportList = "";

			for (int i = 0; i < nl.getLength(); i++)
			{
				Element aptEl = (Element) nl.item(i);
				String id = WxUtil.getTextAttribute(aptEl, WxConst.cfg_id);
				if (id.length() > 0)
				{
					if (airportList.length() > 0)
						airportList += ",";
					airportList += id;
				}
			}
			
			cfgEntity.setProperty(WxConst.cfg_airports, airportList);
		}

		// put the current time in the entity as the last config update time
		Date now = new Date();
		long currTime = now.getTime();
		cfgEntity.setProperty(WxConst.cfg_lastUpdate, currTime);

		// set the first month/year
		cfgEntity.setProperty(WxConst.cfg_firstMonth, now.getMonth() + 1);
		cfgEntity.setProperty(WxConst.cfg_firstYear, now.getYear() + 1900);
		
		// put it in the datastore
		datastore.put(cfgEntity);
    }
	
	public static Element getConfigFromXML()
	{
		Element rootElement = null;
		
        // load the configuration from config.xml
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
        	// TODO: i uploaded this as a static file, but now it's visible to all, is this a problem?  is this the optimal way to configure the service?
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse("http://apt-wx.appspot.com/config.xml");
			//Document dom = db.parse("config.xml");
			
			// root element
			rootElement = dom.getDocumentElement();
        }
        catch (Exception e)
        {
        	log.warning(e.toString());
        }
        
		return rootElement;
	}
	
	public static Element getAirportsFromXML()
	{
		Element airportsElement = null;
		
        // load the configuration from config.xml
		Element rootElement = getConfigFromXML();
        try
        {
			// find airports element (there should be only one)
			NodeList nl = rootElement.getElementsByTagName(WxConst.cfg_airports);
			if (nl != null && nl.getLength() == 1)
			{
				airportsElement = (Element) nl.item(0);
			}
        }
        catch (Exception e)
        {
        	log.warning(e.toString());
        }
        
        return airportsElement;
	}
}
