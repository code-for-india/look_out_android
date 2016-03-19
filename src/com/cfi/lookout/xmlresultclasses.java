package com.cfi.lookout;

import android.graphics.Bitmap;

public class xmlresultclasses
{
	public static class GetLocation
	{
		double [] latitude;
		double [] longitude;
		double [] rating;
		String [] name;
		String [] icon;
		Bitmap [] bitmap;
		float  [] distance;
		float  [] angle;
		int	   [] type;
		String [] reference;
		int count;
	}

	public static class OrientationValues
	{
		public float Roll;
		public float Pitch;
		public float Azimuth;
	}

	public static class Places {
		//      private String icon;
		//      private String id;
		//      private String name;
		//      private String opening_hours;
		//      private String rating;
		//      private String reference;
		//      private String vicinity;
		//
		//      private List<Places> results;
		private String [] html_attributions;
		public String next_page_token;
		public Results [] results;

		class Results
		{
			public Geometry geometry;
			public String icon;
			public String id;
			public String name;
			public OpeningHours opening_hours;
			public double rating;
			public String reference;
			public String [] types;
			public String vicinity;

			class OpeningHours
			{
				public Boolean opening_hours;
			}

			class Geometry
			{
				public Location location;
				class Location
				{
					public double lat;
					public double lng;
				}
			}
		}
	}

	public static class PlaceDetails
	{
		private String [] html_attributions;
		public Result result;
		public String status;

		class Result
		{
			private AddressComponents [] address_components;
			public String formatted_address;
			public String formatted_phone_number;
			public Geometry geometry;
			public String icon;
			public String id;
			public String name;
			public Photos [] photos;
			public double rating;
			public String reference;
			public Reviews [] reviews;
			public String [] types;
			public String url;
			public int utc_offset;
			public String vicinity;

			class AddressComponents
			{
				private String long_name;
				private String short_name;
				private String [] types;
			}

			class Geometry
			{
				public Location location;
				class Location
				{
					public double lat;
					public double lng;
				}
			}

			class Photos
			{
				int height;
				private String [] html_attributions;
				public String photo_reference;
				int width;
			}

			class Reviews
			{
				public Aspects [] aspects;
				public String author_name;
				public String author_url;
				public String text;
				public long time;

				class Aspects 
				{
					public double rating;
					public String type;
				}
			}

		}
	}
}