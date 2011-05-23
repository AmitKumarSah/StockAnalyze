/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.StockSearchActivity;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

/**
 * @author tomas
 *
 */
public class DataManagerTest extends AndroidTestCase {

	DataManager dataManager;
	Context context = null;
	StockDataSqlStore sqlStore;
	
	/* 
	 * @see android.test.AndroidTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.context = new IsolatedContext(new MockContentResolver(), getContext());
		sqlStore = new StockDataSqlStore(this.context);
		ConnectivityManager connectivity = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		this.dataManager = DataManager.getInstance(context);
	}

	public void testPrerequisities() {
		assertNotNull(this.dataManager);
		assertNotNull(this.context);
		assertNotNull(this.sqlStore);
	}
	
	public void testLastAvailableData() throws NullPointerException, IOException {
		// get all items from prague stock exchange
		List<StockItem> items = this.dataManager.search("*", MarketFactory.getMarket("cz"));
		Calendar yesterday = Calendar.getInstance();
		yesterday.roll(Calendar.DAY_OF_MONTH, false);
		
		for (StockItem stockItem : items) {
			sqlStore.insertDayData(stockItem, new DayData(1, 1, yesterday.getTime(), 1000, 1, 0, yesterday.getTimeInMillis()));
		}
		
		for (StockItem stockItem : items) {
			DayData data = this.dataManager.getLastValue(stockItem);
			assertNotNull(data);
		}
	}
	
	public void testAllData() {
		Map<String, StockItem> items = this.dataManager.getStockItems(MarketFactory.getMarket("cz"));
		Calendar cal = Utils.createDateOnlyCalendar(Calendar.getInstance());
		cal = Utils.getLastValidDate(cal);
		for (StockItem stockItem : items.values()) {
			sqlStore.insertDayData(stockItem, new DayData(1, 1, cal.getTime(), 1000, 1, 0, cal.getTimeInMillis()));
		}
		Map<StockItem, DayData> data = this.dataManager.getLastDataSet(items);
		
		assertEquals(items.size(), data.size());
	}

}
