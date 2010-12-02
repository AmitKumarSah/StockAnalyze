package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.home_layout);
		
		this.findViewById(R.id.homeBlockCurrencies).setOnClickListener(this);
		this.findViewById(R.id.homeBlockNews).setOnClickListener(this);
		this.findViewById(R.id.homeBlockPortfolio).setOnClickListener(this);
		this.findViewById(R.id.homeBlockStockList).setOnClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
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
				Toast.makeText(this, "Failed to start " + target == null ? "unkown" : target, Toast.LENGTH_SHORT).show();
			}
		}
		
	}

}
