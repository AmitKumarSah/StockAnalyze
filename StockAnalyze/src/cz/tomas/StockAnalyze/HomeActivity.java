package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import cz.tomas.StockAnalyze.utils.DownloadService;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
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
		
//		ImageView chart = (ImageView) this.findViewById(R.id.home_chart);
//		
//		String downloadUrl = "http://www.pse.cz/generated/a_indexy/X1_R.GIF";
//		byte[] chartArray = null;
//		try {
//			chartArray = DownloadService.GetInstance().DownloadFromUrl(downloadUrl);
//			
//			Bitmap bmp = BitmapFactory.decodeByteArray(chartArray, 0, chartArray.length);
//			//chart.setImageURI(Uri.parse("http://www.pse.cz/generated/a_indexy/X1_R.GIF"));
//			chart.setImageBitmap(bmp);
//			chart.setBackgroundDrawable(null);
//			chart.setMinimumHeight(100);
//			chart.setMinimumWidth(100);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ChartUpdateTask task = new ChartUpdateTask();
		task.execute((Void[])null);
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

	final class ChartUpdateTask extends AsyncTask<Void, Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(Void... params) {
			String downloadUrl = "http://www.pse.cz/generated/a_indexy/X1_R.GIF";
			byte[] chartArray = null;
			Bitmap bmp = null;
			try {
				chartArray = DownloadService.GetInstance().DownloadFromUrl(downloadUrl, false);
				
				bmp = BitmapFactory.decodeByteArray(chartArray, 0, chartArray.length);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView chart = (ImageView) findViewById(R.id.home_chart);
			chart.setImageBitmap(result);
		}
		
	}
}
