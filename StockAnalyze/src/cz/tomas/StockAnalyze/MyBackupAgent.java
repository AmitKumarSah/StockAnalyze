package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.utils.Utils;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public final class MyBackupAgent extends BackupAgentHelper {

    static final String PREFS_BACKUP_KEY = "prefs";
    
	@Override
    public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Utils.PREF_NAME);
		addHelper(PREFS_BACKUP_KEY, helper);
    }
}
