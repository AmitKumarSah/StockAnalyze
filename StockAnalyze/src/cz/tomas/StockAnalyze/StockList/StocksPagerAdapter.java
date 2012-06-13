package cz.tomas.StockAnalyze.StockList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.StockGridFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class StocksPagerAdapter extends FragmentPagerAdapter {

	private Market[] markets;
	
	public StocksPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void setMarkets(Market[] markets) {
		if (markets == null) {
			this.markets = null;
		} else {
			Market[] temp = new Market[markets.length];
			int index = 0;
			for (Market market : markets) {
				if (market.getUiOrder() >= 0) {
					temp[index] = market;
					index++;
				}
			}
			this.markets = new Market[index];
			System.arraycopy(temp, 0, this.markets, 0, index);
		}
		this.notifyDataSetChanged();
	}

	public Market getMarketByPosition(int position) {
		if (this.markets == null || position >= this.markets.length) {
			return null;
		}
		return this.markets[position];
	}
	
	@Override
	public Fragment getItem(int position) {
		//StockListFragment fragment = new StockListFragment();
		Market market = this.getMarketByPosition(position);
		Fragment fragment = new StockGridFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(StockListFragment.ARG_MARKET, market);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getCount() {
		if (this.markets == null) {
			return 0;
		}
		return this.markets.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Market market = this.markets[position];
		return market.getName().toUpperCase();
	}

}
