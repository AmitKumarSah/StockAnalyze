/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	public StockListAdapter(final Context context, int textViewResourceId, final DataManager dataManager, final String filter) {
		super(context, textViewResourceId);
		this.dataManager = dataManager;
		
		final List<StockItem> tempItems = new ArrayList<StockItem>();
		
		// firstly, "getStockList" is runned to get data, then in ui thread "updateUi" is invoked
		
		final Runnable updateUi = new Runnable() {
			
			@Override
			public void run() {
            	for (int i = 0; i < tempItems.size(); i++) {
            		add(tempItems.get(i));
				}
            	
		    	notifyDataSetChanged();
		    	progressDialog.dismiss();
			}
		};
		
		Runnable getStockList = new Runnable(){
            @Override
            public void run() {
            	List<StockItem> items = null;
            	try {
					items = StockListAdapter.this.dataManager.search(filter);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("StockListAdapter", e.getMessage());
			    	progressDialog.dismiss();
			    	Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
					return;
				}
            	Collections.sort(items, new StockComparator(StockCompareTypes.Volume, dataManager));
            	
            	for (int i = 0; i < items.size(); i++) {
            		tempItems.add(items.get(i));
				}
            	
            	((Activity) context).runOnUiThread(updateUi);
            }
        };
    	
		Thread thread =  new Thread(null, getStockList, "StockListBackground");
        thread.start();
        progressDialog = ProgressDialog.show(getContext(),    
                "Please wait...", "Retrieving data ...", true);
	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.stock_list_item, null);
        }
        StockItem stock = this.getItem(position);
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
		
        return v;
	}
}