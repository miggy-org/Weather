using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

// TODO: add message dialogs back in
// TODO: add response caching back in

namespace Blackbird
{
    public class AptUtil
    {
        //private const string KEY_AIRPORT_LIST_RESPONSE = "AirportList";
        //private const string KEY_AIRPORT_LIST_RESPONSE_DAY = "AirportListDay";
        //private const string KEY_AIRPORT_MONTH_LIST_RESPONSE = "AirportMonthList-%1";
        //private const string KEY_AIRPORT_MONTH_LIST_RESPONSE_DAY = "AirportMonthListDay-%1";
        //private const bool USE_CACHING = true;

        // composes a key string that uniquely identifies today
        private static string getDayString()
        {
            DateTime now = DateTime.Now;
            return now.Year + "-" + now.Month + "-" + now.Day;
        }

        // saves the airport list response to local storage
        private static void saveAirportListResponse(string resp)
        {
            /*string lastDay = getDayString();
            if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(KEY_AIRPORT_LIST_RESPONSE))
            {
                Windows.Storage.ApplicationData.Current.LocalSettings.Values[KEY_AIRPORT_LIST_RESPONSE] = resp;
                Windows.Storage.ApplicationData.Current.LocalSettings.Values[KEY_AIRPORT_LIST_RESPONSE_DAY] = lastDay;
            }
            else
            {
                Windows.Storage.ApplicationData.Current.LocalSettings.Values.Add(KEY_AIRPORT_LIST_RESPONSE, resp);
                Windows.Storage.ApplicationData.Current.LocalSettings.Values.Add(KEY_AIRPORT_LIST_RESPONSE_DAY, lastDay);
            }*/
        }

        // saves the airport month list response to local storage
        private static void saveAirportMonthListResponse(string airportID, string resp)
        {
            /*string keyData = KEY_AIRPORT_MONTH_LIST_RESPONSE.Replace("%1", airportID);
            string keyDay = KEY_AIRPORT_MONTH_LIST_RESPONSE_DAY.Replace("%1", airportID);

            string lastDay = getDayString();
            if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(keyData))
            {
                Windows.Storage.ApplicationData.Current.LocalSettings.Values[keyData] = resp;
                Windows.Storage.ApplicationData.Current.LocalSettings.Values[keyDay] = lastDay;
            }
            else
            {
                Windows.Storage.ApplicationData.Current.LocalSettings.Values.Add(keyData, resp);
                Windows.Storage.ApplicationData.Current.LocalSettings.Values.Add(keyDay, lastDay);
            }*/
        }

        // saves the airport month data response to local storage
        private static void saveAirportMonthDataResponse(string airportID, int month, int year, string resp)
        {
            /*string cacheFileName = airportID + ".txt";
            StorageFile cacheFile = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(cacheFileName, Windows.Storage.CreationCollisionOption.ReplaceExisting);
            await FileIO.WriteTextAsync(cacheFile, resp);*/
        }

        // pulls the airport list string
        private static async Task<string> getAirportListString()
        {
            // determine if we need to pull new data from the airport weather service
            bool needToPullNewData = true;
            /*if (USE_CACHING)
            { 
                string currDay = getDayString();
                if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(KEY_AIRPORT_LIST_RESPONSE_DAY))
                {
                    string lastDay = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[KEY_AIRPORT_LIST_RESPONSE_DAY];
                    if (currDay.CompareTo(lastDay) == 0)
                        needToPullNewData = false;
                }
            }*/

            string airportListString = "";
            if (needToPullNewData)
            {
                using (HttpClient http = new HttpClient())
                {
                    try
                    {
                        airportListString = await http.GetStringAsync(new Uri(AptConst.uriGetAirportList));
                        saveAirportListResponse(airportListString);
                    }
                    catch (Exception)
                    {
                        // use the cached version, if available
                        //if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(KEY_AIRPORT_LIST_RESPONSE))
                        //    airportListString = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[KEY_AIRPORT_LIST_RESPONSE];
                    }
                }
            }
            else
            {
                //airportListString = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[KEY_AIRPORT_LIST_RESPONSE];
            }
            return airportListString;
        }

        // gets the list of airports
        public static async Task<List<AirportEntry>> getAirportList()
        {
            List<AirportEntry> items = new List<AirportEntry>();
            string error = "";

            try
            {
                string resp = await getAirportListString();

                string[] entries = resp.Split('|');
                for (int i = 1; i < entries.Count(); i++)
                {
                    string[] fields = entries[i].Split(';');
                    if (fields.Count() >= 6)
                    {
                        AirportEntry entry = new AirportEntry(fields[0], fields[1], fields[2], Double.Parse(fields[3]), Double.Parse(fields[4]), Int32.Parse(fields[5]));
                        items.Add(entry);
                    }
                }
            }
            catch (Exception ex)
            {
                error = ex.ToString();
            }

            if (error != "")
            {
                //MessageDialog dlg = new MessageDialog(error, "Get Airport List Error");
                //await dlg.ShowAsync();
            }

            return items;
        }

        // pulls the airport month list string
        private static async Task<string> getAirportMonthListString(string airportID)
        {
            // determine if we need to pull new data from the airport weather service
            bool needToPullNewData = true;
            /*string currDay = getDayString();
            string keyDay = KEY_AIRPORT_MONTH_LIST_RESPONSE_DAY.Replace("%1", airportID);
            string keyData = KEY_AIRPORT_MONTH_LIST_RESPONSE.Replace("%1", airportID);
            if (USE_CACHING)
            {
                if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(keyDay))
                {
                    string lastDay = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[keyDay];
                    if (currDay.CompareTo(lastDay) == 0)
                        needToPullNewData = false;
                }
            }*/

            string monthListString = "";
            if (needToPullNewData)
            {
                // first argument is the airport ID
                string uri = AptConst.uriGetAirportMonthList;
                uri = uri.Replace("%1", airportID);

                using (HttpClient http = new HttpClient())
                {
                    try
                    {
                        monthListString = await http.GetStringAsync(new Uri(uri));
                        saveAirportMonthListResponse(airportID, monthListString);
                    }
                    catch (Exception)
                    {
                        // use the cached version, if available
                        //if (Windows.Storage.ApplicationData.Current.LocalSettings.Values.ContainsKey(keyData))
                        //    monthListString = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[keyData];
                    }
                }
            }
            else
            {
                //monthListString = (string)Windows.Storage.ApplicationData.Current.LocalSettings.Values[keyData];
            }
            return monthListString;
        }

        // gets the list of months for which we have data, given an airport
        public static async Task<List<MonthEntry>> getAirportMonthList(string airportID)
        {
            List<MonthEntry> items = new List<MonthEntry>();
            string error = "";

            try
            {
                string resp = await getAirportMonthListString(airportID);

                string[] entries = resp.Split('|');
                for (int i = 1; i < entries.Count(); i++)
                {
                    string[] fields = entries[i].Split(';');
                    if (fields.Count() >= 2)
                    {
                        MonthEntry entry = new MonthEntry(Int32.Parse(fields[0]), Int32.Parse(fields[1]));
                        items.Add(entry);
                    }
                }
            }
            catch (Exception ex)
            {
                error = ex.ToString();
            }

            if (error != "")
            {
                //MessageDialog dlg = new MessageDialog(error, "Get Airport Month List Error");
                //await dlg.ShowAsync();
            }

            return items;
        }

        // pulls the airport month data string
        private static async Task<string> getAirportMonthDataString(string airportID, int month, int year)
        {
            // TODO: determine if we need to pull new data from the airport weather service
            bool needToPullNewData = true;

            string monthDataString = "";
            if (needToPullNewData)
            {
                // arguments are airport ID, month and year
                string uri = AptConst.uriGetAirportMonthData;
                uri = uri.Replace("%1", airportID);
                uri = uri.Replace("%2", month.ToString());
                uri = uri.Replace("%3", year.ToString());

                using (HttpClient http = new HttpClient())
                {
                    try
                    {
                        monthDataString = await http.GetStringAsync(new Uri(uri));
                        saveAirportMonthDataResponse(airportID, month, year, monthDataString);
                    }
                    catch
                    {
                        // we will handle this below
                    }

                    if (monthDataString.Length == 0)
                    {
                        // load cached version
                        //string cacheFileName = airportID + ".txt";
                        //StorageFile cacheFile = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(cacheFileName, Windows.Storage.CreationCollisionOption.OpenIfExists);
                        //monthDataString = await FileIO.ReadTextAsync(cacheFile);
                    }
                }
            }
            return monthDataString;
        }

        private static void addToMetarListIfNewDay(List<Metar> listOfMetars, Metar newMetar)
        {
            if (listOfMetars.Count == 0 ||
                listOfMetars[listOfMetars.Count-1].Time.Day != newMetar.Time.Day)
            {
                listOfMetars.Add(newMetar);
            }
        }

        // gets the list of months for which we have data, given an airport
        public static async Task getAirportMonthData(string airportID, int month, int year,
            List<Metar> listMorning, List<Metar> listNoon, List<Metar> listAfternoon, List<Metar> listEvening)
        {
            string error = "";

            try
            {
                string resp = await getAirportMonthDataString(airportID, month, year);

                // response comes in descending order, we want ascending
                string[] entries = resp.Split('|');
                for (int i = entries.Count() - 1; i > 0; i--)
                {
                    try
                    {
                        Metar metar = new Metar(entries[i], month, year);
                        if (metar.Time.Hour < 10)
                            addToMetarListIfNewDay(listMorning, metar);
                        else if (metar.Time.Hour < 14)
                            addToMetarListIfNewDay(listNoon, metar);
                        else if (metar.Time.Hour < 18)
                            addToMetarListIfNewDay(listAfternoon, metar);
                        else
                            addToMetarListIfNewDay(listEvening, metar);
                    }
                    catch (Exception ex)
                    {
                        error = "Error while processing " + entries[i] + "\n\n" + ex.ToString();
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                error = ex.ToString();
            }

            if (error != "")
            {
                //MessageDialog dlg = new MessageDialog(error, "Get Airport Month Data Error");
                //await dlg.ShowAsync();
            }
        }
    }
}
