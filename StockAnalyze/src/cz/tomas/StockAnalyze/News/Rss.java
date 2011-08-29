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
package cz.tomas.StockAnalyze.News;

import java.net.URL;
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

	/**
	 * constructor, Context is required to connect to database
	 */
	public Rss(Context context) {
		//this.handler = new RSSHandler(context);
		this.handler = new XmlFeedPullParseHandler(context);
		this.sqlHelper = this.handler.getDbHelper();
	}
	
	/**
	 * insert new feed to database - does NOT check for duplicates
	 */
	public boolean insertFeed(String title, URL url, String countryCode) {
		return this.sqlHelper.insertFeed(title, url, countryCode);
	}
	
	/**
	 * delete feed from database by its id
	 */
	public void deleteFeed(long feedId) {
		this.sqlHelper.deleteFeed(feedId);
	}
	
	/**
	 * delete all articles from given feed
	 */
	public void deleteArticles(long feedId) {
		this.sqlHelper.deleteArticles(feedId);
	}
//	
//	/**
//	 * download and save articles from given feed to database
//	 */
//	@Deprecated
//	public void updateArticles(Feed feed) throws Exception {
//		this.handler.updateArticles(feed);
//	}
	
	/**
	 * download and save new articles from given feed to database
	 * @returns list of merged articles - downloaded + already present in database
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
		//int downloadedCount = downloadedArticles.size();
		if (downloadedArticles.size() > 0)
			for (Article article1 : presentArticles) {
				for (int i = 0; i < downloadedArticles.size(); i++) {
					Article article2 = downloadedArticles.get(i);
					if (article1.getDate() == article2.getDate() &&
							article1.getUrl().equals(article2.getUrl())) {
						downloadedArticles.remove(i);
						break;
					}
				}
			}
		
		if (downloadedArticles.size() > 0)
			this.sqlHelper.insertArticles(feed.getFeedId(), downloadedArticles);
		
		downloadedArticles.addAll(presentArticles);
		return downloadedArticles;
	}
	
	/**
	 * get all articles from given feed that are stored in database
	 */
	public List<Article> getArticles(long feedId) {
		return this.sqlHelper.getArticles(feedId);
	}
	 
	/**
	 * get articles from given feed, limited by limit
	 */
	public List<Article> getArticles(long feedId, int limit) {
		return this.sqlHelper.getArticles(feedId, limit);
	}

	/**
	 * get all feeds stored in database
	 */
	public List<Feed> getFeeds() {
		return this.sqlHelper.getFeeds();
	}
	
	/**
	 * close database
	 */
	public void done() {
		this.handler.done();
	}
}
