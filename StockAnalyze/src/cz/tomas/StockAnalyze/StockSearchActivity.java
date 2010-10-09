/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.List;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author tomas
 *
 */
public class StockSearchActivity extends Activity {

	DataManager dataManger;
	
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
				if (s.length() > 2) {
					List<StockItem> stocks = StockSearchActivity.this.dataManger.search(s.toString());
					String[] displayResults = new String[stocks.size()];
					
					for (int i = 0; i < displayResults.length; i++) {
						displayResults[i] = stocks.get(i).toString();
					}
					if (stocks != null) {
						try {
							list.setAdapter(new ArrayAdapter<String>(StockSearchActivity.this, R.layout.stock_list, displayResults));
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
}
