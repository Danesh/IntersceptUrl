package com.danesh.intercepturl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class main extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent getIntent=getIntent();
		String path = "",filename = "";
		if (getIntent!=null){
			if (getIntent.getData()!=null){
				path = getIntent.getData().toString();
				filename = getIntent.getData().getLastPathSegment();
			}			
		}
		if (path==null || filename==null || path=="" || filename==""){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("            InterceptUrl");
			alert.setMessage("Enter download folder path. For eg /sdcard/download");
			final EditText input = new EditText(this);
			alert.setView(input);
			SharedPreferences settings = getSharedPreferences("prefs", 0);
			if (settings.getString("download", "not")=="not"){
				input.setText("/sdcard/download");
			}else{
				input.setText(settings.getString("download", "not"));
			}
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences settings = getSharedPreferences("prefs", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("download",input.getText().toString());
					editor.commit();
					finish();
					showToast("Preference Saved");
				}
			});
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			alert.show();
		}else{
			Intent mIntent = new Intent(this, LocalService.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("path", path);
			mBundle.putString("filename", filename);
			mIntent.putExtras(mBundle);
			startService(mIntent);
			finish();
		}
	}
	public void onBackPressed() {
		android.os.Process.killProcess(android.os.Process.myPid());
		return;
	}
	public void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}
}