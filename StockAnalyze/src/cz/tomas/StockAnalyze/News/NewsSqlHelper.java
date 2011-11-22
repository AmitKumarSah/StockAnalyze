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
package cz.tomas.StockAnalyze.News;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.AbstractSqlHelper;
import cz.tomas.StockAnalyze.utils.Utils;

public final class NewsSqlHelper extends AbstractSqlHelper {

	public static final class FeedColumns {
		public static final String ID = "_id";
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String COUNTRY = "country";
	}
	
	public static final class ArticleColumns {
		public static final String ID = "_id";
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String FEED_ID = "feed_id";
		public static final String DESCRIPTION = "description";
		public static final String TAGS = "tags";
		public static final String DATE = "date";
		public static final String CONTENT = "content";
		public static final String FLAG = "flag";
		public static final String READ = "read";
	}
	
	private final static int DATABASE_VERSION_NUMBER = 2;
	
	private final static String DATABASE_FILE_NAME = "news.db";

	protected static final String FEEDS_TABLE_NAME = "feeds";
	protected static final String ARTICLES_TABLE_NAME = "articles";
	
	private static final String TABLE_DROP =
		"DROP TABLE IF EXISTS ";
	
	private static final String CREATE_TABLE_FEEDS = "CREATE TABLE " + FEEDS_TABLE_NAME + " (" +
			FeedColumns.ID + " integer PRIMARY KEY AUTOINCREMENT, " +
			FeedColumns.TITLE + " text not null," +
			FeedColumns.URL + " text not null, " +
			FeedColumns.COUNTRY + " text not null);";

	private static final String CREATE_TABLE_ARTICLES = "CREATE TABLE " + ARTICLES_TABLE_NAME + " (" +
			ArticleColumns.ID + " integer PRIMARY KEY AUTOINCREMENT, " +
			ArticleColumns.FEED_ID + " integer not null, " +
			ArticleColumns.TITLE + " text not null, " +
			ArticleColumns.DESCRIPTION + " text, " +
			ArticleColumns.TAGS + " text, " +
			ArticleColumns.URL + " text not null, " +
			ArticleColumns.DATE + " integer not null, " +
			ArticleColumns.CONTENT + " text, " +
			ArticleColumns.READ + " integer, " +
			ArticleColumns.FLAG + " integer, " +
			"FOREIGN KEY(" + ArticleColumns.ID + ") REFERENCES " + FEEDS_TABLE_NAME + "(" + FeedColumns.ID + "));";
	
	public static final int FLAG_TO_DELETE = 11;
	public static final int FLAG_FRESH = 0;
	
//	private static final String SOURCE_CYRRUS = "http://www.cyrrus.cz/rss/cs";
//	private static final String SOURCE_CYRRUS_NAME = "Cyrrus";
//	private static final String SOURCE_CYRRUS_COUNTRY = "cz";
	
//	private static final String SOURCE_AKCIE = "http://www.akcie.cz/rss/novinky-a-zpravy.xml";
//	//private static final String SOURCE_AKCIE = "http://www.akcie.cz/rss/zpravy2.xml";
//	private static final String SOURCE_AKCIE_NAME = "Akcie.cz";
//	private static final String SOURCE_AKCIE_COUNTRY = "cz";
	
	private static final String SOURCE_NAME2 = "reuters";
	private static final String SOURCE2= "http://feeds.reuters.com/reuters/globalmarketsNews";
	private static final String SOURCE_COUNTRY2 = "en";
	
	private static final String SOURCE_NAME1 = "bloomberg";
	private static final String SOURCE1 = "http://m.bloomberg.com/android/apps/wds/news/regioneurope.xml.asp";
	private static final String SOURCE_COUNTRY1 = "en";
	
	private static final int DEFAULT_ARTICLE_LIMIT = 20;
	
	private static NewsSqlHelper instance;
	/**
	 * shared builder
	 */
	private StringBuilder builder;
	
	static NewsSqlHelper getInstance(Context context) {
		if (instance == null) {
			instance = new NewsSqlHelper(context);
		}
		return instance;
	}
	
	private NewsSqlHelper(Context context) {
		super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION_NUMBER);
		this.builder = new StringBuilder();
	}

	/**
	 *insert default feed(s) 
	 */
	protected void insertDefaultFeed(SQLiteDatabase db) {
		try {
			Log.d(Utils.LOG_TAG, "Inserting default rss feed source..." + SOURCE_NAME1);
			
			final ContentValues values = new ContentValues();
			values.put("title", SOURCE_NAME1);
			values.put("url", SOURCE1);
			values.put("country", SOURCE_COUNTRY1);
			db.insert(FEEDS_TABLE_NAME, null, values);

			Log.d(Utils.LOG_TAG, "Inserting default rss feed source..." + SOURCE_NAME2);
			values.clear();
			values.put("title", SOURCE_NAME2);
			values.put("url", SOURCE2);
			values.put("country", SOURCE_COUNTRY2);
			db.insert(FEEDS_TABLE_NAME, null, values);
		} catch (SQLException e) {
			Log.d(Utils.LOG_TAG, "Failed to insert default news data: " + e.getMessage());
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(Utils.LOG_TAG, "creating Feeds table!");
		db.execSQL(CREATE_TABLE_FEEDS);
		Log.d(Utils.LOG_TAG, "creating Articles table!");
		db.execSQL(CREATE_TABLE_ARTICLES);
		
		this.insertDefaultFeed(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(TABLE_DROP + ARTICLES_TABLE_NAME);
		db.execSQL(TABLE_DROP + FEEDS_TABLE_NAME);		
		onCreate(db);
	}
	
	/**
	 * insert new feed to db
	 */
	public boolean insertFeed(String title, URL url, String countryCode) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(FeedColumns.TITLE, title);
		values.put(FeedColumns.URL, url.toString());
		values.put(FeedColumns.COUNTRY, countryCode);
		
		SQLiteDatabase db = null;
		boolean inserted;
		try {
			db = super.getWritableDatabase();
			inserted = db.insert(FEEDS_TABLE_NAME, null, values) > 0;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return inserted;
	}

	/**
	 * delete rss feed from db
	 */
	public boolean deleteFeed(Long feedId) throws SQLException {
		SQLiteDatabase db = null;
		boolean deleted;
		try {
			db = super.getWritableDatabase();
			deleted = db.delete(FEEDS_TABLE_NAME,"_id=" + feedId.toString(), null) > 0;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return deleted;
	}

	/**
	 * insert one article to db
	 */
	public boolean insertArticle(SQLiteDatabase db, Long feedId, String title, URL url,
			String description, long date, String content) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(ArticleColumns.FEED_ID, feedId);
		values.put(ArticleColumns.TITLE, title);
		values.put(ArticleColumns.DESCRIPTION, description);
		values.put(ArticleColumns.URL, url.toString());
		values.put(ArticleColumns.DATE, date);
		if (content != null) {
			values.put("content", content);
		}
		boolean result = (db.insert(ARTICLES_TABLE_NAME, null, values) > 0);
		return result;
	}

	/**
	 * delete articles from given feed
	 */
	public boolean deleteArticles(Long feedId) throws SQLException {
		SQLiteDatabase db = null;
		int count;
		try {
			 db = this.getWritableDatabase();
			count = db.delete(ARTICLES_TABLE_NAME, ArticleColumns.ID + "=" + feedId.toString(), null);
		} finally {
			this.close();
		}
		return (count > 0);
	}

	/**
	 * get all feeds
	 */
	public List<Feed> getFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = this.getWritableDatabase();
			c = db.query(FEEDS_TABLE_NAME, new String[] {
					FeedColumns.ID, FeedColumns.TITLE, FeedColumns.URL, FeedColumns.COUNTRY }, null, null, null, null, null);

			if (c.moveToFirst()) {
				do {
					Feed feed = new Feed();
					feed.setFeedId(c.getLong(0));
					feed.setTitle(c.getString(1));
					feed.setUrl(new URL(c.getString(2)));
					feed.setCountryCode(c.getString(3));
					feeds.add(feed);
				} while (c.moveToNext());
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} catch (MalformedURLException e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} finally {
			if (db != null) {
				if (c != null)
					c.close();
				this.close();
			}
		}
		return feeds;
	}

	/**
	 * read all articles from database that belongs to the feed
	 * @returns list of articles, if no articles were found, list would be empty 
	 */
	public List<Article> getArticles(Long feedId) {
		return this.getArticles(feedId, DEFAULT_ARTICLE_LIMIT);
	}
	
	/**
	 * get all articles from all feeds
	 * @return
	 */
	public List<Article> getArticles() {
		ArrayList<Article> articles = new ArrayList<Article>();
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			c = db.query(ARTICLES_TABLE_NAME, new String[] {
					ArticleColumns.ID, ArticleColumns.FEED_ID, ArticleColumns.TITLE, ArticleColumns.DESCRIPTION, 
					ArticleColumns.URL, ArticleColumns.DATE, ArticleColumns.CONTENT }, 
					null, null, null, null, ArticleColumns.DATE + " DESC", null);

			if (c.moveToFirst())
				do {
					Article article = new Article();
					article.setArticleId(c.getLong(0));
					article.setFeedId(c.getLong(1));
					article.setTitle(c.getString(2));
					article.setDescription(c.getString(3));
					article.setUrl(new URL(c.getString(4)));
					article.setDate(Long.parseLong(c.getString(5)));
					article.setContent(c.getString(6));
					articles.add(article);
				} while (c.moveToNext());
			else
				Log.d(Utils.LOG_TAG, "no articles present");
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} finally {
			if (c != null) {
                c.close();
				this.close();
			}
		}
		return articles;
	}
	
	/**
	 * get cursor of articles that belongs to given feed
	 * @returns cusor at first position
	 */
	public Cursor getAllArticlesCursor() {
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			c = db.query(ARTICLES_TABLE_NAME, new String[] {
					ArticleColumns.ID, ArticleColumns.FEED_ID, ArticleColumns.TITLE, ArticleColumns.DESCRIPTION, 
					ArticleColumns.URL, ArticleColumns.DATE, ArticleColumns.CONTENT, ArticleColumns.READ }, 
					null, null, null, null, ArticleColumns.DATE + " DESC", null);
			
			if (!c.moveToFirst()) {
				Log.w(Utils.LOG_TAG, "articles cursor is empty");
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to read articles cursor", e);
		} finally {
			this.close();
		}
		return c;
	}
	
	/**
	 * read articles limited by limit from database that belongs to the feed
	 * @returns list of articles limited by limit, 
	 * if no articles were found, list would be empty 
	 */
	public List<Article> getArticles(Long feedId, int limit) {
		ArrayList<Article> articles = new ArrayList<Article>();
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			c = db.query(ARTICLES_TABLE_NAME, new String[] {
					ArticleColumns.ID, ArticleColumns.FEED_ID, ArticleColumns.TITLE, ArticleColumns.DESCRIPTION, 
					ArticleColumns.URL, ArticleColumns.DATE, ArticleColumns.CONTENT }, 
					ArticleColumns.FEED_ID + "="
					+ feedId.toString(), null, null, null, ArticleColumns.DATE + " DESC", String.valueOf(limit));

			if (c.moveToFirst())
				do {
					Article article = new Article();
					article.setArticleId(c.getLong(0));
					article.setFeedId(c.getLong(1));
					article.setTitle(c.getString(2));
					article.setDescription(c.getString(3));
					article.setUrl(new URL(c.getString(4)));
					article.setDate(Long.parseLong(c.getString(5)));
					article.setContent(c.getString(6));
					articles.add(article);
				} while (c.moveToNext());
			else
				Log.d(Utils.LOG_TAG, "no articles present in feed " + String.valueOf(feedId));
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, e.toString());
		} finally {
			if (db != null) {
				if (c != null)
					c.close();
				this.close();
			}
		}
		return articles;
	}

	/**
	 * insert articles to the feed in transaction
	 */
	public void insertArticles(long feedId, List<Article> articles) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();
			for (Article article : articles) {
				this.insertArticle(db, feedId, article.getTitle(), article.getUrl(), article.getDescription(), article.getDate(), article.getContent());
			}
			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
			}
			this.close();
		}
	}

	public void updateArticleContent(Article article) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(ArticleColumns.CONTENT, article.getContent());
			db.update(ARTICLES_TABLE_NAME, values, ArticleColumns.ID + "=?", 
					new String[] { String.valueOf(article.getArticleId()) });
		} finally {
			this.close();
		}
	}
	/**
	 * mark all current articles with {@link #FLAG_TO_DELETE}
	 */
	public void markArticlesToDelete() {
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		values.put(ArticleColumns.FLAG, FLAG_TO_DELETE);
		try {
			db = this.getWritableDatabase();
			db.update(ARTICLES_TABLE_NAME, values, null, null);
		} finally {
			this.close();
		}
	}

	public void markArticlesFresh(List<String> articleIds) {
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		values.put(ArticleColumns.FLAG, FLAG_FRESH);
		try {
			db = this.getWritableDatabase();
			String[] array = articleIds.toArray(new String[articleIds.size()]);
			Log.d(Utils.LOG_TAG, "marking fresh articles: " + Arrays.toString(array));
			builder.setLength(0);
			for (int i = 0; i < array.length; i++) {
				builder.append("?,");
			}
			builder.setLength(builder.length() - 1);
			db.update(ARTICLES_TABLE_NAME, values, String.format("%s in (%s)", 
					ArticleColumns.ID, builder.toString()), array);
		} finally {
			this.close();
		}
	}
	
	/**
	 * delete all articles marked with FLAG_TO_DELETE
	 * @param feedId 
	 */
	public void deleteOldArticles(long feedId) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			final String where = String.format("%s=? AND %s=?", ArticleColumns.FLAG, ArticleColumns.FEED_ID);
			int deletedCount = db.delete(ARTICLES_TABLE_NAME, where, 
					new String[] {String.valueOf(FLAG_TO_DELETE), String.valueOf(feedId)});
			Log.d(Utils.LOG_TAG, "deleted old articles: " + deletedCount);
		} finally {
			this.close();
		}
	}
}
