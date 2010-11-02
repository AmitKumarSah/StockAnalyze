/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.ArrayList;
import java.util.List;

import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.Feed;
import cz.tomas.StockAnalyze.News.NewsListAdapter;
import cz.tomas.StockAnalyze.News.NewsSqlHelper;
import cz.tomas.StockAnalyze.News.RSSHandler;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * @author tomas
 *
 */
public class NewsActivity extends ListActivity {

	private NewsSqlHelper news;
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
		fill();
		this.getListView().setTextFilterEnabled(true);
		this.setContentView(R.layout.news_layout);
		//fill();
	}

	private void fill() {
		ArrayAdapter<Article> adapter = new NewsListAdapter(this, R.layout.news_layout);
		this.setListAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_stock_list_refresh:
	    	this.setListAdapter(null);
	    	this.findViewById(R.id.progressNews).setVisibility(View.VISIBLE);
	    	this.fill();
	        return true;
	    case R.id.menu_stock_list_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
