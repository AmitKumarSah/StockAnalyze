package cz.tomas.StockAnalyze.News;

import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Task to load articles from database or fetch and merge new articles.
 * Execution has boolean parameter to determine whether to fetch new data or
 * just load them from db.
 * 
 * @author tomas
 *
 */
	
public abstract class NewsItemsTask extends AsyncTask<Boolean, Integer, List<Article>> {
	
	public interface ITaskListener {
		void onUpdateStart();
		void onUpdateFinished();
	}
	
	protected static final int DEFAULT_NEWS_LIMIT = 12;

	protected final Context context;
	protected ITaskListener listener;
	protected Exception ex;
	private Semaphore semaphore;
	protected Rss rss;

	/**
	 * @param context
	 */
	NewsItemsTask(Rss rss, Context context) {
		this.context = context;
		this.semaphore = new Semaphore(1);
		this.rss = rss;
	}
	
	public void setListener(ITaskListener listener) {
		this.listener = listener;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (this.listener != null) {
			this.listener.onUpdateStart();
		}
	}

	@Override
	protected List<Article> doInBackground(Boolean... params) {
		if (params.length < 1) {
			return null;
		}
		try {
			this.semaphore.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		final boolean fetch = params[0];
		List<Article> articles = null;
		
		try {
			rss.sqlHelper.acquireDb(this);
			List<Feed> feeds = rss.getFeeds();
			if (fetch) {
				for (Feed feed : feeds) {
					rss.fetchArticles(feed);
				}
			}
			articles = rss.getArticles();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to read news", e);
			this.ex = e;
		} finally {
			rss.done();
			rss.sqlHelper.releaseDb(true, this);
			this.semaphore.release();
		}
	
		return articles;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(List<Article> result) {
		super.onPostExecute(result);
	}	
}