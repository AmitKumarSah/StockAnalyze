package cz.tomas.StockAnalyze.fragments;

import java.text.DateFormat;
import java.util.Date;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
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
			String dateText = DateFormat.getDateTimeInstance().format(new Date(date));
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
