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
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.PortfoliosActivity;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;
import cz.tomas.StockAnalyze.fragments.PortfolioListFragment;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class AddPortfolioItemActivity extends BaseActivity {

	private static final String INSTANCE_COUNT = "count";
	private static final String INSTANCE_PRICE = "price";
	
	private static final int MINIMAL_FEE = 0;
	private DataManager dataManager = null;
	
	private StockItem stockItem;
	
	private TextView totalValueView = null;
	
	private TextView feeView;
	private TextView priceView;
	private TextView countView; 
	
	private boolean manualFee;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.dataManager = (DataManager) this.getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		this.setContentView(R.layout.portfolio_add_item_layout);
		this.setProgressBarVisibility(false);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.hasExtra(Utils.EXTRA_STOCK_ITEM) && intent.hasExtra(Utils.EXTRA_MARKET_ID)) {
			final Market market = (Market) intent.getExtras().getSerializable(Utils.EXTRA_MARKET_ID);
			stockItem = intent.getExtras().getParcelable(Utils.EXTRA_STOCK_ITEM);
			final DayData data = intent.getExtras().getParcelable(Utils.EXTRA_DAY_DATA);
			
			this.totalValueView = (TextView) this.findViewById(R.id.portfolioAddTotalValue);
			this.feeView = (TextView) this.findViewById(R.id.portfolioAddDealFee);
			this.priceView = (TextView) this.findViewById(R.id.portfolioAddPrice);
			this.countView = (TextView) this.findViewById(R.id.portfolioAddCount);

			final TextView tickerView = (TextView) this.findViewById(R.id.portfolioAddTicker);
			final TextView marketView = (TextView) this.findViewById(R.id.portfolioAddMarket);
			
			final Button addButton = (Button) this.findViewById(R.id.portfolioAddButton);
			final Spinner dealSpinner = (Spinner) this.findViewById(R.id.portfolioAddSpinnerDeal);
			if (priceView != null && data != null) {
				priceView.setText(String.valueOf(data.getPrice()));
			} else if (data == null) {
				DayDataTask task = new DayDataTask();
				task.execute(stockItem);
			}
			
			if (marketView != null && market != null) {
				marketView.setText(market.getName());
			}
			if (stockItem != null && tickerView != null) {
				tickerView.setText(stockItem.getTicker());
			}

			if (feeView != null) {
				feeView.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						manualFee = true;
						return false;
					}
				});
			}
			if (countView != null) {
				countView.setOnFocusChangeListener(this.focusListener);
			}
			if (priceView != null) {
				priceView.setOnFocusChangeListener(this.focusListener);
			}
			if (addButton != null) {
				addButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						addButton.setEnabled(false);
						// check if all fields are filled
						if (priceView.getText() == null || countView.getText() == null ||
								marketView.getText() == null || tickerView.getText() == null) {
							Toast.makeText(AddPortfolioItemActivity.this, R.string.portfolioValidationMessage, Toast.LENGTH_SHORT).show();
							addButton.setEnabled(true);
						} else if (market != null && stockItem != null) {
							addItemToPortfolio(market, dealSpinner);
						}
						return;
					}
				});
			}
			try {
				String priceText = null;
				String countText = null;
				if (savedInstanceState != null) {
					priceText = savedInstanceState.getString(INSTANCE_PRICE);
					countText = savedInstanceState.getString(INSTANCE_COUNT);
					
					priceView.setText(priceText);
					countView.setText(countText);
				} else {
					priceText = priceView.getText().toString();
					countText = countView.getText().toString();
				}
				if (! TextUtils.isEmpty(priceText) && ! TextUtils.isEmpty(countText)) {
					final float price = Float.parseFloat(priceText);
					final int count = Integer.parseInt(countText);
					updateFeeAndValue(price, count);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to fill values", e);
			}
		}
	}
	
	@Override
	protected void onNavigateUp() {
		finish();
	}


	private OnFocusChangeListener focusListener = new OnFocusChangeListener() {
		
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
	};
	
	/**
	 * save stock price and count
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(INSTANCE_PRICE, priceView.getText().toString());
		outState.putString(INSTANCE_COUNT, countView.getText().toString());
		super.onSaveInstanceState(outState);
	}
	

	protected void addItemToPortfolio(final Market market, final Spinner dealSpinner) {
		try {
			final long count = Long.parseLong(countView.getText().toString());
			final String deal = dealSpinner.getSelectedItem().toString();
			final String[] deals = getResources().getStringArray(R.array.portfolioDealArray);
			final boolean sell = deal.equals(deals[1]);
				
			final double price = Double.parseDouble(priceView.getText().toString());
			double fee = 0;
			if (! manualFee) {
				fee = calculateFee(price, count);
			} else {
				fee = FormattingUtils.getPriceFormat(market.getCurrency())
					.parse(this.feeView.getText().toString()).doubleValue();
			}
			setProgressBarVisibility(true);
			AsyncTask<Void, Void, Void> task = new AddTask(fee, count, sell, price, market);
			task.execute();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to parse data from add portfolio layout", e);
		}
	}

	protected void updateFeeAndValue(final double price, final double count) {
		final double fee = calculateFee(price, count);
		
		final double value = price * count + fee;
		
		try {
			NumberFormat priceFormat = FormattingUtils.getPriceFormat(this.stockItem.getMarket().getCurrency());
			if (! this.manualFee) {
				this.feeView.setText(priceFormat.format(fee));
			}
			this.totalValueView.setText(priceFormat.format(value));
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to set total fee and value", e);
		}
	}


	/**
	 * @param price
	 * @param count
	 * @return
	 */
	private double calculateFee(final double price, final double count) {
		double fee = (price * count) * 0.004;
		return Math.max(MINIMAL_FEE, fee);
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
			if (priceView != null) {
				priceView.setText(String.valueOf(result));
				try {
					updateFeeAndValue(result, Integer.parseInt(countView.getText().toString()));
				} catch (NumberFormatException e) {
					Log.e(Utils.LOG_TAG, "failed to parse count text", e);
				}
			}
			setProgressBarVisibility(false);
			super.onPostExecute(result);
		}
	}

	private final class AddTask extends AsyncTask<Void, Void, Void> {
		private final double fee;
		private final long count;
		private final boolean sell;
		private final double price;
		private final Market market;
		private Exception ex;

		private AddTask(double fee, long count, boolean sell, double price, Market market) {
			this.fee = fee;
			this.count = count;
			this.sell = sell;
			this.price = price;
			this.market = market;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				addPortfolioItem(stockItem.getId(), count, price, market.getCurrencyCode(), market.getId(), fee, sell);
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
				Intent intent = new Intent(AddPortfolioItemActivity.this, PortfoliosActivity.class);
				intent.putExtra(PortfolioListFragment.EXTRA_REFRESH, true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				AddPortfolioItemActivity.this.startActivity(intent);
				finish();
			}
			super.onPostExecute(result);
		}
		

		private void addPortfolioItem(String stockId, long count, double price, String portfolioName, String marketId, double fee, boolean sell) throws SQLException {
			Portfolio portfolio = (Portfolio) AddPortfolioItemActivity.this.getApplicationContext().getSystemService(Application.PORTFOLIO_SERVICE);
			
			double buyPrice = 0, sellPrice = 0;
			long buyDate = 0, sellDate = 0;
			long ms = Calendar.getInstance().getTimeInMillis();
			if (! sell) {
				buyPrice = price;
				buyDate = ms;
			} else {
				sellPrice = price;
				sellDate = ms;
				count = -count;
			}
			// construct portfolio item and pass it to Portfolio
			PortfolioItem item = new PortfolioItem(stockId, portfolioName, (int) count, buyPrice, sellPrice,
					buyDate, sellDate, marketId);
			if (count > 0)
				item.setBuyFee(fee);
			else
				item.setSellFee(fee);

			try {
				portfolio.addToPortfolio(item);
				Log.i(Utils.LOG_TAG, "adding new portfolio item for " + stockId);
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, "failed to add portoflio item to db", e);
				throw e;
			}
			Map<String, String> pars = new HashMap<String, String>();
			pars.put(Consts.FLURRY_KEY_PORTFOLIO_NEW_SOURCE, getIntent().getStringExtra(Utils.EXTRA_SOURCE));
			pars.put(Consts.FLURRY_KEY_PORTFOLIO_NEW_OPERATOIN, count > 0 ? "buy" : "sell");
			FlurryAgent.onEvent(Consts.FLURRY_EVENT_PORTFOLIO_NEW, pars);
		}
	}
}
