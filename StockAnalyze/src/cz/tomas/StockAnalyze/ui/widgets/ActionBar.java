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
package cz.tomas.StockAnalyze.ui.widgets;

import java.util.Calendar;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.activity.HomeActivity;
import cz.tomas.StockAnalyze.activity.StockSearchActivity;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActionBar extends RelativeLayout {

	/*
	 * data manager to get last update date;
	 */
	private DataManager dataManager;
	
	/*
	 * text under title - display last update time here
	 */
	private TextView subtitleView; 
	private static String lastUpdateText;
	
	public ActionBar(final Context context, AttributeSet attrs) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.actionbar_layout, this);
        
        this.subtitleView = (TextView) this.findViewById(R.id.actionSubTitle);
        if (this.subtitleView != null && lastUpdateText != null)
        	this.subtitleView.setText(lastUpdateText);
		if (! this.isInEditMode())
			initUpdateListener(context);
		
        View searchButton = this.findViewById(R.id.actionSearchButton);
        if (searchButton != null) {
        	searchButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(getContext(), StockSearchActivity.class);
					getContext().startActivity(intent);
				}
			});
        }
        else
        	Log.d(Utils.LOG_TAG, "action bar search button not found");
	
        View homeButton = this.findViewById(R.id.actionHomeButton);

        if (homeButton != null) {        
            // don't show home button on home screen
            if (this.getParent() != null && this.getParent() instanceof HomeActivity) {
            	homeButton.setVisibility(View.GONE);
            	View separator1 = this.findViewById(R.id.actionbar_sep1);
            	separator1.setVisibility(View.GONE);
            }
	        homeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(getContext(), HomeActivity.class);
					getContext().startActivity(intent);
				}
			});
        }
        else
        	Log.d(Utils.LOG_TAG, "action bar home button not found");
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ActionBarAttrs);
        
        TextView titleView = (TextView) this.findViewById(R.id.actionTitle);
        
        if (a != null && titleView != null && a.hasValue(0)) {
        	String title = a.getString(0);
        	titleView.setText(title);
        }
	}

	/* (non-Javadoc)
	 * @see android.view.View#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		//Log.d(Utils.LOG_TAG, "cleaning action bar");
		if (this.subtitleView != null)
			this.subtitleView.setText(null);
		this.dataManager.removeUpdateChangedListener(this.listener);
		super.onDetachedFromWindow();
	}



	/**
	 * get last update time and register listener for updates 
	 * @param context
	 */
	private void initUpdateListener(final Context context) {
		if (lastUpdateText == null) {
			SharedPreferences preferences = context.getSharedPreferences(Utils.PREF_NAME, 0);
			long time = preferences.getLong(Utils.PREF_LAST_UPDATE_TIME, -1);
			if (time > 0 && this.subtitleView != null) {
				final Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
				cal.setTimeInMillis(time);
				lastUpdateText = FormattingUtils.formatStockDate(cal);
				subtitleView.setText(lastUpdateText);
			}
		}
		this.dataManager = DataManager.getInstance(context);
		this.dataManager.addUpdateChangedListener(listener);
	}
	
	/**
	 * listener to update date change :
	 * will write new update time in listener 
	 */
	IUpdateDateChangedListener listener = new IUpdateDateChangedListener() {
		
		@Override
		public void OnLastUpdateDateChanged(long updateTime) {
			if (subtitleView != null) {
				final Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
				cal.setTimeInMillis(updateTime);
				// this event may come from different thread
				if (getContext() instanceof Activity)
					((Activity) getContext()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							lastUpdateText = FormattingUtils.formatStockDate(cal);
							subtitleView.setText(lastUpdateText);
						}
					});
			} else 
				Log.d(Utils.LOG_TAG, "Can not set action bar sub title");
		}
	};
}
