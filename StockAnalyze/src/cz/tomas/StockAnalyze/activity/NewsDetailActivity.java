package cz.tomas.StockAnalyze.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.ui.widgets.CirclesView;
import cz.tomas.StockAnalyze.utils.Utils;

public final class NewsDetailActivity extends BaseFragmentActivity implements OnPageChangeListener {

	public static final String STATE_CURRENT_POSITION = "cz.tomas.StockAnalyze.activity.State_current_position";
	
	private ViewPager pager;
	private ArticlePagerAdapter adapter;
	private int currentPosition;
	
	private CirclesView circlesView;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.news_detail_layout);
		findViewById(R.id.progressNews).setVisibility(View.VISIBLE);
		
		final Bundle extras = getIntent().getExtras();
		final int initialPosition = extras != null ? extras.getInt(NewsActivity.EXTRA_NEWS_POSITION) : 0;
		final boolean restorePosition = savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_POSITION);
		final int position =  (int) (restorePosition ? savedInstanceState.getLong(STATE_CURRENT_POSITION) : initialPosition);

		this.circlesView = (CirclesView) this.findViewById(R.id.newsCirclesView);
		this.pager = (ViewPager) this.findViewById(R.id.newsArticleViewPager);
		
		this.adapter = new ArticlePagerAdapter(this, getSupportFragmentManager(), new ITaskListener() {

			@Override
			public void onUpdateFinished() {

				try {
					findViewById(R.id.progressNews).setVisibility(View.GONE);
				} catch (Exception e) {
					Log.d(Utils.LOG_TAG, "failed to dissmis progess bar! " + e.getMessage());
				}
				int count = pager.getAdapter().getCount();
				circlesView.setCircles(count);
				pager.setCurrentItem(position);
				// event isn't triggered if position is 0
				if (position == 0 && count > 0) {
					onPageSelected(position);
				}
			}

			@Override
			public void onUpdateStart() {
			}
		});
		this.pager.setAdapter(adapter);
		this.pager.setOnPageChangeListener(this);
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_POSITION, this.currentPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.news_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_share) {
			Article article = adapter.getArticle(this.currentPosition);
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
			intent.putExtra(Intent.EXTRA_TEXT, article.getUrl().toString());
			startActivity(Intent.createChooser(intent, getText(R.string.share)));
		} else if (item.getItemId() == R.id.menu_browser) {
			Article article = adapter.getArticle(this.currentPosition);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(article.getUrl().toString()));
			startActivity(Intent.createChooser(intent, getText(R.string.share)));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		this.currentPosition = position;
		this.circlesView.setSelected(position);
	}
}
