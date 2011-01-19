/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetNewsException;

import android.content.Context;

/**
 * Facade for rss handling - using RssHandler and NewsSqlHelper
 * @author tomas
 *
 */
public class Rss {
	//RSSHandler handler;
	XmlFeedPullParseHandler handler;
	NewsSqlHelper sqlHelper;

	/*
	 * constructor, Context is required to connect to database
	 */
	public Rss(Context context) {
		//this.handler = new RSSHandler(context);
		this.handler = new XmlFeedPullParseHandler(context);
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
	 * download and save articles from given feed to database
	 */
	public void updateArticles(Feed feed) throws Exception {
		this.handler.updateArticles(feed);
	}
	
	/*
	 * download and save new articles from given feed to database
	 */
	public List<Article> fetchArticles(Feed feed) throws FailedToGetNewsException {
		List<Article> downloadedArticles = null;
		try {
			downloadedArticles = this.handler.fetchArticles(feed);
		} catch (Exception e) {
			e.printStackTrace();
			String message = "failed to download updated news: ";
			if (e.getMessage() != null)
				message += e.getMessage();
			throw new FailedToGetNewsException(message, e);
		}
		List<Article> presentArticles = this.getArticles(feed.getFeedId());
		
		// prevent duplicities
		int downloadedCount = downloadedArticles.size();
		for (Article article1 : presentArticles) {
			for (int i = 0; i < downloadedArticles.size(); i++) {
				Article article2 = downloadedArticles.get(i);
				if (article1.getDate() == article2.getDate()) {
					downloadedArticles.remove(i);
					break;
				}
			}
		}
		
		this.sqlHelper.insertArticles(feed.getFeedId(), downloadedArticles);
		
		downloadedArticles.addAll(presentArticles);
		return downloadedArticles;
	}
	
	/*
	 * get all articles from given feed
	 */
	public List<Article> getArticles(long feedId) {
		return this.sqlHelper.getArticles(feedId);
	}
	 
	/*
	 * get articles from given feed, limited by limit
	 */
	public List<Article> getArticles(long feedId, int limit) {
		return this.sqlHelper.getArticles(feedId, limit);
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
