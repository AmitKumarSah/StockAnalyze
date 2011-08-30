/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.utils.Utils;

import android.content.Context;
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

	//private List<Article> newsItems;
	
	private LayoutInflater vi; 
	private NewsItemsTask task;
	private ITaskListener listener;
	private final int MAX_DESCRIPTION_LENGHT = 100;
	
	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public NewsListAdapter(Context context, int textViewResourceId, ITaskListener listener) {
		super(context, textViewResourceId);

		this.listener = listener;
		this.vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.getNewsData(false);
	}

	private void getNewsData(boolean download) {
		task = new NewsAdapterTask(this.getContext());
		task.listener = this.listener;
		task.execute(download);
		Log.d(Utils.LOG_TAG, "loading news data, fetch " + download);
	}
	
	/**
	 * fetch new data for rss feeds
	 */
	public void refresh() {
		this.getNewsData(true);
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
				//Log.d(Utils.LOG_TAG, article.toString());
				if (txtTitle != null)
					txtTitle.setText(article.getTitle());
				else
					Log.d(Utils.LOG_TAG, "can't set title text - TextView is null");	
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
	
	class NewsAdapterTask extends NewsItemsTask {

		NewsAdapterTask(Context context) {
			super(context);
		}

		protected void onPostExecute(List<Article> result) {
			//newsItems = result;
			clear();
			for (int i = 0; i < result.size() && i < DEFAULT_NEWS_LIMIT; i++) {
				add(result.get(i));
			}
			notifyDataSetChanged();
			
			if (result.size() == 0) {
				String message = "";
				
				if (this.ex != null && this.ex.getMessage() != null) {
					message = this.context.getString(R.string.FailedGetNews);
					message += (": " + this.ex.getMessage());
				}
				else
					message = this.context.getString(R.string.NoNews);
				Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
			}
			if (this.listener != null) {
				this.listener.onUpdateFinished();
			}
		}
	}
}
