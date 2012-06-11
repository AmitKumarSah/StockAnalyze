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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.fragments.PortfolioListFragment;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.text.NumberFormat;

/**
 * adapter for portfolio items list in ({@link PortfolioListFragment})
 * 
 * @author tomas
 * 
 */
public class PortfolioListAdapter extends BaseAdapter {
	
	private final LayoutInflater inflater;
	private final SharedPreferences pref;
	
	private PortfolioListData listData;
	
	private final Drawable drawableGreenPortfolio;
	private final Drawable drawableRedPortfolio;
	private final Drawable drawableBlackPortfolio;
	
	private final Drawable drawableGreenPrice;
	private final Drawable drawableRedPrice;
	private final Drawable drawableBlackPrice;

	private boolean includeFee;
	
	public PortfolioListAdapter(Context context) {
		
		this.drawableGreenPrice = context.getResources().getDrawable(R.drawable.bg_simple_green_shape);
		this.drawableRedPrice = context.getResources().getDrawable(R.drawable.bg_simple_red_shape);
		this.drawableBlackPrice = context.getResources().getDrawable(R.drawable.bg_simple_dark_shape);

		this.drawableGreenPortfolio = context.getResources().getDrawable(R.drawable.bg_simple_green_shape);
		this.drawableRedPortfolio = context.getResources().getDrawable(R.drawable.bg_simple_red_shape);
		this.drawableBlackPortfolio = context.getResources().getDrawable(R.drawable.bg_simple_dark_shape);
		
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pref = context.getSharedPreferences(Utils.PREF_NAME, 0);
	}

	@Override
	public PortfolioItem getItem(int position) {
		if (this.listData.portfolioItems == null || position >= this.listData.portfolioItems.length) {
			return null;
		}
		return this.listData.portfolioItems[position];
	}

	@Override
	public int getCount() {
		if (this.listData == null || this.listData.portfolioItems == null) {
			return 0;
		}
		return this.listData.portfolioItems.length;
	}
	
	/**
	 * get DayData associated woth portfolio item.
	 * DayData are available after the adapter is initialized with data.
	 * @param item
	 * @return
	 */
	public DayData getData(PortfolioItem item) {
		if (listData.datas == null)
			return null;
		
		return listData.datas.get(item);
	}
	
	/**
	 * get Stock item associated with portfolio item.
	 * Stock items are available after the adapter is initialized with data.
	 * @param item
	 * @return StockItem if available, otherwise null
	 */
	public StockItem getStockItem(PortfolioItem item) {
		if (listData.stockItems == null)
			return null;
		return listData.stockItems.get(item.getStockId());
	}
	
	/**
	 * reload all data asynchronously
	 */
	public void setData(PortfolioListData data) {
		if (data != null) {
			this.includeFee = this.pref.getBoolean(Utils.PREF_PORTFOLIO_INCLUDE_FEE, true);
			this.listData = data;
			this.notifyDataSetChanged();
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/** 
	 * get view for portfolio item
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PortfolioItemViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_portfolio_list, null);
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
        
		StockItem stock = listData.stockItems.get(portfolioItem.getStockId());
    	DayData dayData = null;
    	try {
    		dayData = listData.datas.get(portfolioItem);
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.d(Utils.LOG_TAG, e.getMessage());
			}
		}
    	
        if (stock == null) {
        	return;
        }
        if (holder.txtName != null)  {
        	holder.txtName.setText(stock.getName());
        }
        if (holder.txtTicker != null) {
        	holder.txtTicker.setText(stock.getTicker());
        }
        if (holder.txtPrice != null && holder.txtChange != null) {
        	if (dayData != null) {
        		holder.txtPrice.setText(FormattingUtils.getValueFormat().format(dayData.getPrice()));
				NumberFormat percentFormat = FormattingUtils.getPercentFormat();
				String strChange = percentFormat.format(dayData.getChange());
				String strAbsChange = percentFormat.format(dayData.getAbsChange());
				holder.txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
				
				// set background drawable according to positive/negative price change
				if (dayData.getChange() > 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableGreenPrice);
				} else if (dayData.getChange() < 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableRedPrice);
				} else if (holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableBlackPrice);
				}
			} else {
				holder.txtPrice.setText("Fail");
			}
        }
        if (holder.txtPortfolioValue != null && holder.txtPortfolioValueChange != null && dayData != null) {        	
        	final double[] changes = new double[2]; 
        	portfolioItem.calculateChanges(dayData.getPrice(), includeFee, changes);
        	final float currentMarketValue = portfolioItem.getCurrentStockCount() * dayData.getPrice();
        	
        	NumberFormat percentFormat = FormattingUtils.getPercentFormat();
        	String strAbsChange = percentFormat.format(changes[1]);
        	String strChange = percentFormat.format(changes[0]);
        	String strCurrentValue = percentFormat.format(currentMarketValue);
        	
        	holder.txtPortfolioValueChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
        	holder.txtPortfolioValue.setText(strCurrentValue);
        	
        	// set background drawable according to positive/negative portfolio value change
			if (changes[0] > 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(this.drawableGreenPortfolio);
			} else if (changes[0] < 0 && holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(this.drawableRedPortfolio);
			} else if (holder.portfolioGroupView != null) {
				holder.portfolioGroupView.setBackgroundDrawable(this.drawableBlackPortfolio);
			}
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
