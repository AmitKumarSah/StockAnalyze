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
import android.util.Log;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;

public class NewsSqlHelper extends StockDataSqlStore {

	private static final String SOURCE_CYRRUS = "http://www.cyrrus.cz/rss/cs";
	private static final String SOURCE_CYRRUS_NAME = "Cyrrus";
	private static final String SOURCE_CYRRUS_COUNTRY = "cz";
	
	public NewsSqlHelper(Context context) {
		super(context);

		// insert default data
		try {
			this.insertFeed(SOURCE_CYRRUS_NAME, new URL(SOURCE_CYRRUS), SOURCE_CYRRUS_COUNTRY);
		} catch (MalformedURLException e) {
			Log.d("NewsSqlHelper", "Failed to insert default news data: " + e.getMessage());
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		
	}
	
	public boolean insertFeed(String title, URL url, String countryCode) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("url", url.toString());
		values.put("country", countryCode);
		
		return (super.getWritableDatabase().insert(FEEDS_TABLE, null, values) > 0);
	}

	public boolean deleteFeed(Long feedId) {
		return (super.getWritableDatabase().delete(FEEDS_TABLE,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	public boolean insertArticle(Long feedId, String title, URL url,
			String description) {
		ContentValues values = new ContentValues();
		values.put("feed_id", feedId);
		values.put("title", title);
		values.put("description", description);
		values.put("url", url.toString());
		
		return (this.getWritableDatabase().insert(ARTICLES_TABLE, null, values) > 0);
	}

	public boolean deleteAricles(Long feedId) {
		return (this.getWritableDatabase().delete(ARTICLES_TABLE,
				"feed_id=" + feedId.toString(), null) > 0);
	}

	public List<Feed> getFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		try {
			Cursor c = super.getWritableDatabase().query(FEEDS_TABLE, new String[] {
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
		}
		return feeds;
	}

	public List<Article> getArticles(Long feedId) {
		ArrayList<Article> articles = new ArrayList<Article>();
		try {
			Cursor c = super.getWritableDatabase().query(ARTICLES_TABLE, new String[] {
					"article_id", "feed_id", "title", "description", "url" }, "feed_id="
					+ feedId.toString(), null, null, null, null);

			c.moveToFirst();
			while (c.moveToNext()) {
				Article article = new Article();
				article.setArticleId(c.getLong(0));
				article.setFeedId(c.getLong(1));
				article.setTitle(c.getString(2));
				article.setDescription(c.getString(3));
				article.setUrl(new URL(c.getString(4)));
				articles.add(article);
			}
		} catch (SQLException e) {
			Log.e("NewsSqlHelper", e.toString());
		} catch (MalformedURLException e) {
			Log.e("NewsSqlHelper", e.toString());
		}
		return articles;
	}
}
