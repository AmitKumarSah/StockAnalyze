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
package cz.tomas.StockAnalyze.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.AboutActivity;
import cz.tomas.StockAnalyze.activity.AppPreferencesActivity;
import cz.tomas.StockAnalyze.activity.StockDetailActivity;
import cz.tomas.StockAnalyze.activity.StockListActivity;

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
	
	public static void goToSettings(Activity activity) {
		Intent intent = new Intent(activity, AppPreferencesActivity.class);
    	activity.startActivity(intent);
	}
	
	public static void gotToAbout(Context context) {
		Intent intent = new Intent(context, AboutActivity.class);
    	context.startActivity(intent);
	}
}
