package cz.tomas.StockAnalyze.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tomas
 */
public class SetMarketsDialogFragment extends DialogFragment {

	public interface IMarketsActivity {
		void onUpdateMarkets();
	}

	DataManager dataManager;
	final List<Market> markets = new ArrayList<Market>();
	int[] originalUiOrder;
	boolean isModified;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		this.dataManager = (DataManager) getActivity().getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		this.markets.addAll(dataManager.getMarkets());
		this.originalUiOrder = new int[this.markets.size()];
		for (int i = 0; i < markets.size(); i++) {
			Market market = markets.get(i);
			final int uiOrder = market.getUiOrder();
			this.originalUiOrder[i] = uiOrder >= 0 ? uiOrder : 0;
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.selectMarkets);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (isModified) {
					SaveMarketsTask task = new SaveMarketsTask(getActivity());
					task.execute((Void) null);
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dismiss();
			}
		});

		String[] labels = new String[markets.size()];
		boolean[] checked = new boolean[markets.size()];
		int index = 0;
		for (Market market : markets) {
			labels[index] = market.getName();
			checked[index] = market.getUiOrder() >= 0;
			index++;
		}
		builder.setMultiChoiceItems(labels, checked, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position, boolean checked) {
				isModified = true;
				final Market market = markets.get(position);
				if (! checked) {
					market.setUiOrder(Market.HIDDEN);
				} else {
					market.setUiOrder(originalUiOrder[position]);
				}
			}
		});
		return builder.create();
	}

	private final class SaveMarketsTask extends AsyncTask<Void, Integer, Void> {

		private ProgressDialogFragment progressDialog;
		private final FragmentActivity activity;

		private SaveMarketsTask(FragmentActivity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialogFragment.newInstance(R.string.selectMarkets, R.string.savingMarkets);
			progressDialog.show(this.activity.getSupportFragmentManager(), "marketsProgress");
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				dataManager.updateMarketsUiOrder(markets);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to update markets", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (activity instanceof IMarketsActivity) {
				((IMarketsActivity) activity).onUpdateMarkets();
			}
			progressDialog.dismiss();
		}

	}
}
