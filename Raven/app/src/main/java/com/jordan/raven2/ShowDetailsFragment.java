package com.jordan.raven2;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.fragment.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowDetailsFragment extends Fragment
{
	String id;
	String name;
	String city;
	
	public ShowDetailsFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_details, container, false);
		return rootView;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Bundle bundle = getArguments();
		if (bundle != null)
		{
			id = bundle.getString(AptConst.keyID);
			name = bundle.getString(AptConst.keyName);
			city = bundle.getString(AptConst.keyCity);
		}
		loadData(id, name, city);
	}

	public void loadData(String id, String name, String city)
	{
		this.id = id;
		this.name = name;
		this.city = city;
		
		TextView tv = (TextView)getActivity().findViewById(R.id.airport_name);
		if (tv != null)
			tv.setText(name != null ? name : "");
		tv = (TextView)getActivity().findViewById(R.id.airport_city);
		if (tv != null)
			tv.setText(city != null ? city : "");
		
		if (id != null)
		{
			String url = AptConst.uriGetDiagramPrefix + id + ".gif";
			GetHttpImage getHttpImage = new GetHttpImage(this);
			getHttpImage.execute(url);
		}
	}
	
	// class used to load an image from an HTTP source on a separate thread
	static class GetHttpImage extends AsyncTask<String, Integer, Bitmap>
	{
		ShowDetailsFragment resultInterface;
		
		GetHttpImage(ShowDetailsFragment iface)
		{
			resultInterface = iface;
		}
		
		protected Bitmap doInBackground(String... urls)
		{
			Bitmap finalBmp = null;

			HttpURLConnection http = null;
			try
			{
				URL url = new URL(urls[0]);
				http = (HttpURLConnection) url.openConnection();
				InputStream in = http.getInputStream();
				finalBmp = BitmapFactory.decodeStream(in); 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (http != null)
					http.disconnect();
			}
			
			return finalBmp;
		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			super.onPostExecute(result);
			if (resultInterface != null)
				resultInterface.onImageLoaded(result);
		}
	}

	private void onImageLoaded(Bitmap bmp)
	{
		if (bmp != null)
		{
			ImageView imageView = (ImageView) getActivity().findViewById(R.id.airport_diagram);
			if (imageView != null)
				imageView.setImageBitmap(bmp);
		}
	}
}
