/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.ListActivity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * @author tomas
 *
 */
public class StockListActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] test = new String[] { "BAACEZ", "BAATELEC", "BAACETV" };
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.stock_list, test));
		
		this.getListView().setTextFilterEnabled(true);
	}
}
