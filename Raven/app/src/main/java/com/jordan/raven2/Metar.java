package com.jordan.raven2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Metar
{
	public static class Cloud
	{
		public enum CloudType { FEW, SCATTERED, BROKEN, OVERCAST };
		
		public CloudType Type;
		public int Floor;
		
		public Cloud(CloudType type, int floor)
		{
			Type = type;
			Floor = floor;
		}
		
		public Cloud(String strCloud)
		{
			char type = strCloud.charAt(0);
			switch (type) {
	            case 'F': Type = CloudType.FEW; break;
	            case 'S': Type = CloudType.SCATTERED; break;
	            case 'B': Type = CloudType.BROKEN; break;
	            case 'O': Type = CloudType.OVERCAST; break;
	        }

            if (strCloud.length() > 1)
            	Floor = Integer.parseInt(strCloud.substring(1));
		}
		
		public String toString()
		{
            switch (Type)
            {
                case FEW: return "F@" + Floor;
                case SCATTERED: return "S@" + Floor;
                case BROKEN: return "B@" + Floor;
                case OVERCAST: return "O@" + Floor;
            }
            return "NONE";
		}
	}

	// fields
    public Calendar Time;
    public int WindDirection;
    public int WindStrength;
    public int WindGust;
    public int Visibility;
	public List<Cloud> Clouds;
    public int Temperature;
    public int DewPoint;
    public int Altimeter;

    public Metar(String strMetar, int month, int year) throws Exception
    {
        Clouds = new ArrayList<Cloud>();

    	String[] elements = strMetar.split(";");
    	if (elements.length >= 7)
    	{
    		final String EMPTY_FIELD = "--";
    		
            // time stamp
            String item = elements[0];
            if (item.length() != 6)
                throw new Exception("Time stamp must be 6 characters");
            int day = Integer.parseInt(item.substring(0, 2));
            int hour = Integer.parseInt(item.substring(2, 4));
            int min = Integer.parseInt(item.substring(4, 6));
            Time = Calendar.getInstance();
            Time.set(year, month - 1, day, hour, min, 0);

            // wind
            item = elements[1];
            if (item.compareTo(EMPTY_FIELD) != 0)
            {
                if (item.length() >= 3 && item.compareTo("VRB") != 0)
                    WindDirection = Integer.parseInt(item.substring(0, 3));
                if (item.length() >= 5)
                    WindStrength = Integer.parseInt(item.substring(3, 5));
                if (item.length() >= 7)
                    WindGust = Integer.parseInt(item.substring(5, 7));
            }

            // visibility
            item = elements[2];
            if (item.compareTo(EMPTY_FIELD) != 0)
                Visibility = Integer.parseInt(item);

            // clouds
            item = elements[3];
            if (item.length() > 0 && item.compareTo("C") != 0 && item.compareTo(EMPTY_FIELD) != 0)
            {
                String[] clouds = item.split(",");
                for (int i = 0; i < clouds.length; i++)
                {
                	Clouds.add(new Cloud(clouds[i]));
                }
            }

            // temperature
            item = elements[4];
            if (item.compareTo(EMPTY_FIELD) != 0)
                Temperature = Integer.parseInt(item);

            // dew point
            item = elements[5];
            if (item.compareTo(EMPTY_FIELD) != 0)
                DewPoint = Integer.parseInt(item);

            // altimeter
            item = elements[6];
            if (item.compareTo(EMPTY_FIELD) != 0)
                Altimeter = Integer.parseInt(item);
    	}
    	else
            throw new Exception("METAR string must have at least 7 elements");
    }
}
