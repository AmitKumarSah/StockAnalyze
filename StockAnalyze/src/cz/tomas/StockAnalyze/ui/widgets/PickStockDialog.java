package cz.tomas.StockAnalyze.ui.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.SimpleMarketAdapter;
import cz.tomas.StockAnalyze.StockList.SimpleStockListAdapter;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

public final class PickStockDialog extends Dialog implements IListAdapterListener<StockItem> {

	public interface IStockDialogListener {
		void onStockSelected(StockItem item);
	}
	
	private static final int ADAPTER_NONE = 0;
	private static final int ADAPTER_MARKETS = 1;
	private static final int ADAPTER_STOCKS = 2;
	
	final private BaseAdapter marketAdapter;
	private BaseAdapter stocksAdapter;
	final ListView list;
	final ProgressBar progressBar;
	
	private int currentAdapter;
	
	private IStockDialogListener listener;
	
	public PickStockDialog(final Context context, final boolean indeces) {
		super(context);

		final FrameLayout.LayoutParams listLayoutParams = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		list = new ListView(context);
		list.setLayoutParams(listLayoutParams);
		list.setBackgroundColor(Color.WHITE);
		list.setCacheColorHint(Color.WHITE);
		
		final TextView emtyView = new TextView(context);
		emtyView.setText(R.string.loading);
		list.setEmptyView(emtyView);
		list.setMinimumHeight(200);
		list.setMinimumWidth(200);
		
		this.progressBar = new ProgressBar(context);
		this.progressBar.setIndeterminate(true);
		this.progressBar.setVisibility(View.GONE);
		
		this.setContentView(list);
		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		this.addContentView(this.progressBar, layoutParams);
		
		this.setTitle(R.string.pickMarket);

		this.marketAdapter = new SimpleMarketAdapter(context, indeces);
		list.setAdapter(this.marketAdapter);
		this.currentAdapter = ADAPTER_MARKETS;
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				if (currentAdapter == ADAPTER_MARKETS) {
					final Market market = (Market) marketAdapter.getItem(position);
					if (market != null) {
						final boolean indecesMarket = market.equals(Markets.GLOBAL);
						currentAdapter = ADAPTER_STOCKS;
						stocksAdapter = new SimpleStockListAdapter(getContext(), market, PickStockDialog.this, indecesMarket);
						setTitle(R.string.pickStock);
						list.setAdapter(stocksAdapter);
					} else {
						Log.w(Utils.LOG_TAG, "market as result from adapter is null");
					}
				} else if (currentAdapter == ADAPTER_STOCKS) {
					final StockItem stockItem = (StockItem) stocksAdapter.getItem(position);
					if (listener != null) {
						listener.onStockSelected(stockItem);
					}
				}
			}
		});
	}

	public void setListener(IStockDialogListener dialogListener) {
		this.listener = dialogListener;
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#dismiss()
	 */
	@Override
	public void dismiss() {
		this.currentAdapter = ADAPTER_NONE;
		this.list.setAdapter(null);
		super.dismiss();
	}

	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (this.currentAdapter == ADAPTER_NONE) {
			this.list.setAdapter(this.marketAdapter);
			this.currentAdapter = ADAPTER_MARKETS;
		}
	}
	
	@Override
	public void onListLoading() {
		this.progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void onListLoaded(StockItem data) {
		this.progressBar.setVisibility(View.GONE);
	}
	
	
}
