package com.jordan.raven2;

public class AptConst
{
    // URIs for the airport weather service
    public static final String uriGetAirportList = "https://apt-wx.appspot.com/client/getairportlist";
    public static final String uriGetAirportMonthList = "https://apt-wx.appspot.com/client/getairportmonthlist?aptid=%1";
    public static final String uriGetAirportMonthData = "https://apt-wx.appspot.com/client/getairportmonthdata?aptid=%1&month=%2&year=%3";
    public static final String uriGetDiagramPrefix = "https://apt-wx.appspot.com/diagrams/";
    public static final String uriMoreInfoPrefix = "https://www.airnav.com/airport/";

    // keys for extra data in an intent
    public static final String keyID = "id";
    public static final String keyName = "name";
    public static final String keyCity = "city";
    public static final String keyLatLon = "latlon";
    public static final String keyMonth = "month";
    public static final String keyYear = "year";
    public static final String keyTime = "time";
}
