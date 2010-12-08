/**
 * 
 */
package cz.tomas.StockAnalyze.StockList;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author tomas
 *
 */

public class StockListAdapter extends ArrayAdapter<StockItem> {
	 
	ProgressDialog progressDialog = null;
	protected DataManager dataManager;
	LayoutInflater vi; 
	
	Map<StockItem, DayData> datas;
	
	Boolean showIcons = true;
	
	public StockListAdapter(final Context context, int textViewResourceId, final DataManager dataManager, final String filter) {
		super(context, textViewResourceId);
		this.dataManager = dataManager;

		this.datas = new HashMap<StockItem, DayData>();
        this.vi = (LayoutInflater)	this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        StockListTask task = new StockListTask();
        task.execute(filter);
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
        View iconView = v.findViewById(R.id.iconStockItem);
        if (! this.showIcons) {
        	 if (iconView != null)
        		 iconView.setVisibility(View.GONE);
        }
        
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
		TextView txtTicker = (TextView) v.findViewById(R.id.toptext);
        TextView txtName = (TextView) v.findViewById(R.id.bottomtext);
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
				NumberFormat percentFormat = DecimalFormat.getNumberInstance();
				percentFormat.setMaximumFractionDigits(2);
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
	
	/*
	 * this task load all stocks from datamanger and notify the ListView,
	 * it also takes care about the progress view
	 */
	class StockListTask extends AsyncTask<String, Integer, List<StockItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	    	try {
				((Activity) getContext()).findViewById(R.id.progressStockList).setVisibility(View.VISIBLE);
			} catch (Exception e) {
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "failed to show progess bar! " + e.getMessage());
			}
		}

		@Override
		protected List<StockItem> doInBackground(String... params) {
			List<StockItem> items = null;
        	try {
				items = StockListAdapter.this.dataManager.search(params[0], MarketFactory.getMarket("cz"));

            	Collections.sort(items, new StockComparator(StockCompareTypes.Volume, dataManager));
            	
			} catch (Exception e) {
				String message = "Failed to get stock list. ";
				if (e.getMessage() != null)
					message += e.getMessage();
				Log.d("StockListAdapter", message);
				e.printStackTrace();
			}
			try {
				// get day data for each stock and save it
            	datas.clear();
            	for (StockItem stockItem : items) {
					DayData dayData = dataManager.getLastValue(stockItem);
					datas.put(stockItem, dayData);
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
			else
	        	for (int i = 0; i < result.size(); i++) {
	        		add(result.get(i));
				}
        	
	    	notifyDataSetChanged();
	    	try {
				((Activity) getContext()).findViewById(R.id.progressStockList).setVisibility(View.GONE);
			} catch (Exception e) {
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "failed to dissmis progess bar! " + e.getMessage());
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