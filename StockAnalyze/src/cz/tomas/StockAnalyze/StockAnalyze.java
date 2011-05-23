/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cz.tomas.StockAnalyze;

import java.text.DateFormat;
import java.util.Calendar;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.activity.NewsActivity;
import cz.tomas.StockAnalyze.activity.StockDetailActivity;
import cz.tomas.StockAnalyze.activity.StockListActivity;
import cz.tomas.StockAnalyze.activity.StockSearchActivity;
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
				
				final String title = String.format("%s (%s)", getString(R.string.app_name), frm.format(cal.getTime()));
				StockAnalyze.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setTitle(title);
					}
				});
			}
		});
	}
}
