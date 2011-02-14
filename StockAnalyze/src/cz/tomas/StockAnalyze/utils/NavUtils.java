package cz.tomas.StockAnalyze.utils;

import android.content.Context;
import android.content.Intent;
import cz.tomas.StockAnalyze.StockDetailActivity;
import cz.tomas.StockAnalyze.StockListActivity;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/*
 * util taks to do common navigation tasks
 */
public class NavUtils {
	/**
	 * Switch to StockDetailActivity with stock item selected
	 * @param stock
	 */
	public static void goToStockDetail(StockItem stock, Context context) {
		if (stock == null)
			throw new RuntimeException("goToStockDetail: Stock item can't be null!");
		Intent intent = new Intent();
		intent.putExtra("stock_id", stock.getId());
		intent.putExtra("market_id", stock.getMarket());
		intent.setClass(context, StockDetailActivity.class);
		context.startActivity(intent);
	}
}
