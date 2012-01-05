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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.News.NewsSqlHelper.ArticleColumns;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public class NewsListAdapter extends SimpleCursorAdapter {

	private Calendar cal = new GregorianCalendar(Utils.PRAGUE_TIME_ZONE);
	
	/**
	 * @param context
	 */
	public NewsListAdapter(Context context, Cursor cursor) {
		super(context, R.layout.news_list_item, cursor, new String[] {ArticleColumns.TITLE, ArticleColumns.DATE, ArticleColumns.DESCRIPTION},
				new int[] {R.id.txtNewsItemTitle, R.id.txtNewsItemBottomInfo, R.id.txtNewsItemContentPreview }, 0);
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
}
