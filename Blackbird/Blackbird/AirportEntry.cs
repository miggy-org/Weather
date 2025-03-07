using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Blackbird
{
    public class AirportEntry
    {
        private string _id;
        private string _name;
        private string _city;
        private double _lat;
        private double _lon;
        private int _tz;

        public string ID { get { return _id; } }
        public string Name { get { return _name; } }
        public string City { get { return _city; } }
        public double Lat { get { return _lat; } }
        public double Lon { get { return _lon; } }
        public int TZ { get { return _tz; } }
        public string TZAbbreviation
        {
            get
            {
                switch (_tz)
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
        }

        public AirportEntry(string id, string name, string city)
        {
            _id = id;
            _name = name;
            _city = city;
        }

        public AirportEntry(string id, string name, string city, double lat, double lon, int tz)
        {
            _id = id;
            _name = name;
            _city = city;
            _lat = lat;
            _lon = lon;
            _tz = tz;
        }
    }
}
