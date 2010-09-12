package cz.tomas.StockAnalyze;

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
		TabHost.TabSpec spec; 			// Resusable TabSpec for each tab
		Intent intent; 					// Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, StockListActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("StockList").setIndicator("Stock List").setContent(intent);
		
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, StockSearchActivity.class);
		spec = tabHost.newTabSpec("StockSearch").setIndicator("Stock Search").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, StockDetailActivity.class);
		spec = tabHost.newTabSpec("StockDetail").setIndicator("Stock Detail").setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
		

		this.getIntent().putExtra("ticker", "BAACEZ");
	}
}