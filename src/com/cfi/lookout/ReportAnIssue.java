package com.cfi.lookout;

import com.cfi.lookout.R;
import com.cfi.lookout.issues.CreateIssueRequest;
import com.cfi.lookout.issues.Issue;
import com.cfi.lookout.issues.IssueResponse;
import com.cfi.lookout.request.RequestExecCallback;
import com.cfi.lookout.request.RequestExecTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ReportAnIssue extends android.support.v4.app.FragmentActivity
{
	private String mName = null;
	private String mGenderString = "M";

    public static final int MESSAGE_PLACE_DETAILS_AVAILABLE = 201;
    public static final int MESSAGE_PLACE_DETAILS_FAILED = 202;
    public static final int MESSAGE_PLACE_DETAILS_FETCH = 203;
    public static final int MESSAGE_IMAGE_DETAILS_FETCH = 204;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MainActivity.sDetailsView = true;

        mName = getIntent().getStringExtra("name");
        setContentView(R.layout.reportanissue);
        
        PopulateUIViews();
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
    	MainActivity.sDetailsView = false;
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed()
    {
    	MainActivity.sDetailsView = false;
    	finish();
    }

    Spinner mLooSpinner;
    Spinner mIssueTypeSpinner;
    private void PopulateUIViews()
    {
    	mLooSpinner = (Spinner) findViewById(R.id.loo_spinner);
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
    	        R.array.loo_array, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    	mLooSpinner.setAdapter(adapter);
    	if( mName != null )
    	{
    		for(int i = 0; i < adapter.getCount(); i ++)
    		{
	    		if( adapter.getItem(i).toString().contentEquals(mName) )
	    		{
	    			mLooSpinner.setSelection(i);	
	    		}
    		}
    	}
    	
    	mIssueTypeSpinner = (Spinner) findViewById(R.id.issue_type_spinner);
    	ArrayAdapter<CharSequence> issueTypeAdapter = ArrayAdapter.createFromResource(this,
    	        R.array.issue_type_array, android.R.layout.simple_spinner_item);
    	issueTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mIssueTypeSpinner.setAdapter(issueTypeAdapter);
    }

   public void onButtonClick(View v)
   {
		if (v.getTag().equals("submit"))
		{	
			Issue issue = new Issue();
			
			EditText commentText = (EditText) findViewById(R.id.comment_string);
			if( commentText.getText() != null )
				issue.setComment(commentText.getText().toString());

	        issue.setGender(mGenderString);
	        
	        issue.setIssueType((String)mIssueTypeSpinner.getSelectedItem());
	        
	        issue.setSource("app");
	        
	        
	        issue.setLooId(mLooSpinner.getSelectedItemPosition() + 1);
	        
	        EditText nameText = (EditText) findViewById(R.id.name_string);
	        if( nameText.getText() != null )
	        	issue.setUserId(nameText.getText().toString());

	        CreateIssueRequest request = new CreateIssueRequest(this, issue);
	        RequestExecTask<CreateIssueRequest, IssueResponse> execTask = new RequestExecTask<CreateIssueRequest, IssueResponse>(
	                new RequestExecCallback<IssueResponse>() {
	                    @Override
	                    public void onPostRequestExec(IssueResponse result) {
	                        if(result != null && result.isSuccess())
	                        {
	                            Log.e("Loo-kOut", result.getHttpStatusCode() + ": " + result.getHttpStatusText());
	                            Toast.makeText(ReportAnIssue.this, "Report submitted successfully", Toast.LENGTH_LONG).show();
	                        }
	                    }
	                }
	        );
	        execTask.execute(request);

			new AlertDialog.Builder(this)
			.setTitle("Thank you!")
			.setMessage("Thanks for reporting an issue. The concerned authority has been informed!")
			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();
				}})
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				}).show();
		}
		else if (v.getTag().equals("cancel"))
		{
			finish();
		}
		else if (v.getTag().equals("male"))
		{
			mGenderString = "M";
		}
		else if (v.getTag().equals("female"))
		{
			mGenderString = "F";
		}
		else if(v.getTag().equals("image_picker"))
		{
		}
	}
}