using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;

namespace BlueJay
{
    class Graph
    {
        private struct GraphPoint
        {
            public int dataPoint;
            public string detailPoint;
        }

        public delegate int GetGraphPoint(Metar metar);
        public delegate string GetGraphPointDetail(Metar metar);

        private const int MARGIN = 20;
        private const int POINT_RADIUS = 10;

        private Grid theGrid;
        private TextBlock theDetailText;
        private MonthEntry theMonth;
        private Dictionary<int, GraphPoint> theDataPoints;
        private int minData;
        private int maxData;
        private Ellipse detailPoint;

        public Graph(Grid _grid, TextBlock _detail, MonthEntry _month, List<Metar> _data, GetGraphPoint _getPoint, GetGraphPointDetail _getDetails)
        {
            theGrid = _grid;
            theDetailText = _detail;
            theMonth = _month;
            minData = -999;
            maxData = -999;

            // fill in the data points from the given metars
            theDataPoints = new Dictionary<int, GraphPoint>();
            foreach (Metar metar in _data)
            {
                GraphPoint pt;
                pt.dataPoint = _getPoint(metar);
                pt.detailPoint = _getDetails(metar);
                int day = metar.Time.Day;
                theDataPoints.Add(metar.Time.Day, pt);

                if (minData == -999 || minData > pt.dataPoint)
                    minData = pt.dataPoint;
                if (maxData == -999 || maxData < pt.dataPoint)
                    maxData = pt.dataPoint;
            }

            // corner case - what if all the data points are the same?
            if (minData == maxData)
            {
                minData = (int) (minData*0.8);
                maxData = (int) (maxData*1.2);
            }

            // fill in missing data points
            for (int i = 1; i <= theMonth.LastDay; i++)
            {
                if (!theDataPoints.ContainsKey(i))
                {
                    GraphPoint pt;
                    pt.dataPoint = 0;
                    pt.detailPoint = "(No data available)";
                    theDataPoints.Add(i, pt);
                }
            }

            if (theDetailText != null)
                theDetailText.Text = "";
            detailPoint = null;
        }

        private void addLine(double x1, double x2, double y1, double y2, Color color, double thickness)
        {
            Line line = new Line();
            line.X1 = x1;
            line.X2 = x2;
            line.Y1 = y1;
            line.Y2 = y2;
            line.Stroke = new SolidColorBrush(color);
            line.StrokeThickness = thickness;
            theGrid.Children.Add(line);
        }

        private void addText(string text, double size, FontWeight weight, Thickness margin, Color color)
        {
            TextBlock label = new TextBlock();
            label.Text = text;
            label.FontSize = size;
            label.FontWeight = weight;
            label.Margin = margin;
            label.Foreground = new SolidColorBrush(color);
            label.HorizontalAlignment = HorizontalAlignment.Right;
            label.VerticalAlignment = VerticalAlignment.Top;
            theGrid.Children.Add(label);
        }

        private void renderAxisLines()
        {
            addLine(MARGIN / 2, MARGIN / 2, MARGIN / 2, theGrid.ActualHeight - MARGIN / 2, Colors.DarkGray, 2);
            addLine(MARGIN / 2, theGrid.ActualWidth - MARGIN / 2, theGrid.ActualHeight - MARGIN / 2, theGrid.ActualHeight - MARGIN / 2, Colors.DarkGray, 2);
        }

        private void renderGraphBackground()
        {
            addLine(MARGIN / 2, theGrid.ActualWidth - MARGIN / 2, (theGrid.ActualHeight - MARGIN / 2) / 4, (theGrid.ActualHeight - MARGIN / 2) / 4, Colors.Gray, 1);
            addLine(MARGIN / 2, theGrid.ActualWidth - MARGIN / 2, 2 * (theGrid.ActualHeight - MARGIN / 2) / 4, 2 * (theGrid.ActualHeight - MARGIN / 2) / 4, Colors.Gray, 1);
            addLine(MARGIN / 2, theGrid.ActualWidth - MARGIN / 2, 3 * (theGrid.ActualHeight - MARGIN / 2) / 4, 3 * (theGrid.ActualHeight - MARGIN / 2) / 4, Colors.Gray, 1);
            addLine(MARGIN / 2, theGrid.ActualWidth - MARGIN / 2, MARGIN / 2, MARGIN / 2, Colors.Gray, 1);
        }

        private void renderLabels()
        {
            Thickness margin = new Thickness(theGrid.ActualWidth - 60, theGrid.ActualHeight - 40, 10, 0);
            addText(minData.ToString(), 20, FontWeights.Bold, margin, Colors.DarkGray);
            margin = new Thickness(theGrid.ActualWidth - 60, 20, 10, 0);
            addText(maxData.ToString(), 20, FontWeights.Bold, margin, Colors.DarkGray);
        }

        private void renderData()
        {
            double xrange = theGrid.ActualWidth - 2 * MARGIN;
            double xdelta = xrange / theMonth.LastDay;
            double yrange = theGrid.ActualHeight - 2 * MARGIN;
            double datarange = maxData - minData;
            for (int i = 1; i <= theMonth.LastDay - 1; i++)
            {
                int p1 = theDataPoints[i].dataPoint;
                int p2 = theDataPoints[i + 1].dataPoint;

                addLine(
                    (i - 1) * xdelta + MARGIN,
                    i * xdelta + MARGIN,
                    (1 - (p1 - minData) / datarange) * yrange + MARGIN,
                    (1 - (p2 - minData) / datarange) * yrange + MARGIN,
                    Colors.Red,
                    1);
            }
        }

        public void Render()
        {
            theGrid.Children.Clear();

            // a single rectangle acts as the background to catch pointer events
            Rectangle rect = new Rectangle();
            rect.Margin = new Thickness(MARGIN / 2);
            rect.Fill = new SolidColorBrush(Colors.DarkGray);
            rect.Opacity = 0.2;
            theGrid.Children.Add(rect);

            // X and Y axis
            renderAxisLines();

            // horizontal lines
            renderGraphBackground();

            // labels
            renderLabels();

            // actual data lines
            renderData();
        }

        public void Tapped(Point pos)
        {
            // update the detail text display
            double xrange = theGrid.ActualWidth - 2 * MARGIN;
            int day = (int)(((pos.X - MARGIN) / xrange) * theMonth.LastDay + 0.5) + 1;
            if (day <= theMonth.LastDay)
            {
                if (theDetailText != null)
                    theDetailText.Text = day.ToString() + ": " + theDataPoints[day].detailPoint;

                double yrange = theGrid.ActualHeight - 2 * MARGIN;
                double xdelta = xrange / theMonth.LastDay;
                double datarange = maxData - minData;
                double x = (day - 1) * xdelta;
                double y = (1 - (theDataPoints[day].dataPoint - minData) / datarange) * yrange;

                // move the detail point to highlight the point on the graph
                if (detailPoint == null)
                {
                    detailPoint = new Ellipse();
                    detailPoint.Opacity = 0.5;
                    detailPoint.Fill = new SolidColorBrush(Colors.Green);
                    detailPoint.Height = POINT_RADIUS * 2;
                    detailPoint.Width = POINT_RADIUS * 2;
                    theGrid.Children.Add(detailPoint);
                }
                detailPoint.Margin = new Thickness(
                    (x > xrange / 2 ? 2 * (x - xrange / 2) : 0) + MARGIN - 2 * POINT_RADIUS,
                    (y > yrange / 2 ? 2 * (y - yrange / 2) : 0) + MARGIN - 2 * POINT_RADIUS,
                    (x < xrange / 2 ? xrange - 2 * x : 0) - MARGIN + 2 * POINT_RADIUS,
                    (y < yrange / 2 ? yrange - 2 * y : 0) - MARGIN + 2 * POINT_RADIUS
                    );
            }
        }
    }
}
