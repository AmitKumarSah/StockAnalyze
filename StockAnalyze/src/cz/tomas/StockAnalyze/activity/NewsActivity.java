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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.NewsListAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseListActivity;

/**
 * @author tomas
 *
 */
public class NewsActivity extends BaseListActivity {
	
	public static final String EXTRA_NEWS_ARTICLE = "news-article";
	public static final String EXTRA_NEWS_POSITION = "news-position";
	private NewsListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.news_layout);
		this.getListView().setTextFilterEnabled(true);
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				Article article = (Article) getListView().getItemAtPosition(position);
				
				if (article != null) {
					Intent intent = new Intent(NewsActivity.this, NewsDetailActivity.class);
					//intent.putExtra(EXTRA_NEWS_ARTICLE, article);
					intent.putExtra(EXTRA_NEWS_POSITION, position);
					startActivity(intent);
				}
				
//				if (article != null && article.getUrl() != null) {
//					Uri uri = Uri.parse(article.getUrl().toString());
//					Intent browserIntent = new Intent("android.intent.action.VIEW", uri);
//					startActivity(browserIntent);
//				}
			}
		});
	}

	private void fill() {
		if (adapter == null)
			adapter = new NewsListAdapter(this, R.layout.news_layout);
		
		this.setListAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fill();
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
	    	this.adapter.refresh();
	        return true;
	    case R.id.menu_stock_list_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
