package cz.tomas.StockAnalyze.Portfolio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.viewpagerindicator.TitleProvider;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.PortfolioListFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;

import java.util.Collection;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class PortfolioPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private Market[] markets;
	
	public PortfolioPagerAdapter(FragmentManager fm, Collection<Market> markets) {
		super(fm);

		if (markets != null) {
			this.markets = markets.toArray(new Market[markets.size()]);
		}
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
	public String getTitle(int position) {
		Market market = this.markets[position];
		return market.getCurrencyCode();
	}

}
