package cz.tomas.StockAnalyze.News;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.News.Rss.IFetchListener;
import cz.tomas.StockAnalyze.utils.Utils;

public class NewsLoader extends AsyncTaskLoader<Cursor> implements IFetchListener {

	public static final int MODE_PEREX = 1;
	public static final int MODE_FULL = 2;
	
	private final Rss rss;
	private Cursor currentCursor;
	
	private final int mode;
	
	public NewsLoader(Context context, int mode) {
		super(context);
		this.rss = (Rss) context.getApplicationContext().getSystemService(Application.RSS_SERVICE);
		this.mode = mode;
	}

	@Override
	public Cursor loadInBackground() {
		try {
			rss.sqlHelper.acquireDb(this);
			if (this.mode == MODE_PEREX) {
				currentCursor = rss.getAllArticlesCursor();
			} else if (this.mode == MODE_FULL) {
				currentCursor = rss.getAllFullArticlesCursor();
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to read news", e);
		} finally {
			rss.sqlHelper.releaseDb(true, this);
		}
	
		return currentCursor;
	}

	@Override
	protected void onStartLoading() {
		if (this.currentCursor != null) {
			this.currentCursor.close();
		}
		this.rss.addListener(this);
		this.forceLoad();
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		if (this.currentCursor != null) {
			this.currentCursor.close();
		}
		this.rss.addListener(null);
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		this.rss.removeListener(this);
	}

	@Override
	public void onDataFetched() {
		this.onContentChanged();
	}
}
