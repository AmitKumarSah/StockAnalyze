package cz.tomas.StockAnalyze.fragments;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class ArticleFragment extends Fragment {

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_article_content, null);
		
		Bundle arguments = getArguments();
		if (arguments != null) {
			String title = arguments.getString(ArticlePagerAdapter.ARTICLE_TITLE);
			long date = arguments.getLong(ArticlePagerAdapter.ARTICLE_DATE);
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(date);
			String dateText = FormattingUtils.formatDate(cal);
			dateText += " " + cal.getTimeZone().getDisplayName(true, TimeZone.SHORT);
			String content = arguments.getString(ArticlePagerAdapter.ARTICLE_CONTENT);
			
			TextView txtContent = (TextView) view.findViewById(R.id.newsArticleContent);
			TextView txtDate = (TextView) view.findViewById(R.id.newsArticleDate);
			TextView txtTitle = (TextView) view.findViewById(R.id.newsArticleTitle);
			
			txtContent.setText(Html.fromHtml(content));
			txtDate.setText(dateText);
			txtTitle.setText(title);
		}
		return view;
	}	
}
