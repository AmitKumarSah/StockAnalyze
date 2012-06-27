package cz.tomas.StockAnalyze.Portfolio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.PortfolioListFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;

import java.util.Collection;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class PortfolioPagerAdapter extends FragmentPagerAdapter {

	private Market[] markets;
	
	public PortfolioPagerAdapter(FragmentManager fm, Collection<Market> markets) {
		super(fm);

		if (markets != null) {
			this.markets = markets.toArray(new Market[markets.size()]);
		}
	}

	public void setMarkets(Collection<Market> markets) {
		if (markets != null) {
			this.markets = markets.toArray(new Market[markets.size()]);
		} else {
			this.markets = null;
		}
		this.notifyDataSetChanged();
	}
	
	@Override
	public Fragment getItem(int position) {
		PortfolioListFragment fragment = new PortfolioListFragment();
		Market market = this.markets[position];
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
		if (this.markets != null && position < this.markets.length) {
			Market market = this.markets[position];
			return market.getCurrencyCode();
		}
		return null;
	}

}
