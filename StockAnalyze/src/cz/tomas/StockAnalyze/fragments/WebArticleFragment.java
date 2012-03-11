package cz.tomas.StockAnalyze.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class WebArticleFragment extends Fragment {

	private WebView webContent;

	/* (non-Javadoc)
			 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
			 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_article_content, null);
		
		Bundle arguments = getArguments();
		if (arguments != null) {
			final String title = arguments.getString(ArticlePagerAdapter.ARTICLE_TITLE);
			final long date = arguments.getLong(ArticlePagerAdapter.ARTICLE_DATE);
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(date);
			String dateText = String.format("%s %s", FormattingUtils.formatDate(cal), cal.getTimeZone().getDisplayName(true, TimeZone.SHORT));

			final TextView txtDate = (TextView) view.findViewById(R.id.newsArticleDate);
			final TextView txtTitle = (TextView) view.findViewById(R.id.newsArticleTitle);
			final View progress = view.findViewById(R.id.newsProgress);

			webContent = (WebView) view.findViewById(R.id.newsArticleContent);
			webContent.getSettings().setLoadsImagesAutomatically(true);
			webContent.setScrollContainer(false);
			webContent.setFocusableInTouchMode(false);
			webContent.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "loaded url " + url);
					progress.setVisibility(View.GONE);
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					progress.setVisibility(View.VISIBLE);
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			            if (url.equals(articleUrl)) {
//							view.loadUrl(url);
//							return true;
//						}
					return false;
				}
			});
			webContent.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View view, int i, KeyEvent keyEvent) {
					if (i == KeyEvent.KEYCODE_BACK && webContent.canGoBack()) {
						webContent.goBack();
						return true;
					}
					return false;
				}
			});
			txtDate.setText(dateText);
			txtTitle.setText(title);
			txtTitle.requestFocus();
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Bundle arguments = getArguments();
		final String content = arguments.getString(ArticlePagerAdapter.ARTICLE_CONTENT);
		if (content != null) {
			webContent.loadDataWithBaseURL(Article.BASE_URL, content, "text/html", "utf-8", null);
		} else {
			final String articleUrl = arguments.getString(ArticlePagerAdapter.ARTICLE_URL);
//				final String perex = arguments.getString(ArticlePagerAdapter.ARTICLE_DESC);
//				webContent.loadDataWithBaseURL(Article.BASE_URL, perex, "text/html", "utf-8", null);
			webContent.loadUrl(articleUrl);
		}
	}
}
