/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockComparator;
import cz.tomas.StockAnalyze.StockList.StockCompareTypes;
import android.content.Context;
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
public class PortfolioListAdapter extends ArrayAdapter<PortfolioItem> {
	
	private DataManager dataManager;
	private LayoutInflater vi; 
	
	private Map<PortfolioItem, DayData> datas;
	private StockComparator comparator;
	
	public PortfolioListAdapter(Context context, int textViewResourceId, final DataManager dataManager) {
		super(context, textViewResourceId);
		
		this.dataManager = dataManager;
		this.comparator = new StockComparator(StockCompareTypes.Name, dataManager);

		this.datas = new HashMap<PortfolioItem, DayData>();
        this.vi = (LayoutInflater)	this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        PortfolioListTask task = new PortfolioListTask();
        task.execute();
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		 if (v == null) {
			 v = vi.inflate(R.layout.stock_list_item, null);
	     }
		 View iconView = v.findViewById(R.id.iconStockItem);
        
    	 if (iconView != null)
    		 iconView.setVisibility(View.GONE);
	        
        PortfolioItem item = this.getItem(position);
        fillView(v, item);
        return v;
	}

	private void fillView(View v, PortfolioItem item) {
		TextView txtTicker = (TextView) v.findViewById(R.id.bottomtext);
        TextView txtName = (TextView) v.findViewById(R.id.toptext);
        TextView txtPrice = (TextView) v.findViewById(R.id.righttext);
        TextView txtChange = (TextView) v.findViewById(R.id.righttext2);
        View priceGroupView = v.findViewById(R.id.pricelayout);
        
        StockItem stock = this.dataManager.getStockItem(item.getStockId());
        if (stock == null)
        	return;
        if (txtName != null) 
        	txtName.setText(stock.getName());
        if(txtTicker != null)
        	txtTicker.setText(stock.getTicker());
        if(txtPrice != null && txtChange != null) {
        	DayData data = null;
			try {
				data = this.datas.get(item);
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
	private class PortfolioListTask extends AsyncTask<String, Integer, List<PortfolioItem>> {

		private final Portfolio portfolio = new Portfolio(getContext());
		private Exception ex;
		
		@Override
		protected List<PortfolioItem> doInBackground(String... arg0) {

			List<PortfolioItem> items = null;
			try {
				items = this.portfolio.getPortfolioItems();
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
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		
	}
	
}
