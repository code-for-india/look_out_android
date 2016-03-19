package com.cfi.lookout;

import com.cfi.lookout.R;
import com.cfi.lookout.issues.ResolveIssueRequest;
import com.cfi.lookout.issues.ResolveIssueResponse;
import com.cfi.lookout.request.RequestExecCallback;
import com.cfi.lookout.request.RequestExecTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReportedIssue extends android.support.v4.app.FragmentActivity
{
	
	String mIssueType = null;
	String mLooName = null;
	String mLooId = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MainActivity.sDetailsView = true;

       // mName = getIntent().getStringExtra("name");
        setContentView(R.layout.reported_issue);
        
        mLooName = getIntent().getExtras().getString("name");
        mLooId =  getIntent().getExtras().getString("looId");
        mIssueType = getIntent().getExtras().getString("issueType");
        
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

    private void PopulateUIViews()
    {
    	TextView looNameView = (TextView) findViewById(R.id.loo_name);
    	looNameView.setText(mLooName);
    	
    	TextView issueTypeView = (TextView) findViewById(R.id.issue_type);
    	issueTypeView.setText(mIssueType);
    }

   public void onButtonClick(View v)
   {
		if (v.getTag().equals("resolved"))
		{	
	        ResolveIssueRequest request = new ResolveIssueRequest(this, mLooId, mIssueType);
	        RequestExecTask<ResolveIssueRequest, ResolveIssueResponse> execTask = new RequestExecTask<ResolveIssueRequest, ResolveIssueResponse>(
	                new RequestExecCallback<ResolveIssueResponse>() {
	                    @Override
	                    public void onPostRequestExec(ResolveIssueResponse result) {
	                        if(result != null && result.isSuccess())
	                        {
	                            Log.e("Loo-kOut", result.getHttpStatusCode() + ": " + result.getHttpStatusText());
	                            Toast.makeText(ReportedIssue.this, "The issue has been marked resolved", Toast.LENGTH_LONG).show();
	                        }
	                    }
	                }
	        );
	        execTask.execute(request);

			new AlertDialog.Builder(this)
			.setTitle("Thank you!")
			.setMessage("The issue has been marked resolved!")
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
	}
}