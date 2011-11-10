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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.NewsItemsTask.ITaskListener;
import cz.tomas.StockAnalyze.News.NewsSqlHelper.ArticleColumns;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public class NewsListAdapter extends SimpleCursorAdapter {

	//private LayoutInflater vi; 
	private NewsItemsTask task;
	private ITaskListener listener;
	//private final int MAX_DESCRIPTION_LENGHT = 100;
	private Rss rss;

	private Calendar cal = new GregorianCalendar(Utils.PRAGUE_TIME_ZONE);
	
	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public NewsListAdapter(Context context, ITaskListener listener) {
		super(context, R.layout.news_list_item, null, new String[] {ArticleColumns.TITLE, ArticleColumns.DATE, ArticleColumns.DESCRIPTION},
				new int[] {R.id.txtNewsItemTitle, R.id.txtNewsItemBottomInfo, R.id.txtNewsItemContentPreview }, 0);

		this.listener = listener;
		//this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.rss = new Rss(context);
		CursorTask task = new CursorTask();
		task.execute((Void)null);
	}
	
	/**
	 * fetch new data for rss feeds
	 */
	public void refresh() {
		task = new NewsAdapterTask(this.rss, this.mContext);
		task.listener = this.listener;
		task.execute(true);
		Log.d(Utils.LOG_TAG, "loading news data");
	}
	
	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		super.bindView(view, arg1, cursor);
		TextView txtDate = (TextView) view.findViewById(R.id.txtNewsItemBottomInfo);
		View readMark = view.findViewById(R.id.viewReadMark);
		//String desc = cursor.getString(cursor.getColumnIndex(ArticleColumns.DESCRIPTION));
		long date = cursor.getLong(cursor.getColumnIndex(ArticleColumns.DATE));
		boolean read = cursor.getInt(cursor.getColumnIndex(ArticleColumns.READ)) > 0;
		
		if (read) {
			readMark.setBackgroundDrawable(null);
		}
		cal.setTimeInMillis(date);
		String dateText = FormattingUtils.formatDate(cal);
		txtDate.setText(dateText);
	}



	class CursorTask extends AsyncTask<Void, Integer, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			Cursor c = rss.getAllArticlesCursor();
			return c;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			swapCursor(result);
		}
	}
	
	class NewsAdapterTask extends NewsItemsTask {

		NewsAdapterTask(Rss rss, Context context) {
			super(rss, context);
		}

		@SuppressWarnings("unchecked")
		protected void onPostExecute(List<Article> result) {
			if (result != null) {
//				clear();
//				for (int i = 0; i < result.size() && i < DEFAULT_NEWS_LIMIT; i++) {
//					add(result.get(i));
//				}
//				notifyDataSetChanged();
				if (result.size() == 0) {
					String message = "";

					if (this.ex != null && this.ex.getMessage() != null) {
						message = this.context.getString(R.string.FailedGetNews);
						message += (": " + this.ex.getMessage());
					} else {
						message = this.context.getString(R.string.NoNews);
					}
					Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
				}
				final FetchContentTask task = new FetchContentTask(this.rss);
				task.execute(result);
			}
			if (this.listener != null) {
				this.listener.onUpdateFinished();
			}
			final CursorTask task = new CursorTask();
			task.execute((Void)null);
		}
	}
	
	private class FetchContentTask extends AsyncTask<List<Article>, Integer, Void> {

		private final Rss rss;
		public FetchContentTask(Rss rss) {
			this.rss = rss;
		}

		@Override
		protected Void doInBackground(List<Article>... params) {
			if (params == null || params.length != 1) {
				return null;
			}
			
			this.rss.downloadContent(params[0]);
			return null;
		}
	}
}
