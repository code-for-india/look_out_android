package com.cfi.lookout;

class publicLoosUtils
{
	static class Loo
	{
		int id;
		String address;
		float latitude;
		float longitude;
		String timing;
		String type;
		int urinal_count;
		boolean handicap_support;
		boolean paid;
		float average_rating;
	};
	
	static Loo loo[] = new Loo[10];
	
	public static Loo[] getLoo()
	{	
		for(int i = 0; i < 10; i++)
			loo[i] = new Loo();

		loo[0].id = 1;
		loo[0].address = "Hauz Khas";
		loo[0].latitude = 28.6015999f;
		loo[0].longitude = 77.1863348f;
		loo[0].timing = "0700-1800";
		loo[0].type = "Western";
		loo[0].urinal_count = 5;
		loo[0].handicap_support = true;
		loo[0].paid = false;
		loo[0].average_rating = 4.28709f;
		
		
		loo[1].id = 2;
		loo[1].address = "Pahar Ganj";
		loo[1].latitude = 28.60933403f;
		loo[1].longitude = 77.1914649f;
		loo[1].timing = "0500-1900";
		loo[1].type = "Indian";
		loo[1].urinal_count = 5;
		loo[1].handicap_support = false;
		loo[1].paid = false;
		loo[1].average_rating = 3.28388f;
		
		loo[2].id = 3;
		loo[2].address = "Saraswati Vihar";
		loo[2].latitude = 28.6045868f;
		loo[2].longitude = 77.19987631f;
		loo[2].timing = "0700-1800";
		loo[2].type = "Western";
		loo[2].urinal_count = 5;
		loo[2].handicap_support = false;
		loo[2].paid = false;
		loo[2].average_rating = 4.01144f;
		
		loo[3].id = 4;
		loo[3].address = "Sabzi Mandi";
		loo[3].latitude = 28.60846749f;
		loo[3].longitude = 77.20845938f;
		loo[3].timing = "1000-2200";
		loo[3].type = "Western";
		loo[3].urinal_count = 2;
		loo[3].handicap_support = false;
		loo[3].paid = false;
		loo[3].average_rating = 3.2862f;
		
		loo[4].id = 5;
		loo[4].address = "Delhi Cantt";
		loo[4].latitude = 28.60884424f;
		loo[4].longitude = 77.2093606f;
		loo[4].timing = "1200-2400";
		loo[4].type = "Western";
		loo[4].urinal_count = 7;
		loo[4].handicap_support = true;
		loo[4].paid = true;
		loo[4].average_rating = 3.2862f;
		
		loo[5].id = 6;
		loo[5].address = "Karol Bagh";
		loo[5].latitude = 28.61016289f;
		loo[5].longitude = 77.21287966f;
		loo[5].timing = "1200-2400";
		loo[5].type = "Indian";
		loo[5].urinal_count = 4;
		loo[5].handicap_support = false;
		loo[5].paid = true;
		loo[5].average_rating = 4.97278f;
		
		loo[6].id = 7;
		loo[6].address = "Vivek Vihar";
		loo[6].latitude = 28.61747164f;
		loo[6].longitude = 77.2130084f;
		loo[6].timing = "0500-1900";
		loo[6].type = "Indian";
		loo[6].urinal_count = 2;
		loo[6].handicap_support = false;
		loo[6].paid = false;
		loo[6].average_rating = 4.20981f;
		
		loo[7].id = 8;
		loo[7].address = "Gandhi Nagar";
		loo[7].latitude = 28.61901621f;
		loo[7].longitude = 77.21923113f;
		loo[7].timing = "0500-1900";
		loo[7].type = "Indian/Western";
		loo[7].urinal_count = 7;
		loo[7].handicap_support = true;
		loo[7].paid = false;
		loo[7].average_rating = 4.23862f;
		
		loo[8].id = 9;
		loo[8].address = "Vasant Vihar";
		loo[8].latitude = 28.62395114f;
		loo[8].longitude = 77.19936132f;
		loo[8].timing = "0700-1800";
		loo[8].type = "Indian/Western";
		loo[8].urinal_count = 2;
		loo[8].handicap_support = false;
		loo[8].paid = false;
		loo[8].average_rating = 1.41688f;
		
		loo[8].id = 10;
		loo[8].address = "Seema Puri";
		loo[8].latitude = 28.61475917f;
		loo[8].longitude = 77.18944788f;
		loo[8].timing = "0500-1900";
		loo[8].type = "Indian/Western";
		loo[8].urinal_count = 3;
		loo[8].handicap_support = false;
		loo[8].paid = false;
		loo[8].average_rating = 4.37693f;
		
		return loo;
	}
	
	public static Loo getLooInfo(String id)
	{
		if( loo[0] == null )
			getLoo();

		int intId = Integer.parseInt(id);
		if( intId > 0 && intId <= loo.length)
			return loo[intId-1];
		return null;
	}
}