using System;
using System.Collections.Generic;
using Windows.Devices.Geolocation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Blackbird.UWP
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        //private const string KEY_SELECTED_AIRPORT_ID = "SelectedAirportID";
        private readonly string aptIDToSelect = "";

        public MainPage()
        {
            this.InitializeComponent();
        }

        // sets the visibility of all of the controls that show airport details
        private void SetAirportDetailControlVisibility(Windows.UI.Xaml.Visibility vis)
        {
            mapAirport.Visibility = vis;
            btnGo.Visibility = vis;
            rectWhiteBg.Visibility = vis;
            imageDiagram.Visibility = vis;
            btnMoreInfo.Visibility = vis;
        }

        // used to populate the airport list box
        private class AirportListItem
        {
            private AirportEntry _entry;
            private Brush _bg;

            public AirportEntry Airport { get { return _entry; } }
            public Brush Background { get { return _bg; } }
            public int Index
            {
                set
                {
                    _bg = new SolidColorBrush(value % 2 == 1 ? Windows.UI.Colors.DarkSlateGray : Windows.UI.Colors.DarkSlateBlue);
                    _bg.Opacity = 0.5;
                }
            }

            public AirportListItem(AirportEntry entry)
            {
                _entry = entry;
            }
        }

        // sets the background brushes of the list items
        private void setAirportListItemBackgrounds(List<AirportListItem> items)
        {
            int count = 0;
            foreach (AirportListItem item in items)
            {
                item.Index = count++;
            }
        }

        private async void pageRoot_Loaded(object sender, RoutedEventArgs e)
        {
            // load the list of airports
            List<AirportEntry> aptEntries = await AptUtil.getAirportList();

            // populate the list view
            List<AirportListItem> items = new List<AirportListItem>();
            AirportListItem itemToSelect = null;
            foreach (AirportEntry entry in aptEntries)
            {
                AirportListItem item = new AirportListItem(entry);
                items.Add(item);

                if (entry.ID == aptIDToSelect)
                    itemToSelect = item;
            }
            items.Sort(SortByTZ);
            setAirportListItemBackgrounds(items);
            listAirports.ItemsSource = items;

            // if an airport ID was selected, do that now
            if (itemToSelect != null)
            {
                listAirports.SelectedValue = itemToSelect;
                listAirports.ScrollIntoView(itemToSelect);
            }
        }

        private void listAirports_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            AirportListItem item = (AirportListItem)listAirports.SelectedValue;
            if (item != null)
            {
                // update the map
                //Bing.Maps.Location loc = new Bing.Maps.Location(item.Airport.Lat, item.Airport.Lon);
                BasicGeoposition geop = new BasicGeoposition()
                {
                    Altitude = 0,
                    Latitude = item.Airport.Lat,
                    Longitude = item.Airport.Lon
                };
                mapAirport.Center = new Geopoint(geop);
                mapAirport.ZoomLevel = 13;

                // update the airport diagram
                string uriDiagram = AptConst.uriGetDiagramPrefix + item.Airport.ID + ".gif";
                BitmapImage bmi = new BitmapImage(new Uri(uriDiagram));
                imageDiagram.Source = bmi;

                // update the more info button
                btnMoreInfo.NavigateUri = new Uri(AptConst.uriMoreInfoPrefix + item.Airport.ID);
            }

            // show the hidden controls
            SetAirportDetailControlVisibility(item != null ? Windows.UI.Xaml.Visibility.Visible : Windows.UI.Xaml.Visibility.Collapsed);
        }

        private int SortByID(AirportListItem x, AirportListItem y)
        {
            return (x.Airport.ID.CompareTo(y.Airport.ID));
        }

        private void ID_Tapped(object sender, TappedRoutedEventArgs e)
        {
            List<AirportListItem> items = (List<AirportListItem>)listAirports.ItemsSource;
            listAirports.ItemsSource = null;

            items.Sort(SortByID);
            setAirportListItemBackgrounds(items);
            listAirports.ItemsSource = items;
        }

        private int SortByCity(AirportListItem x, AirportListItem y)
        {
            return (x.Airport.City.CompareTo(y.Airport.City));
        }

        private void City_Tapped(object sender, TappedRoutedEventArgs e)
        {
            List<AirportListItem> items = (List<AirportListItem>)listAirports.ItemsSource;
            listAirports.ItemsSource = null;

            items.Sort(SortByCity);
            setAirportListItemBackgrounds(items);
            listAirports.ItemsSource = items;
        }

        private int SortByTZ(AirportListItem x, AirportListItem y)
        {
            if (x.Airport.TZ == y.Airport.TZ)
                return SortByID(x, y);

            return (x.Airport.TZ > y.Airport.TZ ? 1 : -1);
        }

        private void TZ_Tapped(object sender, TappedRoutedEventArgs e)
        {
            List<AirportListItem> items = (List<AirportListItem>)listAirports.ItemsSource;
            listAirports.ItemsSource = null;

            items.Sort(SortByTZ);
            setAirportListItemBackgrounds(items);
            listAirports.ItemsSource = items;
        }

        private void btnGo_Click(object sender, RoutedEventArgs e)
        {
            AirportListItem item = (AirportListItem)listAirports.SelectedValue;
            if (item != null)
            {
                // go to the airport weather detail page
                if (this.Frame != null)
                    this.Frame.Navigate(typeof(InfoPage), item.Airport);
            }
        }
    }
}
