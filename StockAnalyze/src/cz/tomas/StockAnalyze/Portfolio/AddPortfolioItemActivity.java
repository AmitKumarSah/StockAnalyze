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
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.PortfolioActivity;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class AddPortfolioItemActivity extends Activity {

	private DataManager dataManager = null;
	private TextView totalValueView = null;
	private TextView feeView = null;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.dataManager = (DataManager) this.getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.portfolio_add_item_layout);
		this.setProgressBarVisibility(false);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.hasExtra(Utils.EXTRA_STOCK_ITEM) && intent.hasExtra(Utils.EXTRA_MARKET_ID)) {
			final Market market = (Market) intent.getExtras().getSerializable(Utils.EXTRA_MARKET_ID);
			final StockItem stockItem = intent.getExtras().getParcelable(Utils.EXTRA_STOCK_ITEM);
			final DayData data = intent.getExtras().getParcelable(Utils.EXTRA_DAY_DATA);
			
			this.totalValueView = (TextView) this.findViewById(R.id.portfolioAddTotalValue);
			this.feeView = (TextView) this.findViewById(R.id.portfolioAddDealFee);
			final TextView tickerView = (TextView) this.findViewById(R.id.portfolioAddTicker);
			final TextView marketView = (TextView) this.findViewById(R.id.portfolioAddMarket);
			final TextView priceView = (TextView) this.findViewById(R.id.portfolioAddPrice);
			final TextView countView = (TextView) this.findViewById(R.id.portfolioAddCount);
			final Button addButton = (Button) this.findViewById(R.id.portfolioAddButton);
			final Spinner dealSpinner = (Spinner) this.findViewById(R.id.portfolioAddSpinnerDeal);
			if (priceView != null)
				priceView.setText(String.valueOf(data.getPrice()));
			
			if (marketView != null && market != null)
				marketView.setText(market.getName());
			if (stockItem != null) {
				if (tickerView != null)
					tickerView.setText(stockItem.getTicker());
			}

			if (countView != null) {
				countView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (! hasFocus)
							try {
								final float price = Float.parseFloat(priceView.getText().toString());
								final int count = Integer.parseInt(countView.getText().toString());

								updateFeeAndValue(price, count);
							} catch (Exception e) {
								Log.e(Utils.LOG_TAG, "failed to set fee text", e);
							}
					}
				});
			}
			if (priceView != null) {
				priceView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (! hasFocus) {
							try {
								final int count = Integer.parseInt(countView.getText().toString());
								final float price = Float.parseFloat(priceView.getText().toString());
								
								updateFeeAndValue(price, count);
							} catch (Exception e) {
								Log.e(Utils.LOG_TAG, "failed to set total value", e);
							}
						}
					}
				});
			}
			if (addButton != null)
				addButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						addButton.setEnabled(false);
						// check if all fields are filled
						if (priceView.getText() == null || countView.getText() == null ||
								marketView.getText() == null || tickerView.getText() == null)
							Toast.makeText(AddPortfolioItemActivity.this, R.string.portfolioValidationMessage, Toast.LENGTH_SHORT).show();
						else if (market != null && stockItem != null) {
							try {
								addButton.setEnabled(false);
								final int count = Integer.parseInt(countView.getText().toString());
								float price = Float.parseFloat(priceView.getText().toString());
								if (((String) dealSpinner.getSelectedItem()).equalsIgnoreCase("sell"))
									price = -price;
								final float finalPrice = price; 
								final float fee = calculateFee(price, count);

								setProgressBarVisibility(true);
								AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
									private Exception ex;
									@Override
									protected Void doInBackground(Void... params) {
										try {
											addPortfolioItem(stockItem.getId(), count, finalPrice, "default", market.getId(), fee);
										} catch (SQLException e) {
											ex = e;
										}
										return null;
									}

									@Override
									protected void onPostExecute(Void result) {
										setProgressBarVisibility(false);
										if (ex != null) {
											String message = getText(R.string.portfolioFailedToAdd).toString();
											if (ex.getMessage() != null)
												message += ex.getMessage();
											
											Toast.makeText(AddPortfolioItemActivity.this, message, Toast.LENGTH_LONG).show();
										} else {
											Intent intent = new Intent(AddPortfolioItemActivity.this, PortfolioActivity.class);
											intent.putExtra("refresh", true);
											AddPortfolioItemActivity.this.startActivity(intent);
										}
										super.onPostExecute(result);
									}
								};
								task.execute((Void) null);
							} catch (NumberFormatException e) {
								Log.e(Utils.LOG_TAG, "failed to parse data from add portfolio layout", e);
							}
						}
						addButton.setEnabled(true);
						return;
					}
				});
			try {
				final float price = Float.parseFloat(priceView.getText().toString());
				final int count = Integer.parseInt(countView.getText().toString());
				updateFeeAndValue(price, count);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to fill values", e);
			}
		}
	}

	protected void updateFeeAndValue(final float price, final int count) {
		final float fee = calculateFee(price, count);
		
		final float value = price * count + fee;
		
		try {
			this.feeView.setText(String.valueOf(fee));
			this.totalValueView.setText(FormattingUtils.getPriceFormatCzk().format(value));
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to set total fee and value", e);
		}
	}

	private void addPortfolioItem(String stockId, int count, float price, String portfolioName, String marketId, float fee) throws SQLException {
		Portfolio portfolio = new Portfolio(this);
		
		// construct portfolio item and pass it to Portfolio
		PortfolioItem item = new PortfolioItem(stockId, portfolioName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		if (count > 0)
			item.setBuyFee(fee);
		else
			item.setSellFee(fee);

		try {
			dataManager.acquireDb(this.getClass().getName());
			portfolio.addToPortfolio(item);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to add portoflio item to db", e);
			throw e;
		} finally {
			this.dataManager.releaseDb(true, this.getClass().getName());
		}
	}

	/**
	 * @param price
	 * @param count
	 * @return
	 */
	private float calculateFee(final float price, final int count) {
		float fee = (price * count) * 0.001f;
		return fee;
	}

	/**
	 * task to load price from db
	 * @author tomas
	 *
	 */
	class DayDataTask extends AsyncTask<StockItem, Integer, Float> {

		@Override
		protected Float doInBackground(StockItem... params) {
			if (params == null || params.length != 1)
				return 0f;
			StockItem stockItem = params[0];
			float stockPrice = 0;
			try {
				stockPrice = dataManager.getLastValue(stockItem).getPrice();
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get stock day data", e);
			}
			return stockPrice;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Float result) {
			final TextView priceView = (TextView) findViewById(R.id.portfolioAddPrice);
			if (priceView != null)
				priceView.setText(String.valueOf(result));
			setProgressBarVisibility(false);
			super.onPostExecute(result);
		}
		
	}
}
