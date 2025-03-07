<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.jordan.airportweather.WxConst" %>
<%@ page import="com.jordan.airportweather.WxUtil" %>
<%@ page import="com.jordan.airportweather.InfoUtil" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
  </head>

  <body>
<%
  String airportArg = request.getParameter("aptid");
  if (airportArg == null) {
%>
    <h2>Aviation Airport Weather Service Status</h2><br>

    <table border="1">
      <tr>
        <td> <b>ICAO identifier</b> </td>
        <td> <b>Name</b> </td>
        <td> <b>City</b> </td>
        <td> <b>Records (est)</b> </td>
        <td> <b>Last update</b> </td>
      </tr>
<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String[] airports = WxUtil.getAirportList(datastore);
    for (String item : airports)
    {
      Key airportKey = KeyFactory.createKey(WxConst.kind_airport, item);
      Entity airport = datastore.get(airportKey);
%>
      <tr>
        <td> <a href="info.jsp?aptid=<%= item %>"><%= item %></a> </td>
        <td> <%= airport.getProperty(WxConst.prop_name) %> </td>
        <td> <%= airport.getProperty(WxConst.prop_city) %> </td>
        <td> <%= airport.getProperty(WxConst.prop_metarCount) %> </td>
        <td> <%= airport.getProperty(WxConst.prop_lastUpdate) %> </td>
      </tr>
<%
    }
%>
    </table>
    
<%
  } else {
%>

    <h2>METARs for <%= airportArg %></h2><br>

    <table border="1">
      <tr>
        <td> <b>Date</b> </td>
        <td> <b>Wind</b> </td>
        <td> <b>Visibility</b> </td>
        <td> <b>Clouds</b> </td>
        <td> <b>Temperature</b> </td>
        <td> <b>Dew Point</b> </td>
        <td> <b>Altimeter</b> </td>
        <td> <b>Remarks</b> </td>
      </tr>
<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key airportKey = KeyFactory.createKey(WxConst.kind_airport, airportArg);

    Query query = new Query(WxConst.kind_metar, airportKey).addSort(WxConst.prop_date, Query.SortDirection.DESCENDING);
    List<Entity> metars = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
    for (Entity metar : metars)
    {
%>
      <tr>
        <td> <%= metar.getProperty(WxConst.prop_date) %> </td>
        <td> <%= InfoUtil.getMETARWindString(metar) %> </td>
        <td> <%= InfoUtil.getMETARVisibilityString(metar) %> </td>
        <td> <%= InfoUtil.getMETARCloudString(metar) %> </td>
        <td> <%= InfoUtil.getMETARTemperatureString(metar) %> </td>
        <td> <%= InfoUtil.getMETARDewPointString(metar) %> </td>
        <td> <%= InfoUtil.getMETARAltimeterString(metar) %> </td>
        <td> <%= InfoUtil.getMETARRemarksString(metar) %> </td>
      </tr>
<%
    }
%>

    </table>
    
<%
  }
%>
  </body>
</html
