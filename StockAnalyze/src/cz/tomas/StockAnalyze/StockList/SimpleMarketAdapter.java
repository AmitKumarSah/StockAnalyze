package cz.tomas.StockAnalyze.StockList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.Collection;

public final class SimpleMarketAdapter extends ArrayAdapter<Market> {

	private LayoutInflater inflater;
	
	public SimpleMarketAdapter(Context context, boolean includeIndeces) {
		super(context, 0);
		
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Collection<Market> markets = DataManager.getInstance(context).getMarkets();
		for (Market market : markets) {
			this.add(market);
		}
		if (includeIndeces) {
			this.add(Markets.GLOBAL);
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.select_dialog_singlechoice, null);
		}
		Market market = this.getItem(position);
		if (market != null) {
			((TextView) convertView).setText(market.getDescription());
		} else {
			Log.w(Utils.LOG_TAG, "market is null in SimpleMarketAdapter at position " + position);
		}
		return convertView;
	}	
}
