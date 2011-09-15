package cz.tomas.StockAnalyze.StockList;

import java.util.Map;

import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public final class SimpleStockListAdapter extends BaseAdapter {

	private StockItem[] stocks;
	//private Market market;
	private Context context;
	
	private LayoutInflater inflater;
	private IListAdapterListener<StockItem> listener;
	private boolean indeces;
	
	/**
	 * @param market
	 */
	public SimpleStockListAdapter(Context context, Market market, IListAdapterListener<StockItem> listener, boolean indeces) {
		super();
		//this.market = market;
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.listener = listener;
		this.indeces = indeces;
		
		StockListTask task = new StockListTask();
		task.execute(market);
	}

	@Override
	public int getCount() {
		if (stocks == null) {
			return 0;
		}
		return stocks.length;
	}

	@Override
	public StockItem getItem(int position) {
		if (stocks == null) {
			return null;
		}
		return stocks[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.inflater.inflate(android.R.layout.select_dialog_singlechoice, null);
		}
		
		StockItem item = this.getItem(position);
		if (item != null) {
			((TextView) convertView).setText(item.getName());
		} else {
			Log.w(Utils.LOG_TAG, "stock item is null in SimpleStockListAdapter at position " + position);
		}
		
		return convertView;
	}
	
	private class StockListTask extends AsyncTask<Market, Integer, StockItem[]> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (listener != null) {
				listener.onListLoading();
			}
		}

		@Override
		protected StockItem[] doInBackground(Market... params) {
			if (params == null || params.length != 1) {
				return null;
			}
			
			Map<String, StockItem> stockItems = null;
			try {
				Market market = params[0];
				DataManager dataManager = (DataManager) context.getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
				stockItems = dataManager.getStockItems(market, indeces);
				
				stocks = new StockItem[stockItems.size()];
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get stock list from DataManager", e);
			}
			if (stockItems != null) {
				return stockItems.values().toArray(stocks);
			} else {
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(StockItem[] result) {
			super.onPostExecute(result);
			
			notifyDataSetChanged();
			if (listener != null) {
				listener.onListLoaded(null);
			}
		}
	}

}
