using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BlueJay
{
    class AptConst
    {
        // URIs for the airport weather service
        public const string uriGetAirportList = "http://apt-wx.appspot.com/client/getairportlist";
        public const string uriGetAirportMonthList = "http://apt-wx.appspot.com/client/getairportmonthlist?aptid=%1";
        public const string uriGetAirportMonthData = "http://apt-wx.appspot.com/client/getairportmonthdata?aptid=%1&month=%2&year=%3";

        public const string uriGetDiagramPrefix = "http://apt-wx.appspot.com/diagrams/";
        public const string uriMoreInfoPrefix = "http://www.airnav.com/airport/";
    }
}
