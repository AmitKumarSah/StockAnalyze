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

import android.content.Context;
import android.net.ConnectivityManager;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

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
		sqlStore = StockDataSqlStore.getInstance(this.context);
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
		Collection<StockItem> items = this.dataManager.getStockItems(Markets.GLOBAL).values();
		Calendar yesterday = Calendar.getInstance();
		yesterday.roll(Calendar.DAY_OF_MONTH, false);
		
		for (StockItem stockItem : items) {
			sqlStore.insertDayData(stockItem.getId(), new DayData(1, 1, yesterday.getTimeInMillis(), 1000, 1, 0, yesterday.getTimeInMillis()));
		}
		
		for (StockItem stockItem : items) {
			DayData data = this.dataManager.getLastValue(stockItem);
			assertNotNull(data);
		}
	}
	
	public void testAllData() {
		Map<String, StockItem> items = this.dataManager.getStockItems(Markets.GLOBAL);
		Calendar cal = Utils.createDateOnlyCalendar(Calendar.getInstance());
		cal = Utils.getLastValidDate(cal);
		for (StockItem stockItem : items.values()) {
			sqlStore.insertDayData(stockItem.getId(), new DayData(1, 1, cal.getTimeInMillis(), 1000, 1, 0, cal.getTimeInMillis()));
		}
		Map<StockItem, DayData> data = this.dataManager.getLastDataSet(Markets.GLOBAL);
		
		assertEquals(items.size(), data.size());
	}

}
