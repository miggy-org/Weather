using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Blackbird
{
    public class Metar
    {
        // contains a single cloud layer
        public class Cloud
        {
            public enum CloudType { FEW, SCATTERED, BROKEN, OVERCAST };

            public CloudType Type { get; private set; }
            public int Floor { get; private set; }

            public Cloud(CloudType type, int floor)
            {
                Type = type;
                Floor = floor;
            }

            public Cloud(string strCloud)
            {
                char type = strCloud[0];
                switch (type)
                {
                    case 'F': Type = CloudType.FEW; break;
                    case 'S': Type = CloudType.SCATTERED; break;
                    case 'B': Type = CloudType.BROKEN; break;
                    case 'O': Type = CloudType.OVERCAST; break;
                }

                if (strCloud.Length > 1)
                    Floor = Int32.Parse(strCloud.Substring(1));
            }

            public override string ToString()
            {
                switch (Type)
                {
                    case CloudType.FEW: return "F@" + Floor.ToString();
                    case CloudType.SCATTERED: return "S@" + Floor.ToString();
                    case CloudType.BROKEN: return "B@" + Floor.ToString();
                    case CloudType.OVERCAST: return "O@" + Floor.ToString();
                }
                return "NONE";
            }
        }
        private List<Cloud> _clouds;

        // fields
        public DateTime Time { get; private set; }
        public int WindDirection { get; private set; }
        public int WindStrength { get; private set; }
        public int WindGust { get; private set; }
        public int Visibility { get; private set; }
        public List<Cloud> Clouds { get { return _clouds; } }
        public int Temperature { get; private set; }
        public int DewPoint { get; private set; }
        public int Altimeter { get; private set; }

        public Metar(string strMetar, int month, int year)
        {
            _clouds = new List<Cloud>();

            string[] elements = strMetar.Split(';');
            if (elements.Length >= 7)
            {
                const string EMPTY_FIELD = "--";

                // time stamp
                string item = elements[0];
                if (item.Length != 6)
                    throw new ArgumentException("Time stamp must be 6 characters");
                int day = Int32.Parse(item.Substring(0, 2));
                int hour = Int32.Parse(item.Substring(2, 2));
                int min = Int32.Parse(item.Substring(4, 2));
                Time = new DateTime(year, month, day, hour, min, 0);

                // wind
                item = elements[1];
                if (item != EMPTY_FIELD)
                {
                    if (item.Length >= 3 && item != "VRB")
                        WindDirection = Int32.Parse(item.Substring(0, 3));
                    if (item.Length >= 5)
                        WindStrength = Int32.Parse(item.Substring(3, 2));
                    if (item.Length >= 7)
                        WindGust = Int32.Parse(item.Substring(5, 2));
                }

                // visibility
                item = elements[2];
                if (item != EMPTY_FIELD)
                    Visibility = Int32.Parse(item);

                // clouds
                item = elements[3];
                if (item.Length > 0 && item != "C" && item != EMPTY_FIELD)
                {
                    string[] clouds = item.Split(',');
                    foreach (string cloud in clouds)
                    {
                        _clouds.Add(new Cloud(cloud));
                    }
                }

                // temperature
                item = elements[4];
                if (item != EMPTY_FIELD)
                    Temperature = Int32.Parse(item);

                // dew point
                item = elements[5];
                if (item != EMPTY_FIELD)
                    DewPoint = Int32.Parse(item);

                // altimeter
                item = elements[6];
                if (item != EMPTY_FIELD)
                    Altimeter = Int32.Parse(item);
            }
            else
                throw new ArgumentException("METAR string must have at least 7 elements");
        }
    }
}
