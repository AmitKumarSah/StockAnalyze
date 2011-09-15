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
/**
 * 
 */
package cz.tomas.StockAnalyze.test;

import cz.tomas.StockAnalyze.activity.StockListActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;

/**
 * @author tomas
 *
 */
public class StockListActivityTest extends ActivityInstrumentationTestCase2<StockListActivity> {

	StockListActivity activity;
	ListView listView;
	View progressView;
	
	private final int STOCK_COUNT = 16;
	private final int WAIT_TIME = 20000;
	
	/**
	 * 
	 */
	public StockListActivityTest() {
		super("cz.tomas.StockAnalyze", StockListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.activity = this.getActivity();
		listView = this.activity.getListView();
		this.progressView = this.activity.findViewById(cz.tomas.StockAnalyze.R.id.actionRefreshButton);
	}
	
	public void testPreconditions() {
	      assertNotNull(this.activity);
	      assertNotNull(this.listView);
	      assertNotNull(this.progressView);
	      assertNotNull(this.activity.getListAdapter());
	}
	
//	public void testProgressView() throws InterruptedException {
//		Thread.sleep(2000);
//		assertEquals(View.VISIBLE, this.progressView.getVisibility());
//		Thread.sleep(WAIT_TIME);
//		assertEquals(View.GONE, this.progressView.getVisibility());
//	}
	
	/*
	 * test initial loading of activity - will download data and store it in db
	 */
  	public void testLoadStockList1() throws InterruptedException {
  		assertEquals(View.VISIBLE, this.progressView.getVisibility());
  		// it takes time to download data and display them
  		Thread.sleep(WAIT_TIME);
  		int count = listView.getCount();
  		
  		assertEquals(STOCK_COUNT, count);

		assertEquals(View.GONE, this.progressView.getVisibility());
  	}
  	
  	/*
  	 * test second loading of activity - should fill immediately by data from already initialized adapter
  	 */
  	public void testLoadStockList2() throws InterruptedException {
  		assertEquals(View.GONE, this.progressView.getVisibility());
  		int count = listView.getCount();
  		
  		assertEquals(STOCK_COUNT, count);
  	}
}
