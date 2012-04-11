package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import cz.tomas.StockAnalyze.R;

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
				FindStockDialogFragment fragment = FindStockDialogFragment.newInstance(R.string.addStockItem);
				fragment.show(getFragmentManager(), "addStock");
			}
		});
		return v;
	}
}
