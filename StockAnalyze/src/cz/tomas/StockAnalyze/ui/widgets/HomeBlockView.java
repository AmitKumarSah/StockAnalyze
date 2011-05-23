/**
 * 
 */
package cz.tomas.StockAnalyze.ui.widgets;

import cz.tomas.StockAnalyze.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author tomas
 *
 */
public class HomeBlockView extends RelativeLayout {

	String target;

	private LayoutInflater inflater = null;
	
	/*
	 * get target to invoke from this block
	 */
	public String getTarget() {
		return target;
	}


	public HomeBlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (this.inflater == null)
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.inflater.inflate(R.layout.home_block_item, this);
        
        try {
			this.init(context, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("HomeBlockView", "Failed to init Block item");
		}
	}

	/*
	 * read attributes from xml layout file for this view
	 */
	private void init(Context context, AttributeSet attrs) {
		TextView textView = (TextView) this.findViewById(R.id.homeBlockItemTextView);
		ImageView image = (ImageView) this
				.findViewById(R.id.homeBlockItemImage);

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.HomeBlockItemAtts);

		if (textView != null)
			textView.setText(a.getResourceId(2, R.string.hello));
		if (image != null)
			image.setImageResource(a.getResourceId(3, R.drawable.ic_launcher));
		this.target = a.getString(4);
		a.recycle();
	}
}
