package cz.tomas.StockAnalyze.News;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataSqlHelper;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.utils.Utils;

public final class NewsSqlHelper extends DataSqlHelper {

//	private static final String SOURCE_CYRRUS = "http://www.cyrrus.cz/rss/cs";
//	private static final String SOURCE_CYRRUS_NAME = "Cyrrus";
//	private static final String SOURCE_CYRRUS_COUNTRY = "cz";
	
	private static final String SOURCE_AKCIE = "http://www.akcie.cz/rss/novinky-a-zpravy.xml";
	//private static final String SOURCE_AKCIE = "http://www.akcie.cz/rss/zpravy2.xml";
	private static final String SOURCE_AKCIE_NAME = "Akcie.cz";
	private static final String SOURCE_AKCIE_COUNTRY = "cz";
	
	private static final int DEFAULT_ARTICLE_LIMIT = 20;
	
	NewsSqlHelper(Context context) {
		super(context);

		// insert default data
		try {
			if (this.getFeeds().size() == 0) {
				Log.d("cz.tomas.StockAnalyze.News.NewsSqlHelper", "Inserting default rss feed source..." + SOURCE_AKCIE_NAME);
//				if (! this.insertFeed(SOURCE_CYRRUS_NAME, new URL(SOURCE_CYRRUS), SOURCE_CYRRUS_COUNTRY))
//					throw new SQLException("The feed record wasn't inserted.");
				if (! this.insertFeed(SOURCE_AKCIE_NAME, new URL(SOURCE_AKCIE), SOURCE_AKCIE_COUNTRY))
					throw new SQLException("The feed record wasn't inserted.");
			}
		} catch (MalformedURLException e) {
			Log.d("cz.tomas.StockAnalyze.News.NewsSqlHelper", "Failed to insert default news data, check the address: " + e.getMessage());
		} catch (SQLException e) {
			Log.d("cz.tomas.StockAnalyze.News.NewsSqlHelper", "Failed to insert default news data: " + e.getMessage());
		} finally {
			this.close();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		
	}
	
	/*
	 * insert new feed to db
	 */
	public boolean insertFeed(String title, URL url, String countryCode) throws SQLException {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("url", url.toString());
		values.put("country", countryCode);
		
		return (super.getWritableDatabase().insert(FEEDS_TABLE_NAME, null, values) > 0);
	}

	/*
	 * delete rss feed from db
	 */
	public boolean deleteFeed(Long feedId) throws SQLException {
		return (super.getWritableDatabase().delete(FEEDS_TABLE_NAME,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	/*
	 * insert one article to db
	 */
	public boolean insertArticle(Long feedId, String title, URL url,
			String description, long date) throws SQLException {
		ContentValues values = new ContentValues();
		values.put("feed_id", feedId);
		values.put("title", title);
		values.put("description", description);
		values.put("url", url.toString());
		values.put("date", date);
		
		SQLiteDatabase db = this.getWritableDatabase();
		Boolean result = (db.insert(ARTICLES_TABLE_NAME, null, values) > 0);
		return result;
	}

	/*
	 * delete articles from given feed
	 */
	public boolean deleteArticles(Long feedId) throws SQLException {
		return (this.getWritableDatabase().delete(ARTICLES_TABLE_NAME,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	/*
	 * get all feeds
	 */
	public List<Feed> getFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		Cursor c = null;
		try {
			c = this.getWritableDatabase().query(FEEDS_TABLE_NAME, new String[] {
					"feed_id", "title", "url", "country" }, null, null, null, null, null);

			c.moveToFirst();
			do {
				Feed feed = new Feed();
				feed.setFeedId(c.getLong(0));
				feed.setTitle(c.getString(1));
				feed.setUrl(new URL(c.getString(2)));
				feed.setCountryCode(c.getString(3));
				feeds.add(feed);
			} while (c.moveToNext());
		} catch (SQLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} catch (MalformedURLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e("NewsSqlHelper", e.toString());
		} finally {
			if (c != null)
				c.close();
		}
		return feeds;
	}

	/*
	 * read all articles from database that belongs to the feed
	 * @returns list of articles, if no articles were found, list would be empty 
	 */
	public List<Article> getArticles(Long feedId) {
		return this.getArticles(feedId, DEFAULT_ARTICLE_LIMIT);
	}
	
	/*
	 * read articles limited by limit from database that belongs to the feed
	 * @returns list of articles limited by limit, 
	 * if no articles were found, list would be empty 
	 */
	public List<Article> getArticles(Long feedId, int limit) {
		ArrayList<Article> articles = new ArrayList<Article>();
		Cursor c = null;
		try {
			c = this.getWritableDatabase().query(ARTICLES_TABLE_NAME, new String[] {
					"article_id", "feed_id", "title", "description", "url", "date" }, "feed_id="
					+ feedId.toString(), null, null, null, "date DESC", String.valueOf(limit));

			if (c.moveToFirst())
				do {
					Article article = new Article();
					article.setArticleId(c.getLong(0));
					article.setFeedId(c.getLong(1));
					article.setTitle(c.getString(2));
					article.setDescription(c.getString(3));
					article.setUrl(new URL(c.getString(4)));
					article.setDate(Long.parseLong(c.getString(5)));
					articles.add(article);
				} while (c.moveToNext());
			else
				Log.d(Utils.LOG_TAG, "no articles present in feed " + String.valueOf(feedId));
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} finally {
			if (c != null)
				c.close();
		}
		return articles;
	}

	/*
	 * insert articles to the feed in transaction
	 */
	public void insertArticles(long feedId, List<Article> articles) {
		this.getWritableDatabase().beginTransaction();
		try {
			for (Article article : articles) {
				this.insertArticle(feedId, article.getTitle(), article.getUrl(), article.getDescription(), article.getDate());
			}
			this.getWritableDatabase().setTransactionSuccessful();
		} finally {
			this.getWritableDatabase().endTransaction();
		}
	}
}
