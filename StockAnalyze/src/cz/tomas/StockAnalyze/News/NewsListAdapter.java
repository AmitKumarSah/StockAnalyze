/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import cz.tomas.StockAnalyze.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author tomas
 *
 */
public class NewsListAdapter extends ArrayAdapter<Article> {

	List<Article> newsItems;
	
	LayoutInflater vi; 
	NewsItemsTask task;
	private final int MAX_DESCRIPTION_LENGHT = 100; 
	
	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public NewsListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

		this.vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.newsItems = new ArrayList<Article>();

		getNewsData();
	}

	private void getNewsData() {
		task = new NewsItemsTask();
		task.execute();
		task = null;
	}
	
	/*
	 * fetch new data for rss feeds
	 */
	public void refresh() {
		this.getNewsData();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = vi.inflate(R.layout.news_list_item, null);
		}
		
		if (this.getCount() > position) {
			TextView txtTitle = (TextView) v.findViewById(R.id.txtNewsItemTitle);
			TextView txtPreview = (TextView) v.findViewById(R.id.txtNewsItemContentPreview);
			TextView txtInfo = (TextView) v.findViewById(R.id.txtNewsItemBottomInfo);
			
			Article article = this.getItem(position);
			if (article != null) {
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", article.toString());
				if (txtTitle != null)
					txtTitle.setText(article.getTitle());
				else
					Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "can't set title text - TextView is null");	
				if (txtPreview != null) {
					String description = article.getEscapedDescription();
					if (description != null) {
						if (description.length() > MAX_DESCRIPTION_LENGHT) {
							description = description.substring(0,
									MAX_DESCRIPTION_LENGHT);
							description += "...";
						}
						txtPreview.setText(description);
					}
				}
				if (txtInfo != null) {
					Calendar cal = Calendar.getInstance();
					long date = article.getDate();
					cal.setTimeInMillis(date);
					java.text.DateFormat frm = SimpleDateFormat.getDateTimeInstance();
					txtInfo.setText(frm.format(cal.getTime()));
				}
			}
		}
		
		return v;
	}
	
//	@Override
//	public int getCount() {
//		return this.newsItems.size();
//	}
	
	
	private class NewsItemsTask extends AsyncTask<Void, Integer, List<Article>> {
		View view;
		Exception ex;
		
		@Override
		protected void onPreExecute() {
			try {
				((Activity) getContext()).findViewById(R.id.progressNews).setVisibility(View.VISIBLE);
			} catch (Exception e) {
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "failed to dissmis progess bar! " + e.getMessage());
			}
		}
		
		@Override
		protected List<Article> doInBackground(Void... params) {
			List<Article> newsItems = new ArrayList<Article>();

			Rss rss = new Rss(getContext());
			
			try {
				List<Feed> feeds = rss.getFeeds();
				for (Feed feed : feeds) {
					rss.deleteArticles(feed.getFeedId());
					rss.fetchArticles(feed);
					List<Article> articles = rss.getArticles(feed.getFeedId());
					
					for (Article a : articles) {
						newsItems.add(a);
					}
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null)
					message = "Failed to get news articles!";
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", message);
				e.printStackTrace();
				this.ex = e;
			} finally {
				rss.done();
				//news.close();
			}
		
			return newsItems;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}

		protected void onPostExecute(List<Article> result) {
			//newsItems.addAll(result);
			for (int i = 0; i < result.size(); i++) {
				add(result.get(i));
				notifyDataSetChanged();
			}
			
			if (result.size() == 0) {
				String message = "";
				
				if (this.ex != null && this.ex.getMessage() != null) {
					message = getContext().getString(R.string.FailedGetNews);
					message += (": " + this.ex.getMessage());
				}
				else
					message = getContext().getString(R.string.NoNews);
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
			try {
				((Activity) getContext()).findViewById(R.id.progressNews).setVisibility(View.GONE);
			} catch (Exception e) {
				Log.d("cz.tomas.StockAnalyze.News.NewsListAdapter", "failed to dissmis progess bar! " + e.getMessage());
			}
			//((Activity) getContext()).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 10);
		}
	}
}
