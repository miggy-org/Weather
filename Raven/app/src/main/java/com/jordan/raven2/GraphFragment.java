package com.jordan.raven2;

import java.util.Calendar;
import java.util.List;

import com.jordan.raven2.GraphView.GraphPoint;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class GraphFragment extends Fragment implements GraphView.IGraphEvents
{
	public interface IGraphPoints
	{
		int getGraphPoint(Metar metar);
		String getGraphPointDetail(Metar metar);
	}

	protected int idFragment;
	protected int idGraph;
	protected int idText;
	
	private SparseArray<GraphPoint> theDataPoints;
	private int minData;
	private int maxData;
	private int lastDay;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(idFragment, container, false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		tryAndGraphData();
	}

	private void tryAndGraphData()
	{
		if (isAdded() && theDataPoints != null)
		{
			GraphView graphView = (GraphView) getActivity().findViewById(idGraph);
			graphView.init(this, theDataPoints, minData, maxData, lastDay);
		}
	}
	
	public void graphData(List<Metar> listData, IGraphPoints graphPoints, int last)
	{
		theDataPoints = new SparseArray<GraphPoint>();
        minData = -999;
        maxData = -999;
        lastDay = last;
        
        // build the array of data points
        for (int i = 0; i < listData.size(); i++)
        {
        	Metar metar = listData.get(i);
        	
            GraphPoint pt = new GraphPoint();
            pt.dataPoint = graphPoints.getGraphPoint(metar);
            pt.detailPoint = graphPoints.getGraphPointDetail(metar);
            int day = metar.Time.get(Calendar.DAY_OF_MONTH);
            theDataPoints.put(day, pt);

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
        for (int i = 1; i <= lastDay; i++)
        {
        	if (theDataPoints.get(i) == null)
            {
                GraphPoint pt = new GraphPoint();
                pt.dataPoint = 0;
                pt.detailPoint = "(No data available)";
                theDataPoints.put(i, pt);
            }
        }

    	// in some cases this fragment will receive it's necessary data before it's even attached
    	//  to an activity, in this case we will init the graph in onStart()
		tryAndGraphData();
	}

    public void onTouch(double x, double y)
    {
    	TextView detailTextView = (TextView) getActivity().findViewById(idText);
    	if (detailTextView != null)
    	{
	        int day = (int)(x * lastDay + 0.5) + 1;
	        if (day <= lastDay)
	        	detailTextView.setText(Integer.toString(day) + ": " + theDataPoints.get(day).detailPoint);
    	}
    }

    ////////////////////////////////////////////////////////////////////////////////
    // below are static implementations of this class for various data point types
    
    public static class GraphWindFragment extends GraphFragment
    {
    	public GraphWindFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_winds;
    		this.idGraph = R.id.windGraph;
    		this.idText = R.id.windDetail;
    	}
    }

    public static class GraphVisibilityFragment extends GraphFragment
    {
    	public GraphVisibilityFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_visibility;
    		this.idGraph = R.id.visGraph;
    		this.idText = R.id.visDetail;
    	}
    }

    public static class GraphCloudsFragment extends GraphFragment
    {
    	public GraphCloudsFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_clouds;
    		this.idGraph = R.id.cloudGraph;
    		this.idText = R.id.cloudDetail;
    	}
    }    
    
    public static class GraphTemperatureFragment extends GraphFragment
    {
    	public GraphTemperatureFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_temperature;
    		this.idGraph = R.id.tempGraph;
    		this.idText = R.id.tempDetail;
    	}
    }

    public static class GraphDewPointFragment extends GraphFragment
    {
    	public GraphDewPointFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_dewpoint;
    		this.idGraph = R.id.dewGraph;
    		this.idText = R.id.dewDetail;
    	}
    }
    
    public static class GraphAltimeterFragment extends GraphFragment
    {
    	public GraphAltimeterFragment()
    	{
    		this.idFragment = R.layout.fragment_graph_altimeter;
    		this.idGraph = R.id.altGraph;
    		this.idText = R.id.altDetail;
    	}
    }
}
