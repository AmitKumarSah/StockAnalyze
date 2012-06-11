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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.fragments.StockGridFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.text.NumberFormat;
import java.util.Map;

/**
 * Adapter for list of stocks - used in {@link StockListFragment} and {@link StockGridFragment}
 * 
 * @author tomas
 *
 */

public class StockListAdapter extends BaseAdapter {
	 
	private LayoutInflater vi;
	
	/**
	 * these are data to be used in views
	 */
	private Map<StockItem, DayData> dataSet;
	private StockItem[] stocks;
	
	private final Drawable drawableGreen;
	private final Drawable drawableRed;
	private final Drawable drawableBlack;
	
	private int viewId;
	
	public StockListAdapter(Context context, int itemViewId) {
		this.viewId = itemViewId;
		this.drawableGreen = context.getResources().getDrawable(R.drawable.bg_simple_green_shape);
		this.drawableRed = context.getResources().getDrawable(R.drawable.bg_simple_red_shape);
		this.drawableBlack = context.getResources().getDrawable(R.drawable.bg_simple_dark_shape);

        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setData(Map<StockItem, DayData> data) {
		this.dataSet = data;
		if (data != null) {
			this.stocks = new StockItem[this.dataSet.size()];
			this.stocks = this.dataSet.keySet().toArray(this.stocks);
		} else {
			this.stocks = null;
		}
		this.notifyDataSetChanged();
	}
	
	public DayData getDayData(StockItem stockItem) {
		if (dataSet != null) {
			return dataSet.get(stockItem);
		}
		return null;
	}
	
	@Override
	public StockItem getItem(int position) {
		if (this.stocks == null || position >= this.stocks.length) {
			return null;
		}
		return this.stocks[position];
	}

	@Override
	public int getCount() {
		if (this.stocks == null) {
			return 0;
		}
		return this.stocks.length;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        StockItemViewHolder holder = null;
        
        if (v == null) {
            v = vi.inflate(this.viewId, null);
            
            holder = new StockItemViewHolder();
            holder.txtTicker = (TextView) v.findViewById(R.id.ticker);
            holder.txtName = (TextView) v.findViewById(R.id.name);
            holder.txtPrice = (TextView) v.findViewById(R.id.price);
            holder.txtChange = (TextView) v.findViewById(R.id.change);
            holder.priceGroupView = v.findViewById(R.id.pricelayout);
            v.setTag(holder);
        } else {
        	holder = (StockItemViewHolder) v.getTag();
        }
        
        final StockItem stock = this.getItem(position);
        if (stock != null)
        	fillView(holder, stock);
        else {
        	Log.d(Utils.LOG_TAG, "stock is null, cannot create list's view at position " + position);
        }
		
        return v;
	}

	/**
	 * Get data for stock item and write them to the view
	 * @param holder View to fill
	 * @param stock stock to access data to write to view
	 */
	private void fillView(StockItemViewHolder holder, StockItem stock) {
        if (holder.txtName != null) {
        	holder.txtName.setText(stock.getName() != null ?
			        stock.getName().toUpperCase() : "");
        }
        if(holder.txtTicker != null) {
        	holder.txtTicker.setText(stock.getTicker() != null ?
			        stock.getTicker() : "");
        }
        if(holder.txtPrice != null && holder.txtChange != null) {
        	DayData data = null;
			try {
				data = dataSet.get(stock);
			} catch (Exception e) {
				holder.txtPrice.setText(" - ");
				Log.e(Utils.LOG_TAG, "failed to get daydata for " + stock, e);
			}
        	if (data != null) {
        		holder.txtPrice.setText(FormattingUtils.getValueFormat().format(data.getPrice()));
				NumberFormat percentFormat = FormattingUtils.getPercentFormat();
				String strChange = percentFormat.format(data.getChange());
				String strAbsChange = percentFormat.format(data.getAbsChange());
				holder.txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
				
				// set background drawable according to positive/negative price change
				if (data.getChange() > 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableGreen);
				} else if (data.getChange() < 0 && holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableRed);
				} else if (holder.priceGroupView != null) {
					holder.priceGroupView.setBackgroundDrawable(this.drawableBlack);
				}
			} else {
				Log.w(Utils.LOG_TAG, "there are no day data for " + stock);
				if (holder.txtPrice != null && holder.priceGroupView != null) {
					holder.txtPrice.setText(R.string.InvalidData);
					holder.priceGroupView.setBackgroundDrawable(this.drawableBlack);
				}
			}
        }
	}
	
    @Override
	public boolean hasStableIds() {
    	return true;
	}

	@Override
    public boolean areAllItemsEnabled() {
        return true;
    }
	
	private static class StockItemViewHolder {
		TextView txtTicker;
        TextView txtName;
        TextView txtPrice;
        TextView txtChange;
        View priceGroupView;
	}
}
