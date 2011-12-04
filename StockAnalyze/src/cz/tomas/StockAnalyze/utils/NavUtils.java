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
package cz.tomas.StockAnalyze.utils;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.AddPortfolioItemActivity;
import cz.tomas.StockAnalyze.activity.AboutActivity;
import cz.tomas.StockAnalyze.activity.AppPreferencesActivity;
import cz.tomas.StockAnalyze.activity.HomeActivity;
import cz.tomas.StockAnalyze.activity.StockDetailActivity;

/*
 * util taks to do common navigation tasks
 */
public class NavUtils {
	
	public final static String STOCK_ITEM_OBJECT = "cz.tomas.Stockanalyze.StockItemObject";
	public final static String DAY_DATA_OBJECT = "cz.tomas.Stockanalyze.DayDataObject";
	
	/**
	 * Switch to StockDetailActivity with stock item selected
	 * @param stock
	 */
	public static void goToStockDetail(StockItem stock, Context context) {
		goToStockDetail(stock, null, context);
	}
	/**
	 * Switch to StockDetailActivity with stock item selected
	 * @param stock
	 */
	public static void goToStockDetail(StockItem stock, DayData data, Context context) {
		if (stock == null)
			throw new RuntimeException("goToStockDetail: Stock item can't be null!");
		Intent intent = new Intent();
		intent.putExtra(STOCK_ITEM_OBJECT, stock);
		intent.putExtra(DAY_DATA_OBJECT, data);
		intent.putExtra("stock_id", stock.getId());
		intent.putExtra("market_id", stock.getMarket());
		intent.setClass(context, StockDetailActivity.class);
		context.startActivity(intent);
	}
	

	/**
	 * Navigate to AddToPortfolioActivity and put stock item and day data to intent
	 * 
	 * @param stockItem
	 * @param data
	 */
	public static void goToAddToPortfolio(Activity activity, StockItem stockItem, DayData data) {
		Intent intent = new Intent();
		intent.putExtra(Utils.EXTRA_STOCK_ITEM, stockItem);
		intent.putExtra(Utils.EXTRA_DAY_DATA, data);
		intent.putExtra(Utils.EXTRA_MARKET_ID, stockItem.getMarket());
		intent.putExtra(Utils.EXTRA_SOURCE, activity.getClass().getName());
		intent.setClass(activity, AddPortfolioItemActivity.class);
		activity.startActivity(intent);
	}
	
	public static void goToSettings(Activity activity) {
		Intent intent = new Intent(activity, AppPreferencesActivity.class);
    	activity.startActivity(intent);
	}
	
	public static void gotToAbout(Context context) {
		Intent intent = new Intent(context, AboutActivity.class);
    	context.startActivity(intent);
	}
	
	public static void goUp(Context context, Class<? extends Activity> target) {
		Intent intent = new Intent();
		intent.setClass(context, target);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

		FlurryAgent.logEvent(Consts.FLURRY_EVENT_ACTION_UP);
	}
	
	public static void goHome(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
		
		FlurryAgent.logEvent(Consts.FLURRY_EVENT_ACTION_HOME);
	}
}
