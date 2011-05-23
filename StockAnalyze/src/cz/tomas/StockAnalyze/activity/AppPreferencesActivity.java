/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.activity;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.R.xml;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * General Application Prefernces activity accessible from home page
 * 
 * @author tomas
 * 
 */
public class AppPreferencesActivity extends PreferenceActivity {

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
