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
package cz.tomas.StockAnalyze.StockList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */

public class StockListAdapter extends ArrayAdapter<StockItem> {
	 
	private DataManager dataManager;
	private LayoutInflater vi;
	
	/**
	 * field used to find out if our data is outdated in case of activity resume
	 */
	private static long lastUpdateLocalTime;
	
	/**
	 * items of list view
	 */
	private StockItem[] stockListItems;
	
	/**
	 * these are data to be used in views
	 */
	private Map<StockItem, DayData> dataSet;
	//private StockComparator comparator;
	/**
	 * listeners for changes in this adapter
	 */
	private List<IListAdapterListener<Object>> listeners;
	
	//private Boolean showIcons = true;
	
	private Market market;
	
	private final boolean includeIndeces;
	
	/**
	 * semaphore to synchronize updates to list in StockListTask
	 */
	private Semaphore semaphore;
	
	public StockListAdapter(Activity context, int textViewResourceId, final DataManager dataManager, final Market market, boolean includeIndeces) {
		super(context, textViewResourceId);
		this.dataManager = dataManager;
		this.market = market;
		this.includeIndeces = includeIndeces;
		
		//this.comparator = new StockComparator(StockCompareTypes.Name, dataManager);
		this.listeners = new ArrayList<IListAdapterListener<Object>>();

        this.vi = (LayoutInflater)	this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.setNotifyOnChange(false);
        this.semaphore = new Semaphore(1);
       
    	dataSet = new LinkedHashMap<StockItem, DayData>();
        
    	this.refreshList();
	}

	/**
	 * register in DataManager for data updates
	 * 
	 * @param context
	 */
	public void attachToData() {
		this.dataManager.addStockDataListener(dataListener);
		// check if our data got old
		long time = this.dataManager.getLastUpdateTime();
		if (lastUpdateLocalTime < time) {
			this.refreshList();
		}
		lastUpdateLocalTime = time;
	}
	
	/**
	 * unregister in DataManager
	 */
	public void detachFromData() {
		this.dataManager.removeStockDataListener(dataListener);
	}
	
	private IStockDataListener dataListener = new IStockDataListener() {
		
		@Override
		public void OnStockDataUpdated(IStockDataProvider sender, Map<String, DayData> dataMap) {
			if (sender.getAdviser().isRealTime() && getContext() != null)
				((Activity) getContext()).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Log.d(Utils.LOG_TAG, "StockList: received update notification from DataManager");
						refreshList();
					}
				});
		}
		@Override
		public void OnStockDataUpdateBegin(IStockDataProvider sender) {	
		}
		@Override
		public void OnStockDataNoUpdate(IStockDataProvider sender) {
		}
	};
	
	private void refreshList() {
		StockListTask task = new StockListTask();
        task.execute((String[]) null);
	}
	
	public DayData getDayData(StockItem stockItem) {
		if (dataSet != null)
			return dataSet.get(stockItem);
		return null;
	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
     // Handle dividers
        if(this.isDivider(position)) {
            View divider = this.vi.inflate(R.layout.stock_list_divider, null);
            TextView txt = (TextView) divider.findViewById(R.id.txtStockListDivider);
            txt.setText("Prague Stock Exchange");
            return divider;
        }
        if (v == null) {
            v = vi.inflate(R.layout.stock_list_item, null);
        }
//        View iconView = v.findViewById(R.id.iconStockItem);
//        if (! this.showIcons) {
//        	 if (iconView != null)
//        		 iconView.setVisibility(View.GONE);
//        }
        
        StockItem stock = this.getItem(position);
        if (stock != null)
        	fillView(v, stock);
        else {
        	Log.d(Utils.LOG_TAG, "stock is null, cannot create list's view at position " + position);
        }
		
        return v;
	}

	/**
	 * Get data for stock item and write them to the view
	 * @param v View to fill
	 * @param stock stock to access data to write to view
	 */
	private void fillView(View v, StockItem stock) {
		TextView txtTicker = (TextView) v.findViewById(R.id.bottomtext);
        TextView txtName = (TextView) v.findViewById(R.id.toptext);
        TextView txtPrice = (TextView) v.findViewById(R.id.righttext);
        TextView txtChange = (TextView) v.findViewById(R.id.righttext2);
        View priceGroupView = v.findViewById(R.id.pricelayout);
        
        if (txtName != null) 
        	txtName.setText(stock.getName());
        if(txtTicker != null)
        	txtTicker.setText(stock.getTicker());
        if(txtPrice != null && txtChange != null) {
        	DayData data = null;
			try {
				data = dataSet.get(stock);
			} catch (Exception e) {
				txtPrice.setText("Fail");
				if (e.getMessage() != null) {
					Log.d(Utils.LOG_TAG, e.getMessage());
				}
			}
        	if (data != null) {
				txtPrice.setText(String.valueOf(data.getPrice()));
				NumberFormat percentFormat = FormattingUtils.getPercentFormat();
				String strChange = percentFormat.format(data.getChange());
				String strAbsChange = percentFormat.format(data.getAbsChange());
				txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
				
				// set background drawable according to positive/negative price change
				if (data.getChange() > 0 && priceGroupView != null) {
					priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_green_shape));
				}
				else if (data.getChange() < 0 && priceGroupView != null) {
					priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_red_shape));
				}
				else if (priceGroupView != null) {
					priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_dark_shape));
				}
			} else {
				txtPrice.setText(R.string.InvalidData);
			}
        }
	}
	
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        boolean enabled = false;
        if(position < this.getCount()){
        	enabled = ! this.isDivider(position);
        }
        
        return enabled;
    }
    
    private boolean isDivider(int position) {
    	StockItem item = this.getItem(position);
    	if (item != null)
    		return item.getName().startsWith("-");
    	else
    		return false;
    }
	
	public void showIcons(Boolean show) {
		//this.showIcons = show;
	}
	
	public void addListAdapterListener(IListAdapterListener<Object> listener) {
		this.listeners.add(listener);
	}
	
	/*
	 * this task load all stocks from DataManager and notify the ListView,
	 * it also takes care about the progress view
	 */
	private class StockListTask extends AsyncTask<String, Integer, Map<String, StockItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			for (IListAdapterListener<Object> listener : listeners) {
				listener.onListLoading();
			}
		}

		@Override
		protected Map<String, StockItem> doInBackground(String... params) {
			//List<StockItem> items = null;
			Map<String, StockItem> items = null;
			try {
				Log.d(Utils.LOG_TAG, "adapter's semaphopre waiting queue: " + semaphore.getQueueLength());
				semaphore.acquire();
				dataManager.acquireDb(this.getClass().getName());
				// first, get all stock items we need
				try {
					items = dataManager.getStockItems(market, includeIndeces);
				} catch (Exception e) {
					String message = "Failed to get stock list. ";
					if (e.getMessage() != null)
						message += e.getMessage();
					Log.e(Utils.LOG_TAG, message, e);
				}
				try {
					if (items != null) {
						// get day data for each stock and save it
						dataSet.clear();
						dataSet = dataManager.getLastDataSet(items);
						Log.d(Utils.LOG_TAG, "StockList: loaded data from database: " + dataSet.size());
					}
				} catch (Exception e) {
					String message = "Failed to get stock day data. ";
					if (e.getMessage() != null)
						message += e.getMessage();
					Log.e(Utils.LOG_TAG, message, e);
				}
			} catch (InterruptedException e) {
				Log.e(Utils.LOG_TAG, "semaphore was interrupted", e);
			} finally {
				dataManager.releaseDb(true, this.getClass().getName());
				semaphore.release();
			}
			return items;
		}

		/* 
		 * notify list and hide progress bar
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Map<String, StockItem> result) {
			super.onPostExecute(result);
			if (result == null || result.size() == 0)
				Toast.makeText(getContext(), R.string.update_fail, Toast.LENGTH_LONG).show();
			else {
				clear();
				
				stockListItems = new StockItem[result.size()];
				int index = 0;
				for (Entry<String, StockItem> entry : result.entrySet()) {
					add(entry.getValue());
					stockListItems[index] = entry.getValue();
					index++;
				}
			}
	    	notifyDataSetChanged();
			for (IListAdapterListener<Object> listener : listeners) {
				listener.onListLoaded(null);
			}
		}

		/* 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		 
	}
}
