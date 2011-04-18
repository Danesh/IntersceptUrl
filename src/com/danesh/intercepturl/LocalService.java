package com.danesh.intercepturl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

public class LocalService extends Service {
	String filename, path, download;
	@Override
	public void onCreate() {
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}
	public void onStart(Intent intent, int startId) {
		path = intent.getExtras().getString("path");
		filename = intent.getExtras().getString("filename");
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
		path = intent.getExtras().getString("path");
		filename = intent.getExtras().getString("filename");
		runMe();
		return 1;
	}
	public void runMe(){
		SharedPreferences settings = getSharedPreferences("prefs", 0);
		if (settings.getString("download", "not")=="not"){
			download="/sdcard/download";
		}else{
			download=settings.getString("download", "not");
		}
		CharSequence title = "Downloading";
		CharSequence message = "Progress : 0";
		final NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		final Notification notification = new Notification(R.drawable.icon, "Download Started", System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, LocalService.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(LocalService.this, title, message, pendingIntent);
		notificationManager.notify(1010, notification);
		Thread fdownload = new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL(path);
					File file = new File(download+"/"+filename);
					File dir = new File(download+"/");
					if (!dir.isDirectory())
						dir.mkdir();
					file.createNewFile();
					url.openConnection();
					InputStream reader = url.openStream();
					FileOutputStream writer = new FileOutputStream(file);
					byte[] buffer = new byte[153600];
					int totalBytesRead = 0;
					int bytesRead = 0;int cc = 0;
					URLConnection ucon = url.openConnection();
					double b = ucon.getContentLength();
					while ((bytesRead = reader.read(buffer)) > 0)
					{  
						writer.write(buffer, 0, bytesRead);
						buffer = new byte[153600];
						cc++;
						totalBytesRead += bytesRead;
						notification.setLatestEventInfo(LocalService.this, "Downloading", "Progress : " + Double.toString(100*(totalBytesRead/b)).substring(0, 2)+" %", pendingIntent);
						notificationManager.notify(1010, notification);						
					}
					writer.close();
					reader.close();
				} catch (IOException e) {
				}
			}
		};
		fdownload.run();
		notificationManager.cancel(1010);
		if (settings.getBoolean("auto", false) || (!settings.contains("auto"))){
			ShellCommand command = new ShellCommand();
			if (command.canSU()){
				File down = new File(download+"/"+filename);
				if (down.exists()){
					PackageManager packageManager = getPackageManager();
					PackageInfo p = packageManager.getPackageArchiveInfo(download+"/"+filename, 0);
					if (p==null){
						displayNotification(1012,"Install Unsuccessfull","Error Parsing");
					}else{
						p.applicationInfo.sourceDir=download+"/"+filename;
						p.applicationInfo.publicSourceDir=download+"/"+filename;
						command.su.runWaitFor("pm install -r "+download+"/"+filename);
						Intent mIntent = getPackageManager().getLaunchIntentForPackage(p.packageName);
						if (mIntent != null) {
							try {
								NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
								Notification notification1 = new Notification(R.drawable.icon, "App Installed", System.currentTimeMillis());
								notification1.flags = Notification.FLAG_AUTO_CANCEL;
								PendingIntent contentIntent = PendingIntent.getActivity(this, 1014, mIntent, 0);
								notification1.setLatestEventInfo(this, "App Installed", packageManager.getApplicationLabel(p.applicationInfo).toString(), contentIntent);
								manager.notify(1014, notification1);
							} catch (ActivityNotFoundException err) {
								displayNotification(1012,"Install Unsuccessfull","Error Parsing");
							}
						}else{
							displayNotification(1012,"Install Unsuccessfull","Error Parsing");
						}
					}
				}else{
					displayNotification(1011,"Download Unsuccessful","Error");
				}
			}else{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(download+"/"+filename)), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}else{
			File down = new File(download+"/"+filename);
			if (down.exists()){
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(down), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification1 = new Notification(R.drawable.icon, "Download Completed", System.currentTimeMillis());
				notification1.flags = Notification.FLAG_AUTO_CANCEL;
				PendingIntent contentIntent = PendingIntent.getActivity(this, 1014, intent, 0);
				notification1.setLatestEventInfo(this, "Download Completed", "Tap to install", contentIntent);
				manager.notify(1014, notification1);
			}else{
				displayNotification(1013,"App Not Downloaded","Unsuccessful");
			}
		}

		stopSelf();
	}
	public void displayNotification(int id, String ticker, String msg){
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification1 = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
		notification1.flags = Notification.FLAG_AUTO_CANCEL;
		PendingIntent contentIntent = PendingIntent.getActivity(this, id, new Intent(this, LocalService.class), 0);
		notification1.setLatestEventInfo(this, ticker, msg, contentIntent);
		manager.notify(id, notification1);
	}

}