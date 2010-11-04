/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockCompareTypes;
import cz.tomas.StockAnalyze.StockList.StockComparator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
	
	Boolean showIcons = true;
	
	public StockListAdapter(final Context context, int textViewResourceId, final DataManager dataManager, final String filter) {
		super(context, textViewResourceId);
		this.dataManager = dataManager;

        this.vi = (LayoutInflater)	this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final List<StockItem> tempItems = new ArrayList<StockItem>();
		//tempItems.add(new StockItem("-", "-", "-", "-"));
		// firstly, "getStockList" is run to get data, then in ui thread "updateUi" is invoked
		
		final Runnable updateUi = new Runnable() {
			
			@Override
			public void run() {
				if (tempItems.size() == 0)
					Toast.makeText(context, "Failed!", Toast.LENGTH_LONG).show();
				else
		        	for (int i = 0; i < tempItems.size(); i++) {
		        		add(tempItems.get(i));
					}
            	
		    	notifyDataSetChanged();
		    	try {
					((Activity) getContext()).findViewById(R.id.progressStockList).setVisibility(View.GONE);
				} catch (Exception e) {
					Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "failed to dissmis progess bar! " + e.getMessage());
				}
		    	//progressDialog.dismiss();
			}
		};
		
		Runnable getStockList = new Runnable() {
            @Override
            public void run() {
            	List<StockItem> items = null;
            	try {
					items = StockListAdapter.this.dataManager.search(filter);

	            	Collections.sort(items, new StockComparator(StockCompareTypes.Volume, dataManager));
	            	
	            	for (int i = 0; i < items.size(); i++) {
	            		tempItems.add(items.get(i));
					}
				} catch (Exception e) {
					Log.d("StockListAdapter", e.getMessage());
				}
				finally {
	            	((Activity) context).runOnUiThread(updateUi);	
				}
            	
            }
        };
    	
		Thread thread =  new Thread(null, getStockList, "StockListBackground");
        thread.start();
//        progressDialog = ProgressDialog.show(getContext(),    
//                "Please wait...", "Retrieving data ...", true);
	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
     // Handle dividers
        if(this.isDivider(position)){
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
        
        if (txtName != null) 
        	txtName.setText(stock.getName());
        if(txtTicker != null)
        	txtTicker.setText(stock.getTicker());
        if(txtPrice != null && txtChange != null) {
        	DayData data = null;
			try {
				data = this.dataManager.getLastValue(stock.getTicker());
			} catch (Exception e) {
				txtPrice.setText("Fail");
				Log.d("StockListAdapter", e.getMessage());
			}
        	if (data != null) {
				txtPrice.setText(String.valueOf(data.getPrice()));
				txtChange.setText(data.getChange() + "%");
				if (data.getChange() > 0) {
					txtPrice.setTextColor(Color.GREEN);
					txtChange.setTextColor(Color.GREEN);
				}
				else if (data.getChange() < 0) {
					txtPrice.setTextColor(Color.RED);
					txtChange.setTextColor(Color.RED);
				}
				else {
					txtPrice.setTextColor(Color.BLACK);
					txtChange.setTextColor(Color.BLACK);
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
}