/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.net.URL;
import java.util.List;

import android.content.Context;

/**
 * Facade for rss handling - using RssHandler and NewsSqlHelper
 * @author tomas
 *
 */
public class Rss {
	RSSHandler handler;
	NewsSqlHelper sqlHelper;

	/*
	 * constructor, Context is required to connect to database
	 */
	public Rss(Context context) {
		this.handler = new RSSHandler(context);
		this.sqlHelper = this.handler.getDbHelper();
	}
	
	/*
	 * insert new feed to database - does NOT check for duplicates
	 */
	public boolean insertFeed(String title, URL url, String countryCode) {
		return this.sqlHelper.insertFeed(title, url, countryCode);
	}
	
	/*
	 * delete feed from database by its id
	 */
	public void deleteFeed(long feedId) {
		this.sqlHelper.deleteFeed(feedId);
	}
	
	/*
	 * delete all articles from given feed
	 */
	public void deleteArticles(long feedId) {
		this.sqlHelper.deleteArticles(feedId);
	}
	
	/*
	 * download articles from given feed
	 */
	public void fetchArticles(Feed feed) {
		this.handler.updateArticles(feed);
	}
	
	/*
	 * get all articles from given feed
	 */
	public List<Article> getArticles(long feedId) {
		return this.sqlHelper.getArticles(feedId);
	}

	/*
	 * get all feeds stored in database
	 */
	public List<Feed> getFeeds() {
		return this.sqlHelper.getFeeds();
	}
	
	/*
	 * close database
	 */
	public void done() {
		this.handler.done();
	}
}
