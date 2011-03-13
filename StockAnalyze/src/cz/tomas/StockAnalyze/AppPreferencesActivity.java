/**
 * 
 */
package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.utils.Utils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
	}
}
