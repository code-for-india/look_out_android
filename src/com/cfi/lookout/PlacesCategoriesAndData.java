package com.cfi.lookout;

import android.content.Context;
import android.graphics.Bitmap;

import com.cfi.lookout.publicLoosUtils.Loo;
import com.cfi.lookout.xmlresultclasses.*;

public class PlacesCategoriesAndData
{
	private static int total_categories 	= 10;
	private static final int PLACES_SIZE 	= 80;
	private static final int CLOSEBY_SIZE 	= 80;

	private static class CData
	{
		public GetLocation data;
	};

	private static CData [] categories_data = null;
	
	public static void Initialize()
	{
		categories_data = new CData[total_categories];
		
		for( int i = 1; i < total_categories; i ++ )
		{
			categories_data[i] = new CData();
			categories_data[i].data = new GetLocation();
			categories_data[i].data.count = 0;
		}
	}
	
	public static void AddLoos(int categoryView, Context context)
	{
  		String [] types = new String[1];
  		types[0] = "loo";

  		Loo [] loo = publicLoosUtils.getLoo();
  		
  		for(int i = 0; i < loo.length; i++)
  		{
  			categories_data[categoryView].data.latitude[i] = loo[i].latitude;
  	  		categories_data[categoryView].data.longitude[i] = loo[i].longitude;
  	  		categories_data[categoryView].data.rating[i] = loo[i].average_rating;
  	  		categories_data[categoryView].data.name[i] = loo[i].address;
  	  		categories_data[categoryView].data.icon[i] = null;
  	  		categories_data[categoryView].data.distance[i] = utils.getDistanceFromLatLonInKm(MainActivity.mLat, MainActivity.mLon, categories_data[categoryView].data.latitude[i], categories_data[categoryView].data.longitude[i]);
  	  		categories_data[categoryView].data.angle[i] = utils.GetAngle(MainActivity.mLat, MainActivity.mLon, categories_data[categoryView].data.latitude[i], categories_data[categoryView].data.longitude[i]);
  	  		categories_data[categoryView].data.type[i] = utils.GetType(types);
  	  		categories_data[categoryView].data.bitmap[i] = utils.getBitmapFromType(categories_data[categoryView].data.type[i], context);
  	  		categories_data[categoryView].data.reference[i] = "" + loo[i].id;
  		}

  		categories_data[categoryView].data.count += loo.length;
	}
	
	public static GetLocation AddData(int categoryView, Places places_data, Context context)
	{
		int MAX_SIZE = PLACES_SIZE;
		if( categoryView == MainActivity.VIEW_NEARBY_LOOS )
			MAX_SIZE = CLOSEBY_SIZE;

		if( categories_data[categoryView].data.count == 0 )
		{
			categories_data[categoryView].data.latitude = new double[MAX_SIZE];
  	  		categories_data[categoryView].data.longitude = new double[MAX_SIZE];
  	  		categories_data[categoryView].data.rating = new double[MAX_SIZE];
  	  		categories_data[categoryView].data.name = new String[MAX_SIZE];
  	  		categories_data[categoryView].data.icon = new String[MAX_SIZE];
  	  		categories_data[categoryView].data.bitmap = new Bitmap[MAX_SIZE];
  	  		categories_data[categoryView].data.distance = new float[MAX_SIZE];
  	  		categories_data[categoryView].data.angle = new float[MAX_SIZE];
  	  		categories_data[categoryView].data.type = new int[MAX_SIZE];
  	  		categories_data[categoryView].data.reference = new String[MAX_SIZE];
  	  		
  	  		AddLoos(categoryView, context);
		}
		
		for( int i = categories_data[categoryView].data.count, j = 0 ; 
				j < places_data.results.length && i < MAX_SIZE ; 
				i++, j++ )
		{
			categories_data[categoryView].data.latitude[i] = places_data.results[j].geometry.location.lat;
  	  		categories_data[categoryView].data.longitude[i] = places_data.results[j].geometry.location.lng;
  	  		categories_data[categoryView].data.rating[i] = places_data.results[j].rating;
  	  		categories_data[categoryView].data.name[i] = places_data.results[j].name;
  	  		categories_data[categoryView].data.icon[i] = places_data.results[j].icon;
  	  		categories_data[categoryView].data.distance[i] = utils.getDistanceFromLatLonInKm(MainActivity.mLat, MainActivity.mLon, categories_data[categoryView].data.latitude[i], categories_data[categoryView].data.longitude[i]);
  	  		categories_data[categoryView].data.angle[i] = utils.GetAngle(MainActivity.mLat, MainActivity.mLon, categories_data[categoryView].data.latitude[i], categories_data[categoryView].data.longitude[i]);
  	  		categories_data[categoryView].data.type[i] = utils.GetType(places_data.results[j].types);
  	  		categories_data[categoryView].data.bitmap[i] = utils.getBitmapFromType(categories_data[categoryView].data.type[i], context);
  	  		categories_data[categoryView].data.reference[i] = places_data.results[j].reference;
		}

		categories_data[categoryView].data.count += places_data.results.length;
		return categories_data[categoryView].data;
	}
	
	public static void CleanUp()
	{
		if( categories_data == null )
			return;

		for( int i = 1; i < total_categories; i ++ )
		{
			if( categories_data[i].data == null )
				continue;

			if( categories_data[i].data.count > 0 )
			{
				categories_data[i].data.latitude 	= null;
	  	  		categories_data[i].data.longitude 	= null;
	  	  		categories_data[i].data.rating 		= null;
	  	  		categories_data[i].data.name 		= null;
	  	  		categories_data[i].data.icon 		= null;
	  	  		categories_data[i].data.distance 	= null;
	  	  		categories_data[i].data.angle 		= null;
	  	  		categories_data[i].data.type 		= null;
	  	  		categories_data[i].data.bitmap 		= null;
			}
			categories_data[i].data.count = 0;
			categories_data[i].data = null;
		}
		
		categories_data = null;
	}
	
	public static void Reset()
	{
		if( categories_data == null )
			return;

		for( int i = 1; i < total_categories; i ++ )
		{
			if( categories_data[i].data == null )
				continue;
			categories_data[i].data.count = 0;
		}
	}
	
	public static void ResetSearchData()
	{
		if( categories_data == null )
			return;

		if( categories_data[MainActivity.VIEW_SEARCH_PLACES].data == null )
			return;
		categories_data[MainActivity.VIEW_SEARCH_PLACES].data.count = 0;
	}
	
	public static boolean AreDetailsAlreadyFetched(int index)
	{
		if( categories_data[index].data.count > 0 )
			return true;
		return false;
	}
	
	public static GetLocation GetDetails(int index)
	{
		return categories_data[index].data;
	}
}