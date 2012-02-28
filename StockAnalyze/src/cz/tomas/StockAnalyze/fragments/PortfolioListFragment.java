package cz.tomas.StockAnalyze.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.*;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListAdapter;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListData;
import cz.tomas.StockAnalyze.Portfolio.PortfolioLoader;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.PortfolioDetailActivity;
import cz.tomas.StockAnalyze.activity.PortfoliosActivity;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.text.NumberFormat;

/**
 * fragment holding list of portfolio items of one currency
 * @author tomas
 *
 */
public final class PortfolioListFragment extends ListFragment implements LoaderCallbacks<PortfolioListData> {
	
	public static final String EXTRA_REFRESH = "portfolioRefresh";
	
	private PortfolioListAdapter adapter;

	private Portfolio portfolio;
	
	private Market market;
	
	private View headerView;
	private View footerView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		headerView = inflater.inflate(R.layout.portfolio_list_header, null);
		footerView = inflater.inflate(R.layout.portfolio_list_footer, null);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.portfolio = (Portfolio) getActivity().getApplicationContext().getSystemService(Application.PORTFOLIO_SERVICE);
		this.market = (Market) this.getArguments().get(StockListFragment.ARG_MARKET);
		
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());

		this.setEmptyText(getText(R.string.loading));
		if (this.getListAdapter() == null) {
			this.getListView().addHeaderView(headerView, null, false);
			this.getListView().addFooterView(footerView, null, false);
			this.adapter = new PortfolioListAdapter(this.getActivity());
			this.setListAdapter(this.adapter);
		}

		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				// this feature is not yet finished
				if (Utils.DEBUG) {
					final PortfolioItem portfolioItem = (PortfolioItem) getListAdapter().getItem(position - getListView().getHeaderViewsCount());
					goToPortfolioDetail(portfolioItem);
				}
			}
		});

		this.getListView().setTag(market.getCurrencyCode());
		this.getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		setListAdapter(null);
	}

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
		
		if (portfolioItem == null) {
			return false;
		}
		
		switch (item.getItemId()) {
			case R.id.portfolio_item_context_menu_stock_detail:
				StockItem stock = adapter.getStockItem(portfolioItem);
				NavUtils.goToStockDetail(stock, getActivity());
			return true;
			case R.id.portfolio_item_context_menu_remove:
				removePortfolioRecord(portfolioItem);
				return true;
			case R.id.portfolio_item_context_menu_detail:
				goToPortfolioDetail(portfolioItem);
				return true;
			case R.id.portfolio_item_context_menu_add_more:
				StockItem stockItem = adapter.getStockItem(portfolioItem);
				DayData data = adapter.getData(portfolioItem);
				
				NavUtils.goToAddToPortfolio(getActivity(), stockItem, data);
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
	    	this.getLoaderManager().initLoader(0, null, this);
	        break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * navigate to {@link PortfolioDetailActivity} with selected stock item
	 */
	private void goToPortfolioDetail(PortfolioItem item) {
		Intent intent = new Intent(this.getActivity(), PortfolioDetailActivity.class);
		StockItem stockItem = this.adapter.getStockItem(item);
		intent.putExtra(PortfoliosActivity.EXTRA_STOCK_ITEM, stockItem);
		this.startActivity(intent);
	}
	
	/**
	 * remove all portfolio items in portfolio group,
	 * this task is done on background and progress dialog is showed while
	 * the operation is in progress
	 * @param portfolioItem
	 */
	private void removePortfolioRecord(final PortfolioItem portfolioItem) {
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
				getActivity().dismissDialog(PortfoliosActivity.DIALOG_PROGRESS);
				super.onPostExecute(result);
			}
			
		};
		task.execute();
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
		NumberFormat currencyFormat = FormattingUtils.getPriceFormat(this.market.getCurrency());
    	String strAbsChange = percentFormat.format(portfolioSummary.getTotalAbsChange());
    	String strChange = percentFormat.format(portfolioSummary.getTotalPercChange());
    	String totalValue = currencyFormat.format(portfolioSummary.getTotalValue());
    	
    	if (txtValueSum != null) {
    		txtValueSum.setText(totalValue);
    	}
    	if (txtChangeSum != null) {
    		txtChangeSum.setText(String.format("%s (%s%%)", strAbsChange, strChange));
    	}
	}

	@Override
	public Loader<PortfolioListData> onCreateLoader(int id, Bundle args) {
		return new PortfolioLoader(this.getActivity(), this.market);
	}

	@Override
	public void onLoadFinished(Loader<PortfolioListData> loader,
			PortfolioListData data) {
		if (data == null) {
			return;
		} else if (data.isEmpty()) {
			this.setEmptyText(getText(R.string.noPortfolioItems));
		}
		this.adapter.setData(data);
		this.fillPortfolioSummary(data.portfolioSummary);
	}

	@Override
	public void onLoaderReset(Loader<PortfolioListData> loader) {
	}
}
