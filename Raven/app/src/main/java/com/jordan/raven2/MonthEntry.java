package com.jordan.raven2;

public class MonthEntry
{
	public int Month;
	public int Year;

	public MonthEntry(int m, int y)
	{
		Month = m;
		Year = y;
	}
	
	public String getName()
	{
        switch (Month)
        {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
        }
        return "Unknown";
	}
	
	public int getLastDay()
	{
        switch (Month)
        {
            case 4:
            case 6:
            case 9:
            case 11: return 30;
            case 2: return (Year%4 == 0 ? 29 : 28);
            default: return 31;
        }
	}
}
