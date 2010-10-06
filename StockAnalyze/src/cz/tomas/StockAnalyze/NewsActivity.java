/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * @author tomas
 *
 */
public class NewsActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] test = new String[] { "Telefonica krachuje!", "Kit Digital ma monopol"};
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.news_layout, test));
		this.getListView().setTextFilterEnabled(true);
	}
}
