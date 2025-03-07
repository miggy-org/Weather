using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Microsoft.Phone.Tasks;

namespace BlueJay
{
    public partial class MainPage : PhoneApplicationPage
    {
        public MainPage()
        {
            InitializeComponent();
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
                    _bg = new SolidColorBrush(value % 2 == 1 ? System.Windows.Media.Colors.Green : System.Windows.Media.Colors.Blue);
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

        private async void PhoneApplicationPage_Loaded(object sender, RoutedEventArgs e)
        {
            if (listAirports.ItemsSource == null || listAirports.Items.Count == 0)
            {
                // load the list of airports
                List<AirportEntry> aptEntries = await AptUtil.getAirportList();

                // populate the list box
                List<AirportListItem> items = new List<AirportListItem>();
                foreach (AirportEntry entry in aptEntries)
                {
                    AirportListItem item = new AirportListItem(entry);
                    items.Add(item);
                }
                items.Sort(SortByTZ);
                setAirportListItemBackgrounds(items);
                listAirports.ItemsSource = items;
            }
        }

        private int SortByID(AirportListItem x, AirportListItem y)
        {
            return (x.Airport.ID.CompareTo(y.Airport.ID));
        }

        private void ID_Tapped(object sender, System.Windows.Input.GestureEventArgs e)
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

        private void City_Tapped(object sender, System.Windows.Input.GestureEventArgs e)
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

        private void TZ_Tapped(object sender, System.Windows.Input.GestureEventArgs e)
        {
            List<AirportListItem> items = (List<AirportListItem>)listAirports.ItemsSource;
            listAirports.ItemsSource = null;

            items.Sort(SortByTZ);
            setAirportListItemBackgrounds(items);
            listAirports.ItemsSource = items;
        }

        private void listAirports_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            AirportListItem item = (AirportListItem)listAirports.SelectedValue;
            if (item != null)
            {
                // update the map
                System.Device.Location.GeoCoordinate loc = new System.Device.Location.GeoCoordinate(item.Airport.Lat, item.Airport.Lon);
                mapAirport.Center = loc;
                mapAirport.ZoomLevel = 13;

                // update the airport diagram
                string uriDiagram = AptConst.uriGetDiagramPrefix + item.Airport.ID + ".gif";
                BitmapImage bmi = new BitmapImage(new Uri(uriDiagram));
                imageDiagram.Source = bmi;

                // update the more info button
                //btnMoreInfo.NavigateUri = new Uri(AptConst.uriMoreInfoPrefix + item.Airport.ID);
            }

            // pivot to the details page
            pivotMain.SelectedItem = pivotMain.Items[1];
        }

        private void goBtn_Tapped(object sender, System.Windows.Input.GestureEventArgs e)
        {
            AirportListItem item = (AirportListItem)listAirports.SelectedValue;
            if (item != null)
            {
                NavigationService.Navigate(new Uri("/InfoPage.xaml?aptid=" + item.Airport.ID, UriKind.Relative));
            }
        }

        private void infoBtn_Tapped(object sender, System.Windows.Input.GestureEventArgs e)
        {
            AirportListItem item = (AirportListItem)listAirports.SelectedValue;
            if (item != null)
            {
                WebBrowserTask webTask = new WebBrowserTask();
                webTask.Uri = new Uri(AptConst.uriMoreInfoPrefix + item.Airport.ID);
                webTask.Show();
            }
        }
    }
}