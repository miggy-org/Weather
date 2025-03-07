package com.jordan.airportweather;

import com.google.appengine.api.datastore.Entity;

public class InfoUtil
{
	public static String getMETARWindString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_windDirection))
		{
			long dir = (long) metar.getProperty(WxConst.prop_windDirection);
			if (dir != -1)
			{
				str = dir + " @ " + String.valueOf((long) metar.getProperty(WxConst.prop_windStrength)) + "KTS";
				if (metar.hasProperty(WxConst.prop_windGust))
					str += " (gust " + metar.getProperty(WxConst.prop_windGust) + "KTS)";
			}
			else
				str = "VRB";
		}
		return str;
	}

	public static String getMETARVisibilityString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_visibility))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_visibility)) + "SM";
			if (metar.hasProperty(WxConst.prop_visibilityModifier))
			{
				String mod = (String) metar.getProperty(WxConst.prop_visibilityModifier);
				if (mod.length() > 0)
					str += " (" + mod + ")";
			}
		}
		return str;
	}

	public static String getMETARCloudString(Entity metar)
	{
		String str = "--";
        for (int i = 1; true; i++)
        {
        	if (metar.hasProperty(WxConst.prop_cloudTypePrefix + i))
        	{
        		if (str != "--")
        			str += ", ";
        		else
        			str = "";
        		
        		String type = (String) metar.getProperty(WxConst.prop_cloudTypePrefix + i);
                long floor = (long) metar.getProperty(WxConst.prop_cloudFloorPrefix + i);
        		str += type;
        		if (floor > -1)
        			str += "@" + floor + "00";
        	}
        	else
        		break;
        }
		return str;
	}

	public static String getMETARTemperatureString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_temperature))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_temperature)) + "C";
		}
		return str;
	}

	public static String getMETARDewPointString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_dewPoint))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_dewPoint)) + "C";
		}
		return str;
	}

	public static String getMETARAltimeterString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_altimeter))
		{
			str = String.valueOf((long) metar.getProperty(WxConst.prop_altimeter));
		}
		return str;
	}

	public static String getMETARRemarksString(Entity metar)
	{
		String str = "--";
		if (metar.hasProperty(WxConst.prop_remarks))
		{
			str = (String) metar.getProperty(WxConst.prop_remarks);
		}
		return str;
	}

}
