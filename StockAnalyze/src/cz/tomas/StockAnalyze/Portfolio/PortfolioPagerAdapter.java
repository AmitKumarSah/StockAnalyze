package cz.tomas.StockAnalyze.Portfolio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.jakewharton.android.viewpagerindicator.TitleProvider;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.PortfolioListFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;

import java.util.List;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class PortfolioPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private List<Market> markets;
	
	public PortfolioPagerAdapter(FragmentManager fm, List<Market> markets) {
		super(fm);
		
		this.markets = markets;
	}

	public String getFragmentTag(int position) {
		if(this.markets == null || position >= this.markets.size()) {
			return null;
		}
		return this.markets.get(position).toString();
	}
	
	@Override
	public Fragment getItem(int position) {
		PortfolioListFragment fragment = new PortfolioListFragment();
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
		return market.getCurrencyCode();
	}

}
