package com.jordan.airportweather;

public class WxConst
{
	// entity kinds found in the data store
	public static final String kind_airport = "airport";
	public static final String kind_metar = "metar";
	public static final String kind_config = "config";
	
	// property names
	public static final String prop_id = "id";
	public static final String prop_name = "name";
	public static final String prop_city = "city";
	public static final String prop_tzOffset = "tzOffset";
	public static final String prop_latitude = "lat";
	public static final String prop_longitude = "lon";
	public static final String prop_auto = "auto";
	public static final String prop_visibilityModifier = "visMod";
	public static final String prop_date = "date";
	public static final String prop_windDirection = "windDir";
	public static final String prop_windStrength = "windStr";
	public static final String prop_windGust = "windGust";
	public static final String prop_visibility = "visibility";
	public static final String prop_cloudTypePrefix = "cloudType";
	public static final String prop_cloudFloorPrefix = "cloudFloor";
	public static final String prop_temperature = "temperature";
	public static final String prop_dewPoint = "dewPoint";
	public static final String prop_altimeter = "altimeter";
	public static final String prop_remarks = "remarks";
	public static final String prop_lastUpdate = "lastUpdate";
	public static final String prop_metarCount = "metarCount";
	
	// XML config names
	public static final String cfg_lastUpdate = "lastUpdate";
	public static final String cfg_urlFormat = "updateUrl";
	public static final String cfg_urlMoreInfoFormat = "moreInfoUrl";
	public static final String cfg_airports = "airports";
	public static final String cfg_airport = "airport";
	public static final String cfg_id = "id";
	public static final String cfg_name = "name";
	public static final String cfg_city = "city";
	public static final String cfg_startHour = "startHour";
	public static final String cfg_stopHour = "stopHour";
	public static final String cfg_updateFreq = "updateFreq";
	public static final String cfg_tzOffset = "tzOffset";
	public static final String cfg_coords = "coords";
	public static final String cfg_singlePing = "singlePing";
	public static final String cfg_purgeDays = "purgeDays";
	public static final String cfg_minElapsedTime= "minElapsedTime";
	public static final String cfg_firstMonth = "firstMonth";
	public static final String cfg_firstYear = "firstYear";
	
	// minimum time (in ms) that must elapse between updates for a single airport
	public static final long minElapsedTime = 55*60*1000;
}
