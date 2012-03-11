package cz.tomas.StockAnalyze.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.tomas.StockAnalyze.News.ArticlePagerAdapter;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.utils.FormattingUtils;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class ArticleFragment extends Fragment {

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_article_text_content, null);
		
		Bundle args = getArguments();
		if (args != null) {
			final String title = args.getString(ArticlePagerAdapter.ARTICLE_TITLE);
			final long date = args.getLong(ArticlePagerAdapter.ARTICLE_DATE);
			final GregorianCalendar cal = new GregorianCalendar();

			cal.setTimeInMillis(date);
			String dateText = FormattingUtils.formatDate(cal);
			dateText += " " + cal.getTimeZone().getDisplayName(true, TimeZone.SHORT);
			final String content = args.containsKey(ArticlePagerAdapter.ARTICLE_CONTENT) ?
					args.getString(ArticlePagerAdapter.ARTICLE_CONTENT) :args.getString(ArticlePagerAdapter.ARTICLE_DESC) ;

			final TextView txtDate = (TextView) view.findViewById(R.id.newsArticleDate);
			final TextView txtTitle = (TextView) view.findViewById(R.id.newsArticleTitle);
			
			final TextView txtContent = (TextView) view.findViewById(R.id.newsArticleContent);
			final int padding = (int) (getResources().getDisplayMetrics().density * 9);
			txtContent.setPadding(padding, padding, padding, padding);
			
			if (content != null) {
				txtContent.setText(R.string.loading);
				AsyncTask<Void, Integer, Spanned> task = new AsyncTask<Void, Integer, Spanned>() {

					@Override
					protected Spanned doInBackground(Void... params) {
						return Html.fromHtml(content);
					}

					@Override
					protected void onPostExecute(Spanned result) {
						super.onPostExecute(result);
						txtContent.setText(result);
					}
					
				};
				task.execute((Void)null);
			}
			txtDate.setText(dateText);
			txtTitle.setText(title);
		}
		return view;
	}	
}
