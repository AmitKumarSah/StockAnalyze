package cz.tomas.StockAnalyze.StockList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.viewpagerindicator.TitleProvider;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.StockGridFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;

import java.util.Collection;

/**
 * adapter creating {@link StockListFragment} for selected Markets
 * @author tomas
 *
 */
public final class StocksPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private Market[] markets;
	
	public StocksPagerAdapter(FragmentManager fm, Collection<Market> markets) {
		super(fm);

		if (markets != null) {
			this.markets = markets.toArray(new Market[markets.size()]);
		}
	}
	
	public void setMarkets(Market[] markets) {
		this.markets = markets;
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
//		if (market.getType() == Market.TYPE_SELECTIVE) {
//			fragment = new CustomStockGridFragment();
//		} else {
//			fragment = new StockGridFragment();
//		}
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
		return market.getName().toUpperCase();
	}

}
