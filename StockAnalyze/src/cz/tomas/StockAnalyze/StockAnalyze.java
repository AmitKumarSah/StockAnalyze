package cz.tomas.StockAnalyze;

import java.text.DateFormat;
import java.util.Calendar;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class StockAnalyze extends TabActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; 			// Reusable TabSpec for each tab
		Intent intent; 					// Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, StockListActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("StockList").setIndicator("List").setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, NewsActivity.class);
		spec = tabHost.newTabSpec("News").setIndicator("News").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, StockSearchActivity.class);
		spec = tabHost.newTabSpec("StockSearch").setIndicator("Search").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, StockDetailActivity.class);
		spec = tabHost.newTabSpec("StockDetail").setIndicator("Detail").setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
		
		// default stock to show (CEZ)
		this.getIntent().putExtra("stock_id", "CZ0005112300");
		
		DataManager manager = DataManager.getInstance(this);
		manager.addUpdateChangedListener(new IUpdateDateChangedListener() {
			
			@Override
			public void OnLastUpdateDateChanged(long updateTime) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(updateTime);
				DateFormat frm = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				
				String title = StockAnalyze.this.getString(R.string.app_name);
				title = String.format("%s (%s)", title, frm.format(cal.getTime()));
				setTitle(title);
			}
		});
	}
}