package com.cfi.lookout;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings
{
    private static Context mContext;
    private SharedPreferences mPrefs 	= null;

    private boolean	bfirsttime	= false;
    private int bnumitems = 0;
	private int imaptype = 0;

	public void Initialize(Context context)
	{
		mContext 			= context;
		mPrefs 				= mContext.getSharedPreferences("street_lens_beta1.db", 0);

		bfirsttime 	= mPrefs.getBoolean("bfirsttime", true);
		bnumitems 	= mPrefs.getInt("bnumitems", 0);
		
		imaptype 	= mPrefs.getInt("imaptype", 0);
	}

	public void SaveSettings()
	{
		mPrefs = mContext.getSharedPreferences("street_lens_beta1.db", 0);
        SharedPreferences.Editor ed = mPrefs.edit();

		ed.putBoolean("bfirsttime", bfirsttime);
		ed.putInt("bnumitems", bnumitems);
		ed.putInt("imaptype", imaptype);

        ed.commit();
	}

	public void setRunningFirstTime(boolean value)
	{
		bfirsttime = value;
	}

	public boolean getRunningFirstTime()
	{
		return bfirsttime;
	}
		
	public void clearSearchStrings()
	{
		bnumitems = 0;
	}
	
	public int getMapType()
	{
		return imaptype;
	}
	
	public void setMapType(int type)
	{
		imaptype = type;
	}
}

