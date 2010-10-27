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
import android.widget.ArrayAdapter;

/**
 * @author tomas
 *
 */
public class NewsActivity extends ListActivity {

	NewsSqlHelper news;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		RSSHandler rss = new RSSHandler();
		
		ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
		
		progressDialog.show();
		try {
			this.news = new NewsSqlHelper(this);
			List<String> newsItems = new ArrayList<String>();
			
			List<Feed> feeds = this.news.getFeeds();
			// TODO own thread!!
			for (Feed feed : feeds) {
				rss.updateArticles(this, feed);
				List<Article> articles = this.news.getArticles(feed.getFeedId());
				
				for (Article a : articles) {
					newsItems.add(a.getTitle());
				}
			}
			
//		String[] test = new String[] { "Telefonica krachuje!", "Kit Digital ma monopol"};
			this.setListAdapter(new ArrayAdapter<String>(this, R.layout.news_layout, newsItems));
			this.getListView().setTextFilterEnabled(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		progressDialog.dismiss();
		rss.done();
	}
}
