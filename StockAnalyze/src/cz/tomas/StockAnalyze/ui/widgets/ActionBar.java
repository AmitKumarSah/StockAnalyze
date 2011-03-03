package cz.tomas.StockAnalyze.ui.widgets;

import cz.tomas.StockAnalyze.HomeActivity;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockSearchActivity;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ActionBar extends RelativeLayout {

	public ActionBar(Context context, AttributeSet attr) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.actionbar_layout, this);
        
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
	}
}
