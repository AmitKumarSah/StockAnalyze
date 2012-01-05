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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetNewsException;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Facade for rss handling - using {@link XmlFeedPullParseHandler} and {@link NewsSqlHelper}
 * @author tomas
 *
 */
public class Rss {
	
	interface IFetchListener {
		void onDataFetched();
	}
	
	final XmlFeedPullParseHandler handler;
	final NewsSqlHelper sqlHelper;
	
	private final List<IFetchListener> listeners;

	/**
	 * constructor, Context is required to connect to database
	 */
	public Rss(Context context) {
		this.handler = new XmlFeedPullParseHandler(context);
		this.sqlHelper = NewsSqlHelper.getInstance(context);
		this.listeners = new ArrayList<Rss.IFetchListener>();
	}

	public void addListener(IFetchListener listener) {
		if (listener == null) {
			return; 
		}
		this.listeners.add(listener);
	}
	
	public boolean removeListener(IFetchListener listener) {
		return this.listeners.remove(listener);
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
	
	/**
	 * download and save new articles from all feeds to database
	 */
	public void fetchArticles() {
		List<Feed> feeds = getFeeds();

		this.sqlHelper.acquireDb(this);
		try {
			for (Feed feed : feeds) {
				this.fetchArticles(feed);
			}
			for (IFetchListener listener : this.listeners) {
				listener.onDataFetched();
			}
		} finally {
			this.sqlHelper.releaseDb(true, this);
		}
	}
	
	/**
	 * download and save new articles from given feed to database
	 */
	public void fetchArticles(Feed feed) throws FailedToGetNewsException {
		List<Article> downloadedArticles = null;
		try {
			downloadedArticles = this.handler.fetchArticles(feed);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to download new articles", e);
			String message = "failed to download updated news ";
			throw new FailedToGetNewsException(message, e);
		}
		List<Article> presentArticles;
		List<String> presentFreshArticles = new ArrayList<String>();	// present articles, that were also downloaded
		SQLiteDatabase db = null; 		
		try {
			db = this.sqlHelper.getWritableDatabase();
			db.beginTransaction();
			this.sqlHelper.acquireDb(this);
			presentArticles = this.getArticles(feed.getFeedId());
			this.sqlHelper.markArticlesToDelete();
			
			// prevent duplicities
			if (downloadedArticles.size() > 0) {
				for (Article presentArticle : presentArticles) {
					for (int i = 0; i < downloadedArticles.size(); i++) {
						final Article downloadedArticle = downloadedArticles.get(i);
						
						if (presentArticle.getUrl().equals(downloadedArticle.getUrl())) {
							downloadedArticles.remove(i);
							presentFreshArticles.add(String.valueOf(presentArticle.getArticleId()));
							break;
						}
					}
				}
			}
			if (downloadedArticles.size() > 0) {
				this.sqlHelper.insertArticles(feed.getFeedId(), downloadedArticles);
			}
			if (presentFreshArticles.size() > 0) {
				this.sqlHelper.markArticlesFresh(presentFreshArticles);
			}
			this.sqlHelper.deleteOldArticles(feed.getFeedId());
			db.setTransactionSuccessful();
			final FetchContentTask task = new FetchContentTask(downloadedArticles);
			task.start();
		} finally {
			if (db != null) {
				db.endTransaction();
				this.sqlHelper.releaseDb(true, this);
				this.sqlHelper.close();
			}
		}
	}

	private void downloadContent(Article article) throws IOException {
		byte[] content = DownloadService.GetInstance().DownloadFromUrl(article.getMobilizedUrl(), false);
		String html = new String(content);
		article.setContent(html);
		this.sqlHelper.updateArticleContent(article);
	}

	public void downloadContent(List<Article> list) {
		try {
			this.sqlHelper.acquireDb(this);
			for (Article article : list) {
				try {
					if (TextUtils.isEmpty(article.getContent())) {
						this.downloadContent(article);
					}
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to download content of article " + article, e);
				}
			}
		} finally {
			this.sqlHelper.releaseDb(true, this);
		}
	}
	
	public Cursor getAllArticlesCursor() {
		return this.sqlHelper.getAllArticlesCursor();
	}
	
	public Cursor getAllFullArticlesCursor() {
		return this.sqlHelper.getAllArticlesCursor();
	}

	/**
	 * get all articles in database
	 * @return
	 */
	public List<Article> getArticles() {
		return this.sqlHelper.getArticles();
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
	
	private final class FetchContentTask extends Thread {

		private List<Article> articles;
		
		public FetchContentTask(List<Article> articles) {
			this.articles = articles;
		}

		@Override
		public void run() {
			if (articles == null || articles.size() != 1) {
				return;
			}
			
			downloadContent(this.articles);
		}
	}
}
