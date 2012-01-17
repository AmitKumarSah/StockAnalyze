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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetNewsException;
import cz.tomas.StockAnalyze.News.NewsSqlHelper.ArticleColumns;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Facade for rss handling - using {@link XmlFeedPullParseHandler} and {@link NewsSqlHelper}
 * @author tomas
 *
 */
public class Rss {
	
	final XmlFeedPullParseHandler handler;
	final NewsSqlHelper sqlHelper;
	private final Context context;
	

	/**
	 * constructor, Context is required to connect to database
	 */
	public Rss(Context context) {
		this.handler = new XmlFeedPullParseHandler(context);
		this.sqlHelper = NewsSqlHelper.getInstance(context);
		this.context = context;
	}

//	/**
//	 * insert new feed to database - does NOT check for duplicates
//	 */
//	public boolean insertFeed(String title, URL url, String countryCode) {
//		return this.sqlHelper.insertFeed(title, url, countryCode);
//	}
	
//	/**
//	 * delete feed from database by its id
//	 */
//	public void deleteFeed(long feedId) {
//		this.sqlHelper.deleteFeed(feedId);
//	}
	
	/**
	 * download and save new articles from all feeds to database
	 */
	public void fetchArticles() {
		Collection<Feed> feeds = getFeeds();

		for (Feed feed : feeds) {
			this.fetchArticles(feed);
		}

		this.context.getContentResolver().notifyChange(NewsContentProvider.ARTICLES_CONTENT_URI, null, false);
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
			throw new FailedToGetNewsException("failed to download updated news ", e);
		}
		Map<Article, String> presentFreshArticles = new HashMap<Article, String>();	// present articles, that were also downloaded
		SQLiteDatabase db = null; 		
		try {
			db = this.sqlHelper.getWritableDatabase();
			db.beginTransaction();
			
			//this.sqlHelper.acquireDb(this);
			//presentArticles = this.getArticles(feed.getFeedId());
			this.sqlHelper.markArticlesToDelete(db);
			
			// prevent duplicities
//			if (downloadedArticles.size() > 0) {
//				for (Article presentArticle : presentArticles) {
//					for (int i = 0; i < downloadedArticles.size(); i++) {
//						final Article downloadedArticle = downloadedArticles.get(i);
//						
//						if (presentArticle.getUrl().equals(downloadedArticle.getUrl())) {
//							downloadedArticles.remove(i);
//							presentFreshArticles.add(String.valueOf(presentArticle.getArticleId()));
//							break;
//						}
//					}
//				}
//			}
			//get list of entries existing in db
			final Map<String, Integer> existing = new HashMap<String, Integer>();
			final Cursor c = db.query(NewsSqlHelper.ARTICLES_TABLE_NAME, new String[] { ArticleColumns.URL, ArticleColumns.ID}, null, null, null, null, null);
			try {
				while (c.moveToNext()) {
					final int indexLink = c.getColumnIndex(ArticleColumns.URL);
					final int indexId = c.getColumnIndex(ArticleColumns.ID);
					existing.put(c.getString(indexLink), c.getInt(indexId));
				}
			} finally {
				c.close();
			}
			
			for (Article article : downloadedArticles) {
				final String key = article.getUrl().toString();
				if (existing.containsKey(key)) {
					presentFreshArticles.put(article, String.valueOf(existing.get(key)));
				}
			}
			for (Article article : presentFreshArticles.keySet()) {
				downloadedArticles.remove(article);
			}
			
			if (downloadedArticles.size() > 0) {
				this.sqlHelper.insertArticles(feed.getFeedId(), downloadedArticles, db);
			}
			if (presentFreshArticles.size() > 0) {
				this.sqlHelper.markArticlesFresh(presentFreshArticles.values(), db);
			}
			this.sqlHelper.deleteOldArticles(feed.getFeedId(), db);
			db.setTransactionSuccessful();
			
			final FetchContentTask task = new FetchContentTask(downloadedArticles);
			task.start();
		} finally {
			if (db != null) {
				db.endTransaction();
				//this.sqlHelper.releaseDb(true, this);
				db.close();
			}
		}
	}

	private void downloadContent(Article article) throws IOException {
		byte[] content = DownloadService.GetInstance().DownloadFromUrl(article.getMobilizedUrl(), false);
		String html = new String(content);
		//article.setContent(html);
		//this.sqlHelper.updateArticleContent(article);
		ContentValues values = new ContentValues();
		values.put(NewsSqlHelper.ArticleColumns.CONTENT, html);
		this.context.getContentResolver().update(NewsContentProvider.CONTENT_URI, values, null, null);
	}

	public void downloadContent(List<Article> list) {
		for (Article article : list) {
			try {
				if (TextUtils.isEmpty(article.getContent())) {
					this.downloadContent(article);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to download and save content of article " + article, e);
			}
		}
//		try {
//			this.sqlHelper.acquireDb(this);
//			for (Article article : list) {
//				try {
//					if (TextUtils.isEmpty(article.getContent())) {
//						this.downloadContent(article);
//					}
//				} catch (Exception e) {
//					Log.e(Utils.LOG_TAG, "failed to download content of article " + article, e);
//				}
//			}
//		} finally {
//			this.sqlHelper.releaseDb(true, this);
//		}
	}

//	/**
//	 * get all articles in database
//	 * @return
//	 */
//	public List<Article> getArticles() {
//		return this.sqlHelper.getArticles();
//	}
	
//	/**
//	 * get all articles from given feed that are stored in database
//	 */
//	public List<Article> getArticles(long feedId) {
//		return this.sqlHelper.getArticles(feedId);
//	}

	/**
	 * get all feeds stored in database
	 */
	public Collection<Feed> getFeeds() {
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
