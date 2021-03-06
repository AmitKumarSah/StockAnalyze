package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.fragments.StockFragmentHelper.IStockFragment;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * Fragment with list of stocks and their prices.
 * It needs to have {@link Market} in arguments - see setArguments(Bundle b)
 * @author tomas
 *
 */
public class StockListFragment extends ListFragment implements IStockFragment {

	public static String ARG_MARKET = "market";
	
	private StockFragmentHelper helper;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final StockListAdapter adapter = new StockListAdapter(getActivity(), R.layout.item_stock_list);
		this.helper = new StockFragmentHelper(this, getArguments(), adapter);
		
		this.setListAdapter(adapter);
		this.setEmptyText(getString(R.string.loading));
		
		this.getListView().setTextFilterEnabled(true);

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				StockItem stock = (StockItem) getListView().getItemAtPosition(position);
				NavUtils.goToStockDetail(stock, adapter.getDayData(stock), getActivity());
			}
		});
		
		getLoaderManager().initLoader(0, null, this.helper);
	}

	@Override
	public void onLoadFinished() {
	}
}
