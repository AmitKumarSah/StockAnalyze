package cz.tomas.StockAnalyze.News;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import cz.tomas.StockAnalyze.fragments.ArticleFragment;
import cz.tomas.StockAnalyze.fragments.WebArticleFragment;
import cz.tomas.StockAnalyze.utils.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class ArticlePagerAdapter extends FragmentPagerAdapter {

	public static final String ARTICLE_TITLE = "article-title";
	public static final String ARTICLE_DATE = "article-date";
	public static final String ARTICLE_CONTENT = "article-content";
	public static final String ARTICLE_URL = "article-url";
	
	//private Context context;
	private List<Article> articles;
	
	private final boolean useWebView;
	
	public ArticlePagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.articles = new ArrayList<Article>();
		useWebView = context.getSharedPreferences(Utils.PREF_NAME, 0)
				.getBoolean(Utils.PREF_FULL_ARTICLE, Utils.PREF_DEF_FULL_ARTICLE);
	}
	
	public void setData(Cursor c) {
		this.articles.clear();
		if (c != null && c.moveToFirst()) {
			try {
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
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to read article from cursor");
			}
		} else {
			Log.d(Utils.LOG_TAG, "no articles present");
		}
		this.notifyDataSetChanged();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		final Fragment fragment = useWebView ? new WebArticleFragment() : new ArticleFragment();
		
		Article article = this.articles.get(position);
		if (article != null) {
			final Bundle bundle = new Bundle(3);
			bundle.putString(ARTICLE_TITLE, article.getTitle());
			bundle.putLong(ARTICLE_DATE, article.getDate());

			if (article.getContent() != null) {
				bundle.putString(ARTICLE_CONTENT, article.getContent());
			} else if (! useWebView) {
				bundle.putString(ARTICLE_CONTENT, article.getDescription());
			}
			bundle.putString(ARTICLE_URL, article.getMobilizedUrl());
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
}
