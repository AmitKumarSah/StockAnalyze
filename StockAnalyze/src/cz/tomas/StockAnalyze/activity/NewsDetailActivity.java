package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskFinishedListener;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;

public final class NewsDetailActivity extends BaseFragmentActivity implements OnPageChangeListener {

	public static final String STATE_CURRENT_POSITION = "cz.tomas.StockAnalyze.activity.State_current_position";
	
	private ViewPager pager;
	private ArticlePagerAdapter adapter;
	private Article currentArticle;
	private int currentPosition;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.news_detail_layout);
		
		Bundle extras = getIntent().getExtras();
		final int initialPosition = extras != null ? extras.getInt(NewsActivity.EXTRA_NEWS_POSITION) : 0;
		final boolean restorePosition = savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_POSITION);
		final int position =  restorePosition ? savedInstanceState.getInt(STATE_CURRENT_POSITION) : initialPosition;
		
		this.pager = (ViewPager) this.findViewById(R.id.newsArticleViewPager);
		
		this.adapter = new ArticlePagerAdapter(this, getSupportFragmentManager(), new ITaskFinishedListener() {

			@Override
			public void onUpdateFinished() {
				pager.setCurrentItem(position);
			}
		});
		this.pager.setAdapter(adapter);
		this.pager.setOnPageChangeListener(this);
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (this.currentArticle != null) {
			//int position = this.adapter.getPositionByArticleId(this.currentArticle.getArticleId();
			outState.putInt(STATE_CURRENT_POSITION, this.currentPosition);
			
			//outState.putLong(STATE_CURRENT_ARTICLE_ID, this.currentArticle.getArticleId();
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		this.currentPosition = position;
	}
}
