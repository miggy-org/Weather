package com.jordan.raven2;

import java.util.Comparator;

public class AirportEntry implements Comparable<AirportEntry>
{
	public String ID;
	public String Name;
	public String City;
	public double Lat;
	public double Lon;
	public int TZ;

    public AirportEntry(String id, String name, String city)
    {
        ID = id;
        Name = name;
        City = city;
    }

    public AirportEntry(String id, String name, String city, double lat, double lon, int tz)
    {
        ID = id;
        Name = name;
        City = city;
        Lat = lat;
        Lon = lon;
        TZ = tz;
    }

    public String getTZAbbreviation()
	{
        switch (TZ)
        {
            case 5: return "E";
            case 6: return "C";
            case 7: return "M";
            case 8: return "P";
            case 9: return "A";
            case 10: return "H";
        }
        return "?";
	}

	@Override
	public int compareTo(AirportEntry another) {
		return (ID.compareTo(another.ID));
	}

    public static class OrderById implements Comparator<AirportEntry> {
        @Override
        public int compare(AirportEntry o1, AirportEntry o2) {
        	return (o1.ID.compareTo(o2.ID));
        }
    }

    public static class OrderByCity implements Comparator<AirportEntry> {
        @Override
        public int compare(AirportEntry o1, AirportEntry o2) {
        	return (o1.City.compareTo(o2.City));
        }
    }

    public static class OrderByTz implements Comparator<AirportEntry> {
        @Override
        public int compare(AirportEntry o1, AirportEntry o2) {
        	return (o1.TZ < o2.TZ ? -1 : (o1.TZ > o2.TZ ? 1 : 0));
        }
    }
}
