package cz.tomas.StockAnalyze;

import java.util.Iterator;

import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnClickListener, OnKeyListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.home_layout);
		
		View[] blockViews = new View[4];
		blockViews[0] = this.findViewById(R.id.homeBlockCurrencies);
		blockViews[1] = this.findViewById(R.id.homeBlockNews);
		blockViews[2] = this.findViewById(R.id.homeBlockPortfolio);
		blockViews[3] = this.findViewById(R.id.homeBlockStockList);
		
		for (View view : blockViews) {
			if (view != null) {
				view.setOnClickListener(this);
				view.setOnKeyListener(this);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		startChildActivity(v);
	}


	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		startChildActivity(v);
		return true;
	}
	
	/**
	 * @param v
	 */
	private void startChildActivity(View v) {
		String target = null;
		if (v instanceof HomeBlockView) {
			try {
				target = ((HomeBlockView) v).getTarget();
				
				if (target != null) {
					Intent intent = new Intent();
					intent.setClassName(this, target);
					startActivity(intent);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "Failed to start:\n" + (target == null ? "unkown" : target), Toast.LENGTH_SHORT).show();
			}
		}
	}

}
