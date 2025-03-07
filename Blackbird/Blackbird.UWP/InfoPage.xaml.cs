using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238

namespace Blackbird.UWP
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class InfoPage : Page
    {
        private AirportEntry selectedAirport;
        private List<Metar> listMorningMetars;
        private List<Metar> listNoonMetars;
        private List<Metar> listAfternoonMetars;
        private List<Metar> listEveningMetars;
        private Graph graphWind;
        private Graph graphVisibility;
        private Graph graphClouds;
        private Graph graphTemperature;
        private Graph graphDewPoint;
        private Graph graphAltimeter;

        public InfoPage()
        {
            this.InitializeComponent();
        }

        protected override async void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);

            if (e.Parameter != null)
            {
                // set the title bar
                selectedAirport = (AirportEntry)e.Parameter;
                pageTitle.Text = selectedAirport.ID + " Weather";

                // load the list of months for this airport
                List<MonthEntry> monthEntries = await AptUtil.getAirportMonthList(selectedAirport.ID);

                // populate the list view
                List<MonthListItem> items = new List<MonthListItem>();
                foreach (MonthEntry entry in monthEntries)
                {
                    MonthListItem item = new MonthListItem(entry);
                    items.Add(item);
                }
                setMonthListItemBackgrounds(items);
                listMonths.ItemsSource = items;
            }
        }

        // used to populate the month list view
        private class MonthListItem
        {
            private MonthEntry _month;
            private Brush _bg;

            public MonthEntry Month { get { return _month; } }
            public Brush Background { get { return _bg; } }
            public int Index
            {
                set
                {
                    _bg = new SolidColorBrush(value % 2 == 1 ? Windows.UI.Colors.DarkSlateGray : Windows.UI.Colors.DarkSlateBlue);
                }
            }
            public string Name
            {
                get
                {
                    if (_month.Month == 1)
                        return _month.Name + ", " + _month.Year;
                    return _month.Name;
                }
            }

            public MonthListItem(MonthEntry m)
            {
                _month = m;
            }
        }

        // sets the background brushes of the list items
        private void setMonthListItemBackgrounds(List<MonthListItem> items)
        {
            int count = 0;
            foreach (MonthListItem item in items)
            {
                item.Index = count++;
            }
        }

        private async void updateSelectedData()
        {
            listMorningMetars = new List<Metar>();
            listNoonMetars = new List<Metar>();
            listAfternoonMetars = new List<Metar>();
            listEveningMetars = new List<Metar>();

            if (listMonths.IsEnabled)
            {
                // show the data that we are about to display
                scrollView.Visibility = Windows.UI.Xaml.Visibility.Visible;

                // disable controls while getting data
                listMonths.IsEnabled = false;
                listTime.IsEnabled = false;

                // get the data for the selected month
                MonthEntry month = ((MonthListItem)listMonths.SelectedValue).Month;
                await AptUtil.getAirportMonthData(selectedAirport.ID, month.Month, month.Year,
                    listMorningMetars, listNoonMetars, listAfternoonMetars, listEveningMetars);

                // enabled controls
                listMonths.IsEnabled = true;
                listTime.IsEnabled = true;

                // render the data
                renderSelectedData();
            }
        }

        private void listMonths_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (listTime.SelectedIndex == -1)
                listTime.SelectedIndex = 0;
            updateSelectedData();
        }

        private List<Metar> getSelectedListOfData()
        {
            ListViewItem lvItem = (ListViewItem)listTime.SelectedItem;
            switch ((string)lvItem.Tag)
            {
                case "0": return listMorningMetars;
                case "1": return listNoonMetars;
                case "2": return listAfternoonMetars;
                case "3": return listEveningMetars;
            }
            return null;
        }

        // delegate to return the wind data point for graphing
        private int getWindPoint(Metar metar)
        {
            return metar.WindStrength;
        }

        // delegate to return the wind data point details
        private string getWindPointDetail(Metar metar)
        {
            string detail = string.Format("{0:D3} @ {1} knots", metar.WindDirection, metar.WindStrength);
            if (metar.WindGust > 0)
                detail += " (gusts to " + metar.WindGust.ToString() + ")";
            return detail;
        }

        // delegate to return the visibility data point for graphing
        private int getVisibilityPoint(Metar metar)
        {
            return metar.Visibility;
        }

        // delegate to return the visibility data point details
        private string getVisibilityPointDetail(Metar metar)
        {
            return metar.Visibility.ToString() + " SM";
        }

        // delegate to return the visibility data point for graphing
        private int getCloudPoint(Metar metar)
        {
            int lowest = 250;  // 100s of feet
            foreach (Metar.Cloud cloud in metar.Clouds)
            {
                // ignore few clouds
                if (cloud.Type != Metar.Cloud.CloudType.FEW && cloud.Floor < lowest)
                    lowest = cloud.Floor;
            }
            return lowest;
        }

        // delegate to return the visibility data point details
        private string getCloudPointDetail(Metar metar)
        {
            if (metar.Clouds.Count == 0)
                return "Clear";

            string detail = "";
            foreach (Metar.Cloud cloud in metar.Clouds)
            {
                if (detail.Length > 0)
                    detail += ", ";
                detail += cloud.ToString();
            }
            return detail;
        }

        // delegate to return the visibility data point for graphing
        private int getTemperaturePoint(Metar metar)
        {
            return metar.Temperature;
        }

        // delegate to return the visibility data point details
        private string getTemperaturePointDetail(Metar metar)
        {
            return metar.Temperature.ToString() + " C";
        }

        // delegate to return the visibility data point for graphing
        private int getDewPointPoint(Metar metar)
        {
            return metar.DewPoint;
        }

        // delegate to return the visibility data point details
        private string getDewPointPointDetail(Metar metar)
        {
            return metar.DewPoint.ToString() + " C";
        }

        // delegate to return the visibility data point for graphing
        private int getAltimeterPoint(Metar metar)
        {
            return metar.Altimeter;
        }

        // delegate to return the visibility data point details
        private string getAltimeterPointDetail(Metar metar)
        {
            return (metar.Altimeter / 100.0).ToString();
        }

        private void renderSelectedData()
        {
            List<Metar> listOfData = getSelectedListOfData();
            if (listOfData != null)
            {
                MonthEntry month = ((MonthListItem)listMonths.SelectedValue).Month;

                graphWind = new Graph(windGraph, windDetail, month, listOfData, getWindPoint, getWindPointDetail);
                graphWind.Render();

                graphVisibility = new Graph(visGraph, visDetail, month, listOfData, getVisibilityPoint, getVisibilityPointDetail);
                graphVisibility.Render();

                graphClouds = new Graph(cloudGraph, cloudDetail, month, listOfData, getCloudPoint, getCloudPointDetail);
                graphClouds.Render();

                graphTemperature = new Graph(tempGraph, tempDetail, month, listOfData, getTemperaturePoint, getTemperaturePointDetail);
                graphTemperature.Render();

                graphDewPoint = new Graph(dewGraph, dewDetail, month, listOfData, getDewPointPoint, getDewPointPointDetail);
                graphDewPoint.Render();

                graphAltimeter = new Graph(altGraph, altDetail, month, listOfData, getAltimeterPoint, getAltimeterPointDetail);
                graphAltimeter.Render();
            }
        }

        private void listTime_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            renderSelectedData();
        }

        private void graph_Tapped(object sender, TappedRoutedEventArgs e)
        {
            if (sender == windGraph)
                graphWind.Tapped(e.GetPosition(windGraph));
            else if (sender == visGraph)
                graphVisibility.Tapped(e.GetPosition(visGraph));
            else if (sender == cloudGraph)
                graphClouds.Tapped(e.GetPosition(cloudGraph));
            else if (sender == tempGraph)
                graphTemperature.Tapped(e.GetPosition(tempGraph));
            else if (sender == dewGraph)
                graphDewPoint.Tapped(e.GetPosition(dewGraph));
            else if (sender == altGraph)
                graphAltimeter.Tapped(e.GetPosition(altGraph));
        }

        private void BackButton_OnClick(object sender, RoutedEventArgs e)
        {
            if (Frame.CanGoBack)
                Frame.GoBack();
        }
    }
}
