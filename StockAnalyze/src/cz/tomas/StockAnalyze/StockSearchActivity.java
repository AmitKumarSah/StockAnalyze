/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.List;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * @author tomas
 *
 */
public class StockSearchActivity extends Activity {

	private static final String SELECTED_STOCK = "selected_stock";
	DataManager dataManger;
	static final int DIALOG_ADD = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.dataManger = new DataManager(this);
		
		this.setContentView(R.layout.stock_search);
		final ListView list = (ListView) this.findViewById(R.id.listFoundItems);
		final TextView txtSearch = (TextView) this.findViewById(R.id.SearchEditText);
		txtSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.length() > 1) {
					List<StockItem> stocks = StockSearchActivity.this.dataManger.search(s.toString());
					String[] displayResults = new String[stocks.size()];
					
					for (int i = 0; i < displayResults.length; i++) {
						String line = String.format("%s\t\t%s", stocks.get(i).getTicker(), stocks.get(i).getName());
						displayResults[i] = line;
					}
					if (stocks != null) {
						try {
							//list.setAdapter(new ArrayAdapter<String>(StockSearchActivity.this, R.layout.stock_list, displayResults));
							StockListAdapter adapter = new StockListAdapter(StockSearchActivity.this, R.id.toptext, dataManger, txtSearch.getText().toString());
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
							e.printStackTrace();
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
									
									TabActivity act = (TabActivity) StockSearchActivity.this.getParent();
									act.getIntent().putExtra("stock_id", stock.getId());
									act.getTabHost().setCurrentTabByTag("StockDetail");
								} catch (Exception e) {
									Toast.makeText(StockSearchActivity.this, e.getMessage(), Toast.LENGTH_LONG);
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
