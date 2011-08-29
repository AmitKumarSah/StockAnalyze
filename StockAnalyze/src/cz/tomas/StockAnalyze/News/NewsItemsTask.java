package cz.tomas.StockAnalyze.News;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.Utils;

public abstract class NewsItemsTask extends AsyncTask<Void, Integer, List<Article>> {
	
	public interface ITaskFinishedListener {
		void onUpdateFinished();
	}
	
	protected static final int DEFAULT_NEWS_LIMIT = 20;

	protected final Context context;
	protected ITaskFinishedListener listener;
	protected Exception ex;

	/**
	 * @param context
	 */
	NewsItemsTask(Context context) {
		this.context = context;
	}
	
	public void setListener(ITaskFinishedListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected List<Article> doInBackground(Void... params) {
		Rss rss = new Rss(this.context);
		List<Article> articles = null;
		try {
			List<Feed> feeds = rss.getFeeds();
			for (Feed feed : feeds) {
				articles = rss.fetchArticles(feed);
			}
		} catch (Exception e) {
			Log.d(Utils.LOG_TAG, "failed to read news", e);
			this.ex = e;
		} finally {
			rss.done();
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