package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.GridView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.SearchResult;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.search.SearchStockItemTask;
import cz.tomas.StockAnalyze.UpdateScheduler;

/**
 * fragment for market of type {@link cz.tomas.StockAnalyze.Data.Model.Market#TYPE_SELECTIVE} where user can choose
 * stocks to display
 * @author tomas
 */
public class CustomStockGridFragment extends StockGridFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.custom_stock_grid, container, false);
		this.grid = (GridView) v.findViewById(R.id.gridview);
		this.progress = v.findViewById(R.id.progress);

		View btnAdd = v.findViewById(R.id.stockAdd);
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				searchForStock();
			}
		});
		return v;
	}

	private void searchForStock() {
		SearchStockDialogFragment fragment = SearchStockDialogFragment.newInstance(R.string.addStockItem, helper.getMarket());
//		fragment.setTargetFragment(this, 0);
		fragment.show(getFragmentManager(), "addStock");
	}

	public void addStock(SearchResult searchResult) {
		final ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(R.string.addStockItem, R.string.loading);
		fragment.show(getFragmentManager(), "add_progress");
		SearchStockItemTask task = new SearchStockItemTask(getActivity(), helper.getMarket()) {
			@Override
			protected void onPostExecute(StockItem stockItem) {
				fragment.dismiss();
				if (stockItem != null) {
					final String message = String.format("%s %s", stockItem.getName(), getText(R.string.addedStock).toString());
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
					// update data - this will cause grid update too
					UpdateScheduler scheduler = (UpdateScheduler) getActivity().getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
					scheduler.updateImmediately(helper.getMarket());
				} else {
					Toast.makeText(getActivity(), R.string.addStockItemNotFound, Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(searchResult);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.custom_stock_list_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuCustomStockAdd) {
			searchForStock();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
