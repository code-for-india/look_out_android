package com.cfi.lookout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

import com.cfi.lookout.R;
import com.cfi.lookout.xmlresultclasses.GetLocation;

class OrganizeLensView
{
	private Activity mActivity 	= null;
	public static RowInfo [] rowInfo 	= null;

    int width = 240;
    int height = 150;
    int rectWidth = 240;
    int numRows = 6;
    int startRow = 3;
    int rowHeight = 0;
    int bitmapWidth = 71;
    int mGapBetweenRects = 10;

	public RowInfo [] organize(GetLocation lc, Activity activity)
	{
		mActivity = activity;
		
		rectWidth	= activity.getResources().getDimensionPixelSize(R.dimen.lens_view_rect_width);
        mGapBetweenRects = activity.getResources().getDimensionPixelSize(R.dimen.lens_view_gap_between_rects);

		//1. Sort the array based on angle
		ArrayList<StringList> list = new ArrayList<StringList>();
		StringList item;
		int numItems = lc.count;
		if( numItems != 0 )
		{
			for(int i = numItems - 1; i >= 0; i--)
			{
				item = new StringList();
				item.message = ControllerView.lc.name[i];
				item.rating = ControllerView.lc.rating[i];
				item.latitude = ControllerView.lc.latitude[i];
				item.longitude = ControllerView.lc.longitude[i];
				item.distance = ControllerView.lc.distance[i];
				item.angle = ControllerView.lc.angle[i];
				item.reference = ControllerView.lc.reference[i];
				item.index = i;
				list.add(item);

				if( rowHeight == 0 && ControllerView.lc.bitmap[i] != null )
				{
					rowHeight = ControllerView.lc.bitmap[i].getHeight() + mGapBetweenRects;
					bitmapWidth = ControllerView.lc.bitmap[i].getWidth();
				}
		  	}
		}

		Collections.sort(list, new Comparator<StringList>()
		{
	        @Override
	        public int compare(StringList s1, StringList s2)
	        {
	        	return s1.angle > s2.angle? 1 : (s1.angle == s2.angle) ? 0 : -1;
	        }
	    });
		
		//2. Divide the screens into n rows (calculate n)

        if(android.os.Build.VERSION.SDK_INT >= 13)
        {
     	   Display display = mActivity.getWindowManager().getDefaultDisplay();
     	   Point size = new Point();
     	   display.getSize(size);
     	   width = size.x;
     	   height = size.y;
        }
        else
        {
     	   Display display = mActivity.getWindowManager().getDefaultDisplay(); 
     	   width = display.getWidth();
     	   height = display.getHeight();
        }
        
        int admob_height	= mActivity.getResources().getDimensionPixelSize(R.dimen.lens_view_admob_height);
        height -= admob_height;

        numRows = height/rowHeight;
        startRow = numRows/2;
        
        rowInfo = new RowInfo[numRows];
        
        for(int i = 0; i < numRows; i++ )
        {
        	rowInfo[i] = new RowInfo();
        	rowInfo[i].placeItem = new ArrayList<StringList>();
        	rowInfo[i].startY = new ArrayList<Integer>();
        }

		//3. Start with value x (configurable)
        for(int i = 0; i < list.size(); i++)
        {
        	boolean minus = true;
        	int subCount = 0;
        	int curRow = startRow;
        	
        	while(true)
        	{
        		if( IsSpaceAvailable(curRow, list.get(i).angle ) )
        		{
        			InsertItem(curRow, list.get(i).angle, list.get(i));
        			break;
        		}
        		subCount++;
        		if(minus)
        		{
        			curRow -= subCount;
        			minus = false;
        		}
        		else
        		{
        			curRow += subCount;
        			minus = true;
        		}
        		
        		if( curRow < 0 || curRow >= numRows )
        			break;
        	}
        }
        
        return rowInfo;
	}
	
	private boolean IsSpaceAvailable(int row, float angle)
	{
		if( rowInfo == null )
			return false;

		if( rowInfo[row].startY.size() == 0 )
			return true;

		int distance = (int) ((angle + 180)/360*width*4);

		if( distance < (rowInfo[row].startY.get(rowInfo[row].startY.size() - 1) + rectWidth + bitmapWidth + mGapBetweenRects) )
			return false;

		return true;
	}
	
	public void Clear()
	{   
		if( rowInfo == null ) return;
		
        for(int i = 0; i < numRows; i++ )
        {
        	if( rowInfo[i] == null ) return;

        	if( rowInfo[i].placeItem != null )
        		rowInfo[i].placeItem.clear();
        	
        	if( rowInfo[i].startY != null )
        		rowInfo[i].startY.clear();

        	rowInfo[i] = null;
        }
        
        rowInfo = null;
	}

	private void InsertItem(int row, float angle, StringList item)
	{
		int distance = (int) ((angle + 180)/360*width*4);
		rowInfo[row].startY.add(distance);
		rowInfo[row].placeItem.add(item);
	}
}
