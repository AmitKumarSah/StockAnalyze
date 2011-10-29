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
package cz.tomas.StockAnalyze.Portfolio;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
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
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * adapter for portfolio items list in PortfolioActivity
 * 
 * @author tomas
 * 
 */
public class PortfolioListAdapter extends ArrayAdapter<PortfolioItem> {
	
	private DataManager dataManager;
	private LayoutInflater vi; 
	
	private Map<PortfolioItem, DayData> datas;
	/**
	 * array backed by the datas map
	 */
	private PortfolioItem[] portfolioItems;
	
	/**
	 * portfolio item id is a key for stock items
	 */
	private Map<String, StockItem> stockItems;
	
	private Portfolio portfolio = null;
	private List<IListAdapterListener<PortfolioSum>> portfolioListeners;
	
	private PortfolioSum portfolioSummary;
	boolean includeFee = true;
	private SharedPreferences prefs;
	private Market market;
	
	public PortfolioListAdapter(Context context, int textViewResourceId, final DataManager dataManager, Portfolio portfolio, Market market) {
		super(context, textViewResourceId);
		
		this.market = market;
		this.portfolio = portfolio;
		this.dataManager = dataManager;

		this.setNotifyOnChange(false);
		this.portfolioListeners = new ArrayList<IListAdapterListener<PortfolioSum>>();
		
        this.vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.prefs = getContext().getSharedPreferences(Utils.PREF_NAME, 0);
	
		datas = new LinkedHashMap<PortfolioItem, DayData>();
		stockItems = new LinkedHashMap<String, StockItem>();

        this.refresh();
	}
	
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public PortfolioItem getItem(int position) {
		if (this.portfolioItems == null || position >= this.portfolioItems.length) {
			return null;
		}
		return this.portfolioItems[position];
		//return super.getItem(position);
	}
	



	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		if (this.portfolioItems == null) {
			return 0;
		}
		return this.portfolioItems.length;
	}

	/**
	 * get portfolio summary calculated while last portfolio creation/refresh
	 * @return the portfolioSummary
	 */
	public PortfolioSum getPortfolioSummary() {
		return portfolioSummary;
	}
	
	/**
	 * get DayData associated woth portfolio item.
	 * DayData are available after the adapter is initialized with data.
	 * @param item
	 * @return
	 */
	public DayData getData(PortfolioItem item) {
		if (datas == null)
			return null;
		
		return datas.get(item);
	}
	
	/**
	 * get Stock item associated with portfolio item.
	 * Stock items are available after the adapter is initialized with data.
	 * @param item
	 * @return StockItem if available, otherwise null
	 */
	public StockItem getStockItem(PortfolioItem item) {
		if (stockItems == null)
			return null;
		return stockItems.get(item.getStockId());
	}
	
	/**
	 * reload all data asynchronously
	 */
	public void refresh() {
		this.includeFee = prefs.getBoolean(Utils.PREF_PORTFOLIO_INCLUDE_FEE, true);
		PortfolioListTask task = new PortfolioListTask();
        task.execute();
	}
	
	public void addPortfolioListener(IListAdapterListener<PortfolioSum> listener) {
		this.portfolioListeners.add(listener);
	}

	/** 
	 * get view for portfolio item
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PortfolioItemViewHolder holder;
		if (convertView == null) {
			convertView = vi.inflate(R.layout.portfolio_list_item, null);
			holder = new PortfolioItemViewHolder();
			holder.txtTicker = (TextView) convertView.findViewById(R.id.portfolioStockTicker);
			holder.txtName = (TextView) convertView.findViewById(R.id.portfolioStockName);
			holder.txtPrice = (TextView) convertView.findViewById(R.id.portfolioCurrentStockPrice);
			holder.txtChange = (TextView) convertView.findViewById(R.id.portfolioCurrentStockChange);
			holder.priceGroupView = convertView.findViewById(R.id.portfolioPricelayout);
			holder.portfolioGroupView = convertView.findViewById(R.id.portfolioValueLayout);
			holder.txtPortfolioValue = (TextView) convertView.findViewById(R.id.portfolioCurrentValue);
			holder.txtPortfolioValueChange = (TextView) convertView.findViewById(R.id.portfolioValueChange);
			convertView.setTag(holder);
		} else {
			holder = (PortfolioItemViewHolder) convertView.getTag();
		}
			
		PortfolioItem item = this.getItem(position);
		fillView(convertView, item, holder);
		
        return convertView;
	}

	private void fillView(View v, PortfolioItem portfolioItem, PortfolioItemViewHolder holder) {
		if (portfolioItem == null) {
			Log.d(Utils.LOG_TAG, "portfolio item is null in fillView");
			return;
		}
        
		StockItem stock = stockItems.get(portfolioItem.getStockId());
    	DayData data = null;
    	try {
			data = datas.get(portfolioItem);
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.d("StockListAdapter", e.getMessage());
			}
		}
    	
        if (stock == null)
        	return;
        if (holder.txtName != null) 
        	holder.txtName.setText(stock.getName());
        if(holder.txtTicker != null)
        	holder.txtTicker.setText(stock.getTicker());
        if(holder.txtPrice != null && holder.txtChange != null) {
        	if (data != null) {
        		holder.txtPrice.setText(String.valueOf(data.getPrice()));
				NumberFormat percentFormat = FormattingUtils.getPercentFormat();
				String strChange = percentFormat.format(data.getChange());
				String strAbsChange = percentFormat.format(data.getAbsChange());
				holder.txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
				
				// set background drawable according to positive/negative price change
				if (data.getChange() > 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_green_shape));
				}
				else if (data.getChange() < 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_red_shape));
				}
				else if (holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_dark_shape));
				}
			} else {
				holder.txtPrice.setText("Fail");
			}
        }
        if (holder.txtPortfolioValue != null && holder.txtPortfolioValueChange != null && data != null) {        	
        	final float[] changes = new float[2]; 
        	portfolioItem.calculateChanges(data.getPrice(), includeFee, changes);
        	final float currentMarketValue = portfolioItem.getCurrentStockCount() * data.getPrice();
        	
        	NumberFormat percentFormat = FormattingUtils.getPercentFormat();
        	String strAbsChange = percentFormat.format(changes[1]);
        	String strChange = percentFormat.format(changes[0]);
        	String strCurrentValue = percentFormat.format(currentMarketValue);
        	
        	holder.txtPortfolioValueChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
        	holder.txtPortfolioValue.setText(strCurrentValue);
        	// set background drawable according to positive/negative portfolio value change
			if (changes[0] > 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_green_shape));
			}
			else if (changes[0] < 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_red_shape));
			}
			else if (holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_dark_shape));
			}
        }
	}
	
	
	/**
	 * task that loads portfolio items from db 
	 * and add the to the collection of PortfolioListAdapter
	 */
	private class PortfolioListTask extends AsyncTask<String, Integer, List<PortfolioItem>> {
		
		private Exception ex;
		private float totalValueSum;
		private float totalAbsChangeSum;
		private float totalInvestedSum;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			for (IListAdapterListener<?> listener : portfolioListeners) {
				listener.onListLoading();
			}
		}

		@Override
		protected List<PortfolioItem> doInBackground(String... arg0) {

			List<PortfolioItem> items = null;
			try {
				dataManager.acquireDb(this.getClass().getName());
				try {
					items = portfolio.getGroupedPortfolioItems(market);
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to get groupped portfolio items", e);
					this.ex = e;
				}
				try {
					if (items != null) {
						// get day data for each stock and save it
						datas.clear();
						stockItems.clear();
						for (PortfolioItem portfolioItem : items) {
							DayData dayData = dataManager.getLastOfflineValue(portfolioItem.getStockId());
							datas.put(portfolioItem, dayData);
							
							// load also needed stock items
							StockItem stockItem = dataManager.getStockItem(portfolioItem.getStockId(), portfolioItem.getMarketId());
							stockItems.put(portfolioItem.getStockId(), stockItem);

							float itemValue = portfolioItem.getCurrentStockCount() * dayData.getPrice();
							itemValue = Math.abs(itemValue);
							if (includeFee) {
								itemValue -= portfolioItem.getBuyFee();
								itemValue -= portfolioItem.getSellFee();
							}
							
							// invested value is here because item value might be null,
							// because user bought something and also sold it - therefore
							// we need to know invested money so we can calculate percentual change for whole portfolio
							final float investedValue = portfolioItem.getInvestedValue(includeFee);
							final float[] changes = new float[2];
							portfolioItem.calculateChanges(dayData.getPrice(), includeFee, changes);
							totalValueSum += itemValue;	// count short positions as positive so we have their value
							totalAbsChangeSum += changes[1];
							totalInvestedSum += investedValue;
						}
					}
					portfolioItems = new PortfolioItem[datas.size()];
					portfolioItems = datas.keySet().toArray(portfolioItems);
				} catch (Exception e) {
					String message = "Failed to get stock day data. ";
					if (e.getMessage() != null)
						message += e.getMessage();
					Log.e(Utils.LOG_TAG, message, e);
				}
			} finally {
				dataManager.releaseDb(true, this.getClass().getName());
			}
			
			return items;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<PortfolioItem> result) {
			super.onPostExecute(result);
			
			if (result == null) {
				String message = "Failed to get portfolio!\n";
				if (this.ex != null)
					message += ex.toString();
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
				return;
			}
			float totalPercChange = 0f;
			if (totalInvestedSum > 0)
				totalPercChange = (totalAbsChangeSum / totalInvestedSum)*100;
			portfolioSummary = new PortfolioSum(this.totalValueSum, this.totalAbsChangeSum, totalPercChange);
//			clear();
			
//			// add portfolio items to the adapter list
//			for (PortfolioItem item : result) {
//				add(item);
//			}
			
			// populate the portfolio summary
			for (IListAdapterListener<PortfolioSum> listener : portfolioListeners) {
				listener.onListLoaded(portfolioSummary);
			}
			notifyDataSetChanged();
		}
	}

	private static class PortfolioItemViewHolder {
		TextView txtTicker;
        TextView txtName;
        TextView txtPrice;
        TextView txtChange;
        View priceGroupView;
        View portfolioGroupView;
        TextView txtPortfolioValue;
        TextView txtPortfolioValueChange;
        
	}
}
