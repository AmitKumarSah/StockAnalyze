package cz.tomas.StockAnalyze.News;

import java.util.ArrayList;
import java.util.List;

import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.fragments.ArticleFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public final class ArticlePagerAdapter extends FragmentPagerAdapter {

	public static final String ARTICLE_TITLE = "article-title";
	public static final String ARTICLE_DATE = "article-date";
	public static final String ARTICLE_CONTENT = "article-content";
	
	//private Context context;
	private List<Article> articles;
	
	public ArticlePagerAdapter(Context context, FragmentManager fm, ITaskListener listener) {
		super(fm);
		//this.context = context;
		this.articles = new ArrayList<Article>();
		
		NewsTask task = new NewsTask(new Rss(context), context);
		task.setListener(listener);
		task.execute(false);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		ArticleFragment fragment = new ArticleFragment();
		
		Article article = this.articles.get(position);
		if (article != null) {
			Bundle bundle = new Bundle(3);
			bundle.putString(ARTICLE_TITLE, article.getTitle());
			bundle.putLong(ARTICLE_DATE, article.getDate());
			if (article.getContent() != null) {
				bundle.putString(ARTICLE_CONTENT, article.getContent());
			} else {
				bundle.putString(ARTICLE_CONTENT, article.getDescription());
			}
			fragment.setArguments(bundle);
		}
		return fragment;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		if (articles == null) {
			return 0;
		}
		return articles.size();
	}	
	
	public Article getArticle(int position) {
		return this.articles.get(position);
	}
	
	class NewsTask extends NewsItemsTask {

		NewsTask(Rss rss, Context context) {
			super(rss, context);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<Article> result) {
			super.onPostExecute(result);
			
			articles = result;
			notifyDataSetChanged();
			if (this.listener != null) {
				this.listener.onUpdateFinished();
			}
		}
		
	}
}