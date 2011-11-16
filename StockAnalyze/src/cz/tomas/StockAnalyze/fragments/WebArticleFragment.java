package cz.tomas.StockAnalyze.fragments;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.News.Rss;
import cz.tomas.StockAnalyze.utils.FormattingUtils;

public final class WebArticleFragment extends Fragment {

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
			String dateText = FormattingUtils.formatDate(cal);
			dateText += " " + cal.getTimeZone().getDisplayName(true, TimeZone.SHORT);
			final String content = arguments.getString(ArticlePagerAdapter.ARTICLE_CONTENT);
			final String articleUrl = arguments.getString(ArticlePagerAdapter.ARTICLE_URL);
			
			final FrameLayout articleContainer = (FrameLayout) view.findViewById(R.id.newsArticleContentContainer);
			final TextView txtDate = (TextView) view.findViewById(R.id.newsArticleDate);
			final TextView txtTitle = (TextView) view.findViewById(R.id.newsArticleTitle);
			
			final WebView webContent = new WebView(view.getContext());
			webContent.getSettings().setLoadsImagesAutomatically(true);
			webContent.setScrollContainer(false);
			webContent.setFocusableInTouchMode(false);
			webContent.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			articleContainer.addView(webContent);
			
			if (content != null) {
				webContent.loadDataWithBaseURL(Rss.BASE_URL,content, "text/html", "utf-8", null);
			} else {
				webContent.loadUrl(articleUrl);
				webContent.setWebViewClient(new WebViewClient() {
			        @Override
			        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			        }

			        @Override
			        public boolean shouldOverrideUrlLoading(WebView view, String url) {
			            if (url.equals(articleUrl)) {
							view.loadUrl(url);
							return true;
						}
			            return false;
			        }
			    });
			}
			txtDate.setText(dateText);
			txtTitle.setText(title);
			txtTitle.requestFocus();
		}
		return view;
	}	
}
