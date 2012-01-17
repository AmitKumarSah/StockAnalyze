package cz.tomas.StockAnalyze.News;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import cz.tomas.StockAnalyze.News.NewsSqlHelper.ArticleColumns;
import cz.tomas.StockAnalyze.utils.DbQueryUtils;

public final class NewsContentProvider extends ContentProvider {

	public static final String AUTHORITY = "cz.tomas.stockanalyze.news.newscontentprovider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri ARTICLES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/articles");
	public static final Uri FEEDS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/feeds");

	private static final int ARTICLES = 1;
	private static final int ARTICLES_ID = 2;

	private static final int FEEDS = 4;
	private static final int FEEDS_ID = 5;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(AUTHORITY, "articles", ARTICLES);
		//sUriMatcher.addURI(AUTHORITY, "articledetails", ARTICLE_DETAILS);
		sUriMatcher.addURI(AUTHORITY, "articles/#", ARTICLES_ID);
		sUriMatcher.addURI(AUTHORITY, "feeds", FEEDS);
		sUriMatcher.addURI(AUTHORITY, "feeds/#", FEEDS_ID);
	}

	public static final String ARTICLE_CONTENT_TYPE = "vnd.android.cursor.dir/articles";
	//public static final String ARTICLE_DETAIL_CONTENT_TYPE = "vnd.android.cursor.dir/articledetails";
	public static final String ARTICLE_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/articles";
	public static final String FEED_CONTENT_TYPE = "vnd.android.cursor.dir/feeds";
	public static final String FEED_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/feeds";

	private NewsSqlHelper mSqlHelper;

	@Override
	public boolean onCreate() {
		mSqlHelper = NewsSqlHelper.getInstance(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case ARTICLES:
			return ARTICLE_CONTENT_TYPE;
//		case ARTICLE_DETAILS:
//			return ARTICLE_DETAIL_CONTENT_TYPE;
		case ARTICLES_ID:
			return ARTICLE_CONTENT_TYPE_ITEM;
		case FEEDS:
			return FEED_CONTENT_TYPE;
		case FEEDS_ID:
			return FEED_CONTENT_TYPE_ITEM;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mSqlHelper.getWritableDatabase();
		try {
			final int match = sUriMatcher.match(uri);
			switch (match) {
			case ARTICLES_ID:
				final long id = ContentUris.parseId(uri);
				return db.delete(NewsSqlHelper.ARTICLES_TABLE_NAME, 
						DbQueryUtils.getEqualityClause(ArticleColumns.ID, String.valueOf(id)), null);
            default:
                throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
			}
		} finally {
			db.close();
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case ARTICLES:
			SQLiteDatabase db = mSqlHelper.getWritableDatabase();
			long id = db.insert(NewsSqlHelper.ARTICLES_TABLE_NAME, null, values);
			
			if (id > 0) {
				final Uri result = ContentUris.withAppendedId(ARTICLES_CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(uri, null);
				return result;
			}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);	
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(NewsSqlHelper.ARTICLES_TABLE_NAME);
		if (sortOrder == null) {
			sortOrder = ArticleColumns.DEFAULT_SORT;
		}
		
		SQLiteDatabase db = mSqlHelper.getReadableDatabase();
		
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case ARTICLES:
			break;
		case ARTICLES_ID:
			final long id = ContentUris.parseId(uri);
			qb.appendWhere(DbQueryUtils.getEqualityClause(ArticleColumns.ID, String.valueOf(id)));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);	
		}
		
		//final String[] projection = NewsSqlHelper.ArticleColumns.BASE_PROJECTION;
		Cursor cursor = qb.query(db, projection, null, null, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		final int match = sUriMatcher.match(uri);
		
		switch (match) {
		case ARTICLES_ID:
			final long id = ContentUris.parseId(uri);
			SQLiteDatabase db = mSqlHelper.getWritableDatabase();
			final String where = DbQueryUtils.getEqualityClause(ArticleColumns.ID, String.valueOf(id));
			
			count = db.update(NewsSqlHelper.ARTICLES_TABLE_NAME, values, where, null);
			break;
		case ARTICLES:
			SQLiteDatabase db1 = mSqlHelper.getWritableDatabase();
			count = db1.update(NewsSqlHelper.ARTICLES_TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);	
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

}
