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
package cz.tomas.StockAnalyze.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public class StockSearchActivity extends BaseActivity {

	private static final String SELECTED_STOCK = "stock_id";
	//private static final String MARKET = "market_id";
	DataManager dataManger;
	static final int DIALOG_ADD = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.dataManger = DataManager.getInstance(this);
		
		this.setContentView(R.layout.stock_search);
		final ListView list = (ListView) this.findViewById(R.id.listFoundItems);
		final TextView txtSearch = (TextView) this.findViewById(R.id.SearchEditText);
		txtSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.length() > 1) {
					List<StockItem> stocks = StockSearchActivity.this.dataManger.search(s.toString(), Markets.CZ);
					String[] displayResults = new String[stocks.size()];
					
					for (int i = 0; i < displayResults.length; i++) {
						String line = String.format("%s\t\t%s", stocks.get(i).getTicker(), stocks.get(i).getName());
						displayResults[i] = line;
					}
					if (stocks != null) {
						try {
							//list.setAdapter(new ArrayAdapter<String>(StockSearchActivity.this, R.layout.stock_list, displayResults));
							StockListAdapter adapter = new StockListAdapter(StockSearchActivity.this, R.id.name, dataManger, null, true);
							adapter.showIcons(false);
							list.setAdapter(adapter);
							list.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0,
										View view, int position, long id) {
									StockSearchActivity.this.setIntent(StockSearchActivity.this.getIntent().putExtra(SELECTED_STOCK, position));
									StockSearchActivity.this.showDialog(StockSearchActivity.DIALOG_ADD);
								}
							});
						} catch (Exception e) {
							Log.d(Utils.LOG_TAG, "search list error", e);
						}
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	protected void onPrepareDialog(int id, Dialog dlg)
	{
		ListView list = (ListView) StockSearchActivity.this.findViewById(R.id.listFoundItems);
		String item = "";
		if (this.getIntent().getExtras().containsKey(SELECTED_STOCK)) {
			int position = this.getIntent().getExtras().getInt(SELECTED_STOCK);
			item = list.getItemAtPosition(position).toString();
		}
		
		dlg.setTitle("Add Stock " + item);
	}
	
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_ADD:
	        dialog = this.buildAddStockDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	private Dialog buildAddStockDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("Do you want to add this stock?")
				.setCancelable(false)
				.setTitle("Add Stock")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									final ListView list = (ListView) findViewById(R.id.listFoundItems);
									int position = StockSearchActivity.this.getIntent().getIntExtra(SELECTED_STOCK, -1);
									StockItem stock = (StockItem) list.getItemAtPosition(position);
									
									Activity activity = StockSearchActivity.this.getParent();
									if (activity != null && activity instanceof TabActivity) {
										TabActivity act = (TabActivity) activity;
										act.getIntent().putExtra("stock_id", stock.getId());
										act.getIntent().putExtra("market_id", stock.getMarket());
										act.getTabHost().setCurrentTabByTag("StockDetail");
									}
									else {
										Intent intent = new Intent();
										intent.putExtra("stock_id", stock.getId());
										intent.putExtra("market_id", stock.getMarket());
										intent.setClass(StockSearchActivity.this, StockDetailActivity.class);
										startActivity(intent);
									}
								} catch (Exception e) {
									Toast.makeText(StockSearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
									e.printStackTrace();
								}
								dialog.dismiss();
							}
						})
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}
}
