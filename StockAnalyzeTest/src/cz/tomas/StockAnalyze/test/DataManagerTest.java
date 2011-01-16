/**
 * 
 */
package cz.tomas.StockAnalyze.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.StockSearchActivity;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
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
	
	/* 
	 * @see android.test.AndroidTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.context = new IsolatedContext(new MockContentResolver(), getContext());
		ConnectivityManager connectivity = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		
		this.dataManager = DataManager.getInstance(context);
	}



	public void testPrerequisities() {
		assertNotNull(this.dataManager);
		assertNotNull(this.context);
	}
	
	public void testLastAvailableData() throws NullPointerException, IOException {
		// get all items from prague stock exchange
		List<StockItem> items = this.dataManager.search("*", MarketFactory.getMarket("cz"));
		StockDataSqlStore sqlStore = new StockDataSqlStore(this.context);
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

}
