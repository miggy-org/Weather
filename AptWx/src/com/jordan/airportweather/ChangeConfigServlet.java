package com.jordan.airportweather;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ChangeConfigServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ChangeConfigServlet.class.getName());
    private static final String arg_singlePing = "singlePing";
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		resp.setContentType("text/plain");

		String newUpdateFreq = req.getParameter(WxConst.cfg_updateFreq);
		if (newUpdateFreq != null)
		{
			try
			{
				UpdateUtil.updateTimeFilterVars(Long.parseLong(newUpdateFreq));
	            resp.getWriter().println("OK");
			}
			catch (NumberFormatException e)
			{
				log.warning(e.toString() + " (while trying to change update frequency)");
			}
		}
		
		String newSinglePing = req.getParameter(arg_singlePing);
		if (newSinglePing != null)
		{
			UpdateAllServlet.updateSinglePingSetting(newSinglePing.compareTo("1") == 0);
            resp.getWriter().println("OK");
		}
		
		String newElapsedTime = req.getParameter(WxConst.cfg_minElapsedTime);
		if (newElapsedTime != null)
		{
			try
			{
				UpdateUtil.updateMinElapsedTime(Long.parseLong(newElapsedTime));
	            resp.getWriter().println("OK");
			}
			catch (NumberFormatException e)
			{
				log.warning(e.toString() + " (while trying to change minimum elapsed time)");
			}
		}
	}
}
