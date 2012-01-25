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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.News.NewsContentProvider;
import cz.tomas.StockAnalyze.News.NewsListAdapter;
import cz.tomas.StockAnalyze.News.NewsSqlHelper;
import cz.tomas.StockAnalyze.News.Rss;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Activity with list of titles of all articles together with description.
 * @author tomas
 *
 */
public class NewsActivity extends BaseFragmentActivity implements LoaderCallbacks<Cursor> {
	
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
		listView.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				refresh();
			}
		});
		final ListView refreshableView = listView.getRefreshableView();
		refreshableView.setTextFilterEnabled(true);
		refreshableView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				Intent intent = new Intent(NewsActivity.this, NewsDetailActivity.class);
				intent.putExtra(EXTRA_NEWS_POSITION, position - refreshableView.getHeaderViewsCount());
				startActivity(intent);
			}
		});
		
		this.adapter = new NewsListAdapter(this, null);
		refreshableView.setAdapter(this.adapter);
		
		this.getSupportLoaderManager().initLoader(0, null, this);
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
		NewsRefreshTask task = new NewsRefreshTask();
		task.execute();
	}
	
	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, NewsContentProvider.ARTICLES_CONTENT_URI, NewsSqlHelper.ArticleColumns.BASE_PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		this.adapter.swapCursor(data);
		
		this.getActionBarHelper().setRefreshActionItemState(false);
		this.listView.onRefreshComplete();
		final long current = SystemClock.elapsedRealtime();
		if (current - lastUpdateTime > UPDATE_INTERVAL) {
			this.refresh();
			lastUpdateTime = current;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		this.adapter.swapCursor(null);
	}
	
	private class NewsRefreshTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			getActionBarHelper().setRefreshActionItemState(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Rss rss = (Rss) getApplicationContext().getSystemService(Application.RSS_SERVICE);
			try {
				rss.fetchArticles();
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to refresh news", e);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == null || result == false) {
				// error
				getActionBarHelper().setRefreshActionItemState(false);
				listView.onRefreshComplete();
			}
		}
	}
}
