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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.News.NewsListAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseListActivity;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * Activity with list of titles of all articles together with description.
 * @author tomas
 *
 */
public class NewsActivity extends BaseListActivity implements ITaskListener {
	
	public static final String EXTRA_NEWS_ARTICLE = "news-article";
	public static final String EXTRA_NEWS_POSITION = "news-position";
	private static final long UPDATE_INTERVAL = 60 * 1000;
	
	private NewsListAdapter adapter;
	private PullToRefreshListView listView;
	private static long lastUpdateTime = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.news_layout);
		this.listView = (PullToRefreshListView) this.findViewById(R.id.listView);
		//this.listView = (PullToRefreshListView) this.getListView();
		listView.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				if (adapter != null) {
					adapter.refresh();
				}
			}
		});
		listView.getAdapterView().setTextFilterEnabled(true);
		listView.getAdapterView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				Intent intent = new Intent(NewsActivity.this, NewsDetailActivity.class);
				intent.putExtra(EXTRA_NEWS_POSITION, position - listView.getAdapterView().getHeaderViewsCount());
				startActivity(intent);
			}
		});
		
//		final ActionBar bar = (ActionBar) this.findViewById(R.id.newsActionBar);
//		bar.setActionBarListener(new IActionBarListener() {
//			
//			@Override
//			public void onAction(int viewId) {
//				if (viewId == R.id.actionRefreshButton) {
//					refresh();
//				}
//			}
//		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (adapter == null) {
			adapter = new NewsListAdapter(this, this);
		}
		
		this.setListAdapter(adapter);
		final long current = SystemClock.elapsedRealtime();
		if (current - lastUpdateTime > UPDATE_INTERVAL) {
			this.refresh();
			lastUpdateTime = current;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    final MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.news_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_refresh:	    	
	    	refresh();
	        return true;
	    case R.id.menu_settings:
	    	NavUtils.goToSettings(this);
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
		this.getActionBarHelper().setRefreshActionItemState(false);

		this.listView.onRefreshComplete();
	}

	@Override
	public void onUpdateStart() {
		this.getActionBarHelper().setRefreshActionItemState(true);
	}
	
	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}
}
