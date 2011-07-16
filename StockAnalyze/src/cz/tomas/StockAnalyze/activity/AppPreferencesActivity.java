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
package cz.tomas.StockAnalyze.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.base.BasePreferenceActivity;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * General Application Prefernces activity accessible from home page
 * 
 * @author tomas
 * 
 */
public class AppPreferencesActivity extends BasePreferenceActivity {

	private SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.sharedPreferences = getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);
		
		addPreferencesFromResource(R.xml.preferences);
		// Get the preferences
		CheckBoxPreference updateNotifPref = (CheckBoxPreference) findPreference("prefUpdateNotification");
		CheckBoxPreference permanentNotifPref = (CheckBoxPreference) findPreference("prefPermanentNotification");
		CheckBoxPreference backgroundUpdatesPref = (CheckBoxPreference) findPreference("prefEnableBackgroundUpdate");
		ListPreference updateIntervalPreference = (ListPreference) findPreference("prefUpdateTimeInterval");
		CheckBoxPreference portfolioIncludeFeePref = (CheckBoxPreference) findPreference("prefPortfolioIncludeFee");
		
		updateNotifPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Utils.PREF_UPDATE_NOTIF, (Boolean) newValue);
				editor.commit();
				return true;
			}
		});
		permanentNotifPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Utils.PREF_PERMANENT_NOTIF, (Boolean) newValue);
				editor.commit();
				return true;
			}
		});
		
		backgroundUpdatesPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Utils.PREF_ENABLE_BACKGROUND_UPDATE, (Boolean) newValue);
				editor.commit();
				
				
				return true;
			}
		});
		
		updateIntervalPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = sharedPreferences.edit();
				editor.putInt(Utils.PREF_INTERVAL_BACKGROUND_UPDATE, Integer.parseInt((String) newValue));
				editor.commit();
				return true;
			}
		});
		
		portfolioIncludeFeePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Utils.PREF_PORTFOLIO_INCLUDE_FEE, (Boolean) newValue);
				editor.commit();
				return true;
			}
		});
	}
}
