using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Blackbird
{
    public class MonthEntry
    {
        private int _month;
        private int _year;

        public MonthEntry(int m, int y)
        {
            _month = m;
            _year = y;
        }

        public string Name
        {
            get
            {
                switch (_month)
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
        }
        public int Month { get { return _month; } }
        public int Year { get { return _year; } }
        public int LastDay
        {
            get
            {
                switch (_month)
                {
                    case 4:
                    case 6:
                    case 9:
                    case 11: return 30;
                    case 2: return (_year%4 == 0 ? 29 : 28);
                    default: return 31;
                }
            }
        }
    }
}
