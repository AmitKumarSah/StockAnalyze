package cz.tomas.StockAnalyze.StockList;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.fragments.StockGridFragment;
import cz.tomas.StockAnalyze.fragments.StockListFragment;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Adapter creating {@link StockListFragment} for selected Markets.
 * <br/>
 * Most of the code is taken from {@link android.support.v4.app.FragmentPagerAdapter},
 * but we changed the construction of fragment tag so we can add and remove fragments when user changes
 * market settings.
 * @author tomas
 *
 */
public final class StocksPagerAdapter extends PagerAdapter {

	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = Utils.LOG_TAG;

	private Market[] markets;
	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;
	
	public StocksPagerAdapter(FragmentManager fm) {
		mFragmentManager = fm;
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
	public Object instantiateItem(ViewGroup container, int position) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		// Do we already have this fragment?
		Market market = this.getMarketByPosition(position);
		String name = makeFragmentName(container.getId(), position, market);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			if (DEBUG) Log.v(TAG, "Attaching item #" + position + ": f=" + fragment);
			mCurTransaction.attach(fragment);
		} else {
			fragment = getItem(position);
			if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
			mCurTransaction.add(container.getId(), fragment,
					makeFragmentName(container.getId(), position, market));
		}
		if (fragment != mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}

		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		if (Utils.DEBUG) Log.v(TAG, "Detaching item #" + position + ": f=" + object
				+ " v=" + ((Fragment)object).getView());
		mCurTransaction.detach((Fragment)object);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		Fragment fragment = (Fragment)object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
				mCurrentPrimaryItem.setUserVisibleHint(false);
			}
			if (fragment != null) {
				fragment.setMenuVisibility(true);
				fragment.setUserVisibleHint(true);
			}
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
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

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment)object).getView() == view;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}


	private static String makeFragmentName(int viewId, int position, Market market) {
		return String.format("android:switcher:%d index %d:%s", viewId, position, market.toString());
	}
}
