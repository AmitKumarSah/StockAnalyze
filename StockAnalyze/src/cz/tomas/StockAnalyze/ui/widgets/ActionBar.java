package cz.tomas.StockAnalyze.ui.widgets;

import cz.tomas.StockAnalyze.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ActionBar extends LinearLayout {

	public ActionBar(Context context, AttributeSet attr) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.actionbar_layout, this);
        
        View homeButton = this.findViewById(R.id.actionHomeView);
        
        if (homeButton != null)
	        homeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
        else
        	Log.d("ActionBar", "action bar home button not found");
	}
	
	private void init(Context context, AttributeSet attrs) {
	
	}
}
