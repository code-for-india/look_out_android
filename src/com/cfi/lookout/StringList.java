package com.cfi.lookout;

public class StringList
{
	String 	message;
	double 	latitude;
	double 	longitude;
	double 	rating;
	float 	distance;
	float 	angle;
	String 	reference;
	int 	type;

	StringList()
	{
		message = null;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	// Used only in lens view
	int 	index;
};