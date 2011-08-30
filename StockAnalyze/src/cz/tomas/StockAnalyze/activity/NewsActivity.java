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
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.NewsListAdapter;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.activity.base.BaseListActivity;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar.IActionBarListener;

/**
 * @author tomas
 *
 */
public class NewsActivity extends BaseListActivity implements ITaskListener {
	
	public static final String EXTRA_NEWS_ARTICLE = "news-article";
	public static final String EXTRA_NEWS_POSITION = "news-position";
	private static final long UPDATE_INTERVAL = 60 * 1000;
	
	private NewsListAdapter adapter;
	private View refreshButoon;
	private Animation refreshAnimation;
	private static long lastUpdateTime = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.refreshAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_rotate);
		
		this.setContentView(R.layout.news_layout);
		this.getListView().setTextFilterEnabled(true);
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				Article article = (Article) getListView().getItemAtPosition(position);
				
				if (article != null) {
					Intent intent = new Intent(NewsActivity.this, NewsDetailActivity.class);
					intent.putExtra(EXTRA_NEWS_POSITION, position);
					startActivity(intent);
				}
			}
		});
		
		ActionBar bar = (ActionBar) this.findViewById(R.id.newsActionBar);
		this.refreshButoon = bar.findViewById(R.id.actionRefreshButton);
		bar.setActionBarListener(new IActionBarListener() {
			
			@Override
			public void onAction(int viewId) {
				if (viewId == R.id.actionRefreshButton) {
					refresh();
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (adapter == null) {
			adapter = new NewsListAdapter(this, R.layout.news_layout, this);
		}
		
		this.setListAdapter(adapter);
		long current = SystemClock.elapsedRealtime();
		if (current - lastUpdateTime > UPDATE_INTERVAL) {
			this.refresh();
			lastUpdateTime = current;
		}
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
	    	refresh();
	        return true;
	    case R.id.menu_stock_list_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	protected void refresh() {
		adapter.refresh();
	}
	
	@Override
	public void onUpdateFinished() {
		//this.refreshAnimation.setDuration(0);
		this.refreshButoon.setAnimation(null);
	}

	@Override
	public void onUpdateStart() {
		this.refreshAnimation.reset();
		this.refreshButoon.startAnimation(this.refreshAnimation);
	}
}
