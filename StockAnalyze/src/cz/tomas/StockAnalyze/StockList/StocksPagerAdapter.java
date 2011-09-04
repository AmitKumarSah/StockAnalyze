package cz.tomas.StockAnalyze.StockList;

import java.util.ArrayList;
import java.util.List;

import com.jakewharton.android.viewpagerindicator.TitleProvider;

import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.StockListFragment;
import cz.tomas.StockAnalyze.utils.Markets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class StocksPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private List<Market> markets;
	
	public StocksPagerAdapter(FragmentManager fm) {
		super(fm);
		
		this.markets = new ArrayList<Market>();
		this.markets.add(Markets.CZ);
		this.markets.add(Markets.DE);
	}

	@Override
	public Fragment getItem(int position) {
		StockListFragment fragment = new StockListFragment();
		Market market = this.markets.get(position);
		Bundle bundle = new Bundle();
		bundle.putSerializable(StockListFragment.ARG_MARKET, market);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getCount() {
		return this.markets.size();
	}

	@Override
	public String getTitle(int position) {
		Market market = this.markets.get(position);
		return market.getName();
	}

}
