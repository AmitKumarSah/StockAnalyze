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
import cz.tomas.StockAnalyze.R.layout;
import cz.tomas.StockAnalyze.utils.Utils;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * @author tomas
 *
 */
public class AboutActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.about_layout);
		
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);

			TextView versionText = (TextView) this.findViewById(R.id.aboutVersion);
			String version = info.versionName;
			versionText.setText(version);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to retrieve appliation version", e);
		}
	}
	
	
}
