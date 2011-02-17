/**
 * 
 */
package cz.tomas.StockAnalyze.StockList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;

/**
 * @author tomas
 *
 */

public class StockListAdapter extends ArrayAdapter<StockItem> {
	 
	private DataManager dataManager;
	private LayoutInflater vi; 
	
	private Map<StockItem, DayData> datas;
	private StockComparator comparator;
	private List<IListAdapterListener<Object>> listeners;
	
	private Boolean showIcons = true;
	
	public StockListAdapter(final Context context, int textViewResourceId, final DataManager dataManager, final String filter) {
		super(context, textViewResourceId);
		this.dataManager = dataManager;
		this.comparator = new StockComparator(StockCompareTypes.Name, dataManager);
		this.listeners = new ArrayList<IListAdapterListener<Object>>();

		this.datas = new HashMap<StockItem, DayData>();
        this.vi = (LayoutInflater)	this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.setNotifyOnChange(false);

        OfflineStockListTask offlineTask = new OfflineStockListTask();
        offlineTask.execute((Void[])null);

        // HACK initial update (temporary)
        final StockListTask task = new StockListTask();
        task.execute(filter);
        
        this.dataManager.addStockDataListener(new IStockDataListener() {
			
			@Override
			public void OnStockDataUpdated(IStockDataProvider sender) {
				((Activity) context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						StockListTask task = new StockListTask();
				        task.execute(filter);
					}
				});
			}
			
			@Override
			public void OnStockDataUpdateBegin(IStockDataProvider sender) {
				
			}

			@Override
			public void OnStockDataNoUpdate(IStockDataProvider sender) {

			}
		});
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
        fillView(v, stock);
		
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
				//data = this.dataManager.getLastValue(stock);
				data = this.datas.get(stock);
			} catch (Exception e) {
				txtPrice.setText("Fail");
				if (e.getMessage() != null) {
					Log.d("StockListAdapter", e.getMessage());
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
    
    private Boolean isDivider(int position) {
    	return this.getItem(position).getName().startsWith("-");
    }
	
	public void showIcons(Boolean show) {
		this.showIcons = show;
	}
	
	public void addListAdapterListener(IListAdapterListener<Object> listener) {
		this.listeners.add(listener);
	}
	
	private class OfflineStockListTask extends AsyncTask<Void, Integer, List<StockItem>> {

		@Override
		protected List<StockItem> doInBackground(Void... params) {
			List<StockItem> items = dataManager.getStockItems(null);
			for (StockItem stockItem : items) {
				DayData data = dataManager.getLastOfflineValue(stockItem.getId());
				datas.put(stockItem, data);
			}
			Collections.sort(items, comparator);
			return items;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<StockItem> result) {
			super.onPostExecute(result);
			if (result == null || result.size() == 0)
				Toast.makeText(getContext(), R.string.noOfflineData, Toast.LENGTH_LONG).show();
			else {
				clear();
	        	for (int i = 0; i < result.size(); i++) {
	        		add(result.get(i));
				}
			}
	    	notifyDataSetChanged();
		}
		
		
	}
	
	/*
	 * this task load all stocks from DataManager and notify the ListView,
	 * it also takes care about the progress view
	 */
	private class StockListTask extends AsyncTask<String, Integer, List<StockItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			for (IListAdapterListener<Object> listener : listeners) {
				listener.onListLoading();
			}
		}

		@Override
		protected List<StockItem> doInBackground(String... params) {
			List<StockItem> items = null;
			// first, get all stock items we need, according to the search pattern 
			// that is params[0] in this case
        	try {
				items = StockListAdapter.this.dataManager.search(params[0], MarketFactory.getMarket("cz"));

            	Collections.sort(items, comparator);
            	
			} catch (Exception e) {
				String message = "Failed to get stock list. ";
				if (e.getMessage() != null)
					message += e.getMessage();
				Log.d("StockListAdapter", message);
				e.printStackTrace();
			}
			try {
				if (items != null) {
					// get day data for each stock and save it
					datas.clear();
					for (StockItem stockItem : items) {
						DayData dayData = dataManager.getLastValue(stockItem);
						datas.put(stockItem, dayData);
					}
				}
			} catch (Exception e) {
				String message = "Failed to get stock day data. ";
				if (e.getMessage() != null)
					message += e.getMessage();
				Log.d("StockListAdapter", message);
				e.printStackTrace();
			}
			return items;
		}

		/* 
		 * notify list and hide progress bar
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<StockItem> result) {
			super.onPostExecute(result);
			if (result == null || result.size() == 0)
				Toast.makeText(getContext(), R.string.update_fail, Toast.LENGTH_LONG).show();
			else {
				clear();
	        	for (int i = 0; i < result.size(); i++) {
	        		add(result.get(i));
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