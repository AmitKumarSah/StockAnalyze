package cz.tomas.StockAnalyze.News;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataSqlHelper;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;

public final class NewsSqlHelper extends DataSqlHelper {

	private static final String SOURCE_CYRRUS = "http://www.cyrrus.cz/rss/cs";
	private static final String SOURCE_CYRRUS_NAME = "Cyrrus";
	private static final String SOURCE_CYRRUS_COUNTRY = "cz";
	
	public NewsSqlHelper(Context context) {
		super(context);

		// insert default data
		try {
			if (this.getFeeds().size() == 0) {
				Log.d("cz.tomas.StockAnalyze.News.NewsSqlHelper", "Inserting default rss feed source...");
				if (! this.insertFeed(SOURCE_CYRRUS_NAME, new URL(SOURCE_CYRRUS), SOURCE_CYRRUS_COUNTRY))
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
	
	public boolean insertFeed(String title, URL url, String countryCode) throws SQLException {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("url", url.toString());
		values.put("country", countryCode);
		
		return (super.getWritableDatabase().insert(FEEDS_TABLE, null, values) > 0);
	}

	public boolean deleteFeed(Long feedId) throws SQLException {
		return (super.getWritableDatabase().delete(FEEDS_TABLE,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	public boolean insertArticle(Long feedId, String title, URL url,
			String description, long date) throws SQLException {
		ContentValues values = new ContentValues();
		values.put("feed_id", feedId);
		values.put("title", title);
		values.put("description", description);
		values.put("url", url.toString());
		values.put("date", date);
		
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		Boolean result = (db.insert(ARTICLES_TABLE, null, values) > 0);
		if (result)
			db.setTransactionSuccessful();
		db.endTransaction();
		return result;
	}

	public boolean deleteAricles(Long feedId) throws SQLException {
		return (this.getWritableDatabase().delete(ARTICLES_TABLE,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	public List<Feed> getFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		Cursor c = null;
		try {
			c = super.getReadableDatabase().query(FEEDS_TABLE, new String[] {
					"feed_id", "title", "url", "country" }, null, null, null, null, null);

			c.moveToFirst();
			while (c.moveToNext()) {
				Feed feed = new Feed();
				feed.setFeedId(c.getLong(0));
				feed.setTitle(c.getString(1));
				feed.setUrl(new URL(c.getString(2)));
				feed.setCountryCode(c.getString(3));
				feeds.add(feed);
			}
		} catch (SQLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} catch (MalformedURLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} finally {
			if (c != null)
				c.close();
		}
		return feeds;
	}

	public List<Article> getArticles(Long feedId) {
		ArrayList<Article> articles = new ArrayList<Article>();
		Cursor c = null;
		try {
			c = super.getWritableDatabase().query(ARTICLES_TABLE, new String[] {
					"article_id", "feed_id", "title", "description", "url", "date" }, "feed_id="
					+ feedId.toString(), null, null, null, null);

			c.moveToFirst();
			while (c.moveToNext()) {
				Article article = new Article();
				article.setArticleId(c.getLong(0));
				article.setFeedId(c.getLong(1));
				article.setTitle(c.getString(2));
				article.setDescription(c.getString(3));
				article.setUrl(new URL(c.getString(4)));
				article.setDate(Long.parseLong(c.getString(5)));
				articles.add(article);
			}
		} catch (SQLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} catch (MalformedURLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} finally {
			if (c != null)
				c.close();
		}
		return articles;
	}
}
