/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.ArrayList;
import java.util.List;

import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.Feed;
import cz.tomas.StockAnalyze.News.NewsSqlHelper;
import cz.tomas.StockAnalyze.News.RSSHandler;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
		
		
		progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
		
		fill();
	}

	private void fill() {
		final List<String> newsItems = new ArrayList<String>();
		
		Runnable updateNews = new Runnable() {			
			@Override
			public void run() {
				try {
					RSSHandler rss = new RSSHandler();
					NewsActivity.this.news = new NewsSqlHelper(NewsActivity.this);
					
					List<Feed> feeds = NewsActivity.this.news.getFeeds();
					for (Feed feed : feeds) {
						NewsActivity.this.news.deleteAricles(feed.getFeedId());
						rss.updateArticles(NewsActivity.this, feed);
						List<Article> articles = NewsActivity.this.news.getArticles(feed.getFeedId());
						
						for (Article a : articles) {
							newsItems.add(a.getTitle());
						}
					}
					
					Runnable updateUi = new Runnable() {
						
						@Override
						public void run() {
							if (newsItems.size() == 0)
								Toast.makeText(NewsActivity.this, R.string.FailedGetNews, Toast.LENGTH_LONG);
							else {
								NewsActivity.this.setListAdapter(new ArrayAdapter<String>(NewsActivity.this, R.layout.news_layout, newsItems));
								NewsActivity.this.getListView().setTextFilterEnabled(true);
								NewsActivity.this.progressDialog.dismiss();
							}
						}
					};

					rss.done();
					NewsActivity.this.news.close();
					
					runOnUiThread(updateUi);
				} catch (Exception e) {
					Log.d("cz.tomas.StockAnalyze.NewsActivity", e.getMessage());
				}
			}			
		};	

		Thread thread = new Thread(null, updateNews, "updateNewsThread");
		thread.start();
	}
}
