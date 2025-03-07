package com.jordan.raven2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class GraphView extends View
{
	public interface IGraphEvents
	{
		void onTouch(double x, double y);
	}
	
	public static class GraphPoint
	{
		public int dataPoint;
		public String detailPoint;
	}
	
	private final int DIMEN = 1000;
	private final int MARGIN = 20;
	//private final int POINT_RADIUS = 10;

	private RectShape rectShape;
	private PathShape pathAxisShape;
	private PathShape pathGuideShape;
	private PathShape pathPlotShape;
	private Paint rectPaint;
	private Paint axisPaint;
	private Paint guidePaint;
	private Paint plotPaint;
	private Paint textPaint;

	private IGraphEvents graphEventsTarget;
	private int minData;
	private int maxData;
	
	public GraphView(Context arg0)
	{
		super(arg0);
	}
	
	public GraphView(Context arg0, AttributeSet arg1)
	{
		super(arg0, arg1);
	}

	private Path initPlotPoints(SparseArray<GraphPoint> theData, int lastDay)
	{
		Path path = new Path();
		
		if (theData != null)
		{
	        float xrange = DIMEN - 2 * MARGIN;
	        float xdelta = xrange / lastDay;
	        float yrange = DIMEN - 2 * MARGIN;
	        float datarange = maxData - minData;
	        for (int i = 1; i <= lastDay - 1; i++)
	        {
	            int p1 = theData.get(i).dataPoint;
	            int p2 = theData.get(i + 1).dataPoint;
	            float x, y;
	            
	            if (i == 1)
	            {
	            	x = (i - 1) * xdelta + MARGIN;
	            	y = (1 - (p1 - minData) / datarange) * yrange + MARGIN;
	            	path.moveTo(x, y);
	            }
	            
            	x = i * xdelta + MARGIN;
            	y = (1 - (p2 - minData) / datarange) * yrange + MARGIN;
	            path.lineTo(x, y);
	        }
		}
        
        return path;
	}
	
	public void init(IGraphEvents target, SparseArray<GraphPoint> theData, int min, int max, int lastDay)
	{
		int w = getWidth();
		int h = getHeight();

		// save min/max data
		graphEventsTarget = target;
		minData = min;
		maxData = max;
		
		// background rectangle
		rectShape = new RectShape();
		rectShape.resize(w, h);
		rectPaint = new Paint();
		rectPaint.setColor(Color.DKGRAY);
		rectPaint.setAlpha(192);

		// axis lines
		Path path = new Path();
		path.moveTo(MARGIN, MARGIN);
		path.lineTo(MARGIN, DIMEN - MARGIN);
		path.lineTo(DIMEN - MARGIN, DIMEN - MARGIN);
		pathAxisShape = new PathShape(path, DIMEN, DIMEN);
		pathAxisShape.resize(w, h);
		axisPaint = new Paint();
		axisPaint.setColor(Color.WHITE);
		axisPaint.setStyle(Style.STROKE);
		axisPaint.setStrokeWidth(2);

		// horizontal guide lines
		path = new Path();
		path.moveTo(MARGIN, MARGIN);
		path.lineTo(DIMEN - MARGIN, MARGIN);
		path.moveTo(MARGIN, MARGIN + (DIMEN - MARGIN)/4);
		path.lineTo(DIMEN - MARGIN, MARGIN + (DIMEN - MARGIN)/4);
		path.moveTo(MARGIN, MARGIN + 2*(DIMEN - MARGIN)/4);
		path.lineTo(DIMEN - MARGIN, MARGIN + 2*(DIMEN - MARGIN)/4);
		path.moveTo(MARGIN, MARGIN + 3*(DIMEN - MARGIN)/4);
		path.lineTo(DIMEN - MARGIN, MARGIN + 3*(DIMEN - MARGIN)/4);
		pathGuideShape = new PathShape(path, DIMEN, DIMEN);
		pathGuideShape.resize(w, h);
		guidePaint = new Paint();
		guidePaint.setColor(Color.WHITE);
		guidePaint.setStyle(Style.STROKE);
		guidePaint.setStrokeWidth(2);
		
		// plot points
		path = initPlotPoints(theData, lastDay);
		pathPlotShape = new PathShape(path, DIMEN, DIMEN);
		pathPlotShape.resize(w, h);
		plotPaint = new Paint();
		plotPaint.setColor(Color.RED);
		plotPaint.setStyle(Style.STROKE);
		plotPaint.setStrokeWidth(5);

		// labels
		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(1);
		textPaint.setTextSize(48);
		textPaint.setTextAlign(Align.RIGHT);
		
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (rectShape != null)
			rectShape.draw(canvas, rectPaint);
		if (pathAxisShape != null)
			pathAxisShape.draw(canvas, axisPaint);
		if (pathGuideShape != null)
			pathGuideShape.draw(canvas, guidePaint);
		if (pathPlotShape != null)
			pathPlotShape.draw(canvas, plotPaint);

		if (textPaint != null)
		{
			int w = getWidth();
			int h = getHeight();
			canvas.drawText(Integer.toString(minData), (int) (0.98*w), (int) (0.95*h), textPaint);
			canvas.drawText(Integer.toString(maxData), (int) (0.98*w), (int) (0.08*h), textPaint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (rectShape != null)
			rectShape.resize(w, h);
		if (pathAxisShape != null)
			pathAxisShape.resize(w, h);
		if (pathGuideShape != null)
			pathGuideShape.resize(w, h);
		if (pathPlotShape != null)
			pathPlotShape.resize(w, h);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN && graphEventsTarget != null)
		{
            double x = (event.getX() / getWidth());
            double y = (event.getX() / getWidth());
            graphEventsTarget.onTouch(x, y);
		}
		return super.onTouchEvent(event);
	}
	
}
