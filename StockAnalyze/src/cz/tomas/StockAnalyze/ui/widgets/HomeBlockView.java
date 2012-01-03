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
package cz.tomas.StockAnalyze.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;

/**
 * Clickable view with icon and text. See {@link R.styleable#HomeBlockItemAtts} for attributes.
 * 
 * @author tomas
 *
 */
public class HomeBlockView extends RelativeLayout {

	private static final String NAMESPACE = "http://schemas.android.com/apk/res/cz.tomas.StockAnalyze";

	String target;

	private LayoutInflater inflater = null;
	
	/**
	 * get target activity class to invoke from this block
	 */
	public String getTarget() {
		return target;
	}


	public HomeBlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (this.inflater == null) {
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
        this.inflater.inflate(R.layout.home_block_item, this);
        
        try {
			this.init(context, attrs);
		} catch (Exception e) {
			Log.w("HomeBlockView", "Failed to init Block item", e);
		}
	}

	/**
	 * read attributes from xml layout file for this view
	 */
	private void init(Context context, AttributeSet attrs) {
		final TextView textView = (TextView) this.findViewById(R.id.homeBlockItemTextView);
		final ImageView image = (ImageView) this.findViewById(R.id.homeBlockItemImage);
		
		if (textView != null) {
			textView.setText(attrs.getAttributeResourceValue(NAMESPACE, "textId", R.string.app_name));
		}
		if (image != null) {
			image.setImageResource(attrs.getAttributeResourceValue(NAMESPACE, "imageId", 0));
		}
		this.target = attrs.getAttributeValue(NAMESPACE, "target");
	}
}
