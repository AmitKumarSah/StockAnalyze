package cz.tomas.StockAnalyze.StockList.search;

import android.content.Context;
import android.widget.ArrayAdapter;
import cz.tomas.StockAnalyze.Data.Model.SearchResult;

public final class SearchAdapter extends ArrayAdapter<String> {

	private SearchResult[] results;

	public SearchAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public void setResults(SearchResult[] results) {
		this.results = results;
		this.notifyDataSetChanged();
	}

	@Override
	public String getItem(int position) {
		if (results == null || position >= results.length) {
			return null;
		}
		final SearchResult result = results[position];
		return String.format("%s(%s,%S)", result.getName(), result.getSymbol(), result.getExchDisp());
	}

	public SearchResult getSearchItem(int position) {
		if (results == null || position >= results.length) {
			return null;
		}
		return results[position];
	}

	@Override
	public int getCount() {
		if (results == null) {
			return 0;
		}
		return results.length;
	}
}