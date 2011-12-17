package cz.tomas.StockAnalyze.fragments;

import java.text.NumberFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListAdapter;
import cz.tomas.StockAnalyze.activity.PortfolioDetailActivity;
import cz.tomas.StockAnalyze.activity.PortfoliosActivity;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public final class PortfolioListFragment extends ListFragment implements OnSharedPreferenceChangeListener {
	
	public static final String EXTRA_REFRESH = "portfolioRefresh";
	public static final String EXTRA_STOCK_ITEM = "portfolioStockItem";
	
	private DataManager dataManager;
	private Portfolio portfolio;
	private PortfolioListAdapter adapter;
	
	private Market market;
	
	private View headerView;
	private View footerView;
	private static boolean isDirty;
	
	private View refreshButton;
	private Animation refreshAnim;
	
	private SharedPreferences prefs;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		headerView = inflater.inflate(R.layout.portfolio_list_header, null);
		footerView = inflater.inflate(R.layout.portfolio_list_footer, null);
		
		return view;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());
		
		this.dataManager = DataManager.getInstance(getActivity());
		
		FragmentActivity activity = this.getActivity();
		//isDirty |= activity.getIntent().getBooleanExtra(EXTRA_REFRESH, false);
		
		this.refreshButton = getActivity().findViewById(R.id.actionRefreshButton);

		this.setEmptyText(getText(R.string.loading));
		this.getListView().addHeaderView(headerView, null, false);
		this.getListView().addFooterView(footerView, null, false);
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				final PortfolioItem portfolioItem = (PortfolioItem) getListAdapter().getItem(position -1);
				goToPortfolioDetail(portfolioItem);
			}
		});

		if (portfolio == null)
			portfolio = new Portfolio(activity);
		
		this.prefs = activity.getSharedPreferences(Utils.PREF_NAME, 0);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		this.market = (Market) this.getArguments().get(StockListFragment.ARG_MARKET);

		this.getListView().setTag(market.getCurrencyCode());
		this.fill();
	}

	/** 
	 * check if it is necessary to update the adapter and listview
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) {
			this.setListAdapter(adapter);
		}

		isDirty |= getActivity().getIntent().getBooleanExtra(EXTRA_REFRESH, false);
		if (isDirty) {
			adapter.refresh();
			isDirty = false;
		}
		this.dataManager.addUpdateChangedListener(listener);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		this.dataManager.removeUpdateChangedListener(listener);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	private void fill() {
		if (adapter == null) {
			adapter = new PortfolioListAdapter(this.getActivity(), R.layout.stock_list, this.dataManager, this.portfolio, this.market);
			adapter.addPortfolioListener(new IListAdapterListener<PortfolioSum>() {

				@Override
				public void onListLoading() {					
					if (refreshButton != null)
						refreshAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh_rotate);
						refreshButton.startAnimation(refreshAnim);
				}

				@Override
				public void onListLoaded(PortfolioSum portfolioSummary) {
					Log.d(Utils.LOG_TAG, "Updating portfolio summary");
					fillPortfolioSummary(portfolioSummary);
					
					if (refreshAnim != null)
						refreshAnim.setDuration(0);	
					if (adapter.getCount() == 0) {
						setEmptyText(getText(R.string.noPortfolioItems));
					}
				}
			});
			isDirty = false;
		}
		// restore portfolio summary, it should available in case of resuming
		if (adapter.getPortfolioSummary() != null) {
			this.fillPortfolioSummary(adapter.getPortfolioSummary());
		}
	}
	
	private IUpdateDateChangedListener listener = new IUpdateDateChangedListener() {
		
		@Override
		public void OnLastUpdateDateChanged(long updateTime) {
			Log.d(Utils.LOG_TAG, "refreshing portfolio list adapter because of datamanager update");
			isDirty = true;
		}
	};
	
	/** 
	 * context menu for all stock items in list view
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.portfolio_item_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/** 
	 * stock context menu for portfolio item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (this.isDetached()) {
			return false;
		}
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		ListView listView = (ListView) info.targetView.getParent();
		if (listView.getTag() != null && ! listView.getTag().equals(getListView().getTag())) {
			return false;
		}
		final PortfolioItem portfolioItem = (PortfolioItem) adapter.getItem(info.position - 1);
		
		if (portfolioItem == null)
			return true;
		
		switch (item.getItemId()) {
			case R.id.portfolio_item_context_menu_stock_detail:
			if (portfolioItem.getStockId() != null) {
				new Thread(new Runnable() {
					public void run() {
						StockItem stock = dataManager.getStockItem(portfolioItem.getStockId(), portfolioItem.getMarketId());
						NavUtils.goToStockDetail(stock, getActivity());
					}
				}).start();
			}
			return true;
			case R.id.portfolio_item_context_menu_remove:
				try {
					removePortfolioRecord(portfolioItem);
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to remove portfolio item", e);
				}
				return true;
			case R.id.portfolio_item_context_menu_detail:
				goToPortfolioDetail(portfolioItem);
				return true;
			case R.id.portfolio_item_context_menu_add_more:
				new Thread(new Runnable() {
					public void run() {
						StockItem stock = dataManager.getStockItem(portfolioItem.getStockId(), portfolioItem.getMarketId());
						DayData data = adapter.getData(portfolioItem);
						
						NavUtils.goToAddToPortfolio(getActivity(), stock,data);
					}
				}).start();
			return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menu_refresh:
	    	this.adapter.refresh();
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * navigate to {@link PortfolioDetailActivity} with selected stock item
	 */
	private void goToPortfolioDetail(PortfolioItem item) {
		Intent intent = new Intent(this.getActivity(), PortfolioDetailActivity.class);
		StockItem stockItem = this.adapter.getStockItem(item);
		intent.putExtra(EXTRA_STOCK_ITEM, stockItem);
		this.startActivity(intent);
	}
	
	/**
	 * remove all portfolio items in portfolio group,
	 * this task is done on background and progress dialog is showed while
	 * the operation is in progress
	 * @param portfolioItem
	 */
	private void removePortfolioRecord(final PortfolioItem portfolioItem) {
		if (refreshButton != null)
			refreshButton.setVisibility(View.VISIBLE);
		FlurryAgent.onEvent(Consts.FLURRY_EVENT_PORTFOLIO_REMOVE);
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				getActivity().showDialog(PortfoliosActivity.DIALOG_PROGRESS);
				super.onPreExecute();
			}
			@Override
			protected Void doInBackground(Void... params) {
				try {
					StockItem stock = adapter.getStockItem(portfolioItem);
					portfolio.removeFromPortfolio(stock.getId());
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to remove portfolio item", e);
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				adapter.refresh();
				getActivity().dismissDialog(PortfoliosActivity.DIALOG_PROGRESS);
				super.onPostExecute(result);
			}
			
		};
		task.execute((Void[])null);
	}	

	/**
	 * fill text views with portfolio summary
	 * @param portfolioSummary
	 */
	private void fillPortfolioSummary(PortfolioSum portfolioSummary) {
		if (portfolioSummary == null || this.isRemoving() || this.isDetached() || ! this.isAdded()) {
			return;
		}
		TextView txtValueSum = (TextView)getListView().findViewById(R.id.txtPortfolioFooterSumValue);
		TextView txtChangeSum = (TextView)getListView().findViewById(R.id.txtPortfolioFooterSumChange);
		
		NumberFormat percentFormat = FormattingUtils.getPercentFormat();
    	String strAbsChange = percentFormat.format(portfolioSummary.getTotalAbsChange());
    	String strChange = percentFormat.format(portfolioSummary.getTotalPercChange());
    	String totalValue = percentFormat.format(portfolioSummary.getTotalValue());
    	
    	if (txtValueSum != null)
    		txtValueSum.setText(totalValue);
    	if (txtChangeSum != null)
    		txtChangeSum.setText(String.format("%s (%s%%)", strAbsChange, strChange));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Utils.PREF_PORTFOLIO_INCLUDE_FEE)) {
			isDirty = true;
			this.fill();
		}
		
	}
}
