/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 * adapter for portfolio items list in PortfolioActivity
 */
public class PortfolioListAdapter extends ArrayAdapter<PortfolioItem> {
	
	private DataManager dataManager;
	private LayoutInflater vi; 
	
	private Map<PortfolioItem, DayData> datas;
	private List<PortfolioItem> portfolioItems;
	private Portfolio portfolio = null;
	//private StockComparator comparator;
	
	public PortfolioListAdapter(Context context, int textViewResourceId, final DataManager dataManager, Portfolio portfolio) {
		super(context, textViewResourceId);
		
		this.portfolio = portfolio;
		this.dataManager = dataManager;
		//this.comparator = new StockComparator(StockCompareTypes.Name, dataManager);

		this.datas = new HashMap<PortfolioItem, DayData>();
		this.portfolioItems = new ArrayList<PortfolioItem>();
		
        this.vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.refresh();
	}
	
	public void refresh() {
		PortfolioListTask task = new PortfolioListTask();
        task.execute();
	}

	/* 
	 * PortfolioItems + header + footer
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return this.portfolioItems.size();
	}

	/* 
	 * internal list with portfolio items,
	 * it is filled from PortfolioTask
	 * @see android.widget.ArrayAdapter#add(java.lang.Object)
	 */
	@Override
	public void add(PortfolioItem portfolioItem) {
		this.portfolioItems.add(portfolioItem);
	}

	/* 
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public PortfolioItem getItem(int position) {
		return this.portfolioItems.get(position);
	}
	
	

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#clear()
	 */
	@Override
	public void clear() {
		this.portfolioItems.clear();
	}

	/* 
	 * get view: header for first position
	 * footer for last position
	 * portfolio items for rest
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
        
        StockItem stock = this.dataManager.getStockItem(portfolioItem.getStockId());
    	DayData data = null;
    	try {
			data = this.datas.get(portfolioItem);
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
        	float currentValue = portfolioItem.getStockCount()  * data.getPrice();
        	float startValue = portfolioItem.getStartValue();
        	float change = ((currentValue / startValue) * 100) - 100;
        	NumberFormat percentFormat = FormattingUtils.getPercentFormat();
        	String strAbsChange = percentFormat.format(currentValue - startValue);
        	String strChange = percentFormat.format(change);
        	
        	holder.txtPortfolioValueChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
        	holder.txtPortfolioValue.setText(String.valueOf(currentValue));
        	// set background drawable according to positive/negative portfolio value change
			if (change > 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_green_shape));
			}
			else if (change < 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_red_shape));
			}
			else if (holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.groupbox_dark_shape));
			}
        }
	}
	
	/*
	 * task that loads portfolio items from db 
	 * and add the to the collection of PortfolioListAdapter
	 */
	private class PortfolioListTask extends AsyncTask<String, Integer, List<PortfolioItem>> {
		private Exception ex;
		
		@Override
		protected List<PortfolioItem> doInBackground(String... arg0) {

			List<PortfolioItem> items = null;
			try {
				items = portfolio.getGroupedPortfolioItems();
			} catch (Exception e) {
				e.printStackTrace();
				this.ex = e;
			}
			try {
				if (items != null) {
					// get day data for each stock and save it
					datas.clear();
					for (PortfolioItem portfolioItem : items) {
						DayData dayData = dataManager.getLastOfflineValue(portfolioItem.getStockId());
						datas.put(portfolioItem, dayData);
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
			clear();
			
			for (PortfolioItem item : result) {
				add(item);
			}
			
			notifyDataSetChanged();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
