package cz.tomas.StockAnalyze.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.google.gson.Gson;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.SearchResult;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.search.SearchAdapter;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

import java.net.URLEncoder;

/**
 * Dialog showing list of stocks that were found for user input.
 * @author tomas
 */
public class SearchStockDialogFragment extends DialogFragment {

	public interface ISearchListener {
		void onStockSelected(SearchResult stockTicker);
	}

	private static final String URL_FIND = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
	private static final String RESPONSE_BEGINNING = "YAHOO.Finance.SymbolSuggest.ssCallback({\"ResultSet\":{\"Query\":\"%s\",\"Result\":";

	ListView list;
	View progress;
	Market market;

	private final Handler uiHandler;
	Handler backHandler;
	private SearchTickerThread searchThread;
	private LoadRunnable loadRunnable;
	private ISearchListener searchListener;

	final Gson gson;

	public static SearchStockDialogFragment newInstance(int titleId, Market market) {
		SearchStockDialogFragment frag = new SearchStockDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", titleId);
		args.putSerializable("market", market);
		frag.setArguments(args);
		return frag;
	}

	public SearchStockDialogFragment() {
		this.uiHandler = new Handler();
		this.gson = new Gson();
	}

	@Override
	public void onStart() {
		super.onStart();
		this.searchThread = new SearchTickerThread();
		this.searchThread.start();
	}

	@Override
	public void onStop() {
		super.onStop();
		this.searchThread.cancel();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new RuntimeException("use newInstance(...) static method to initialize this dialog");
		}
		this.market = (Market) getArguments().getSerializable("market");
		int title = getArguments().getInt("title");
		Dialog dialog = new Dialog(getActivity());
		dialog.setTitle(title);
		dialog.setContentView(R.layout.dialog_find_stock);
		this.list = (ListView) dialog.findViewById(android.R.id.list);
		this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				SearchResult result = ((SearchAdapter) list.getAdapter()).getSearchItem(position);
				if (searchListener != null) {
					searchListener.onStockSelected(result);
				}
				dismiss();
			}
		});
		this.progress = dialog.findViewById(R.id.findProgress);

		EditText edit = (EditText) dialog.findViewById(R.id.findEdit);
		edit.requestFocus();
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.length() >= 1 && backHandler != null) {
					if (loadRunnable != null) {
						loadRunnable.cancel();
					}
					backHandler.removeCallbacks(loadRunnable);
					loadRunnable = new LoadRunnable(uiHandler, editable.toString());
					backHandler.post(loadRunnable);
					progress.setVisibility(View.VISIBLE);
				}
			}
		});

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void setSearchListener(ISearchListener searchListener) {
		this.searchListener = searchListener;
	}

	public final class SearchTickerThread extends Thread {

		private Looper looper;

		@Override
		public void run() {
			Looper.prepare();
			backHandler = new Handler();
			looper = Looper.myLooper();

			Looper.loop();
		}

		void cancel() {
			looper.quit();
		}
	}

	public final class LoadRunnable implements Runnable {

		private final Handler uiHandler;
		private final String currentTicker;
		private boolean isCanceled;

		LoadRunnable(Handler uiHandler, String currentTicker) {
			this.uiHandler = uiHandler;
			this.currentTicker = currentTicker;
		}
		@Override
		public void run() {
			try {
				final String ticker = URLEncoder.encode(this.currentTicker);
				String url = String.format(URL_FIND, ticker);
				if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "searching for " + ticker);

				byte[] data = DownloadService.GetInstance().DownloadFromUrl(url, false);
				String text = new String(data, "UTF-8");
				int start = String.format(RESPONSE_BEGINNING, ticker).length();
				text = text.substring(start, text.length() - 3);
				SearchResult[] results = gson.fromJson(text, SearchResult[].class);
				SearchResult[] filteredResults = new SearchResult[results.length];
				int index = 0;

				for (SearchResult result : results) {
					if (result != null && result.getExchDisp() != null &&
							result.getExchDisp().equalsIgnoreCase(market.getId())) {
						filteredResults[index] = result;
						index++;
					}
				}
				final SearchResult[] searchResults = new SearchResult[index];
				System.arraycopy(filteredResults, 0, searchResults, 0, index);

				if (! isCanceled && isAdded()) {
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							SearchAdapter adapter;
							if (list.getAdapter() == null) {
								adapter = new SearchAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
								list.setAdapter(adapter);
							} else {
								adapter = (SearchAdapter) list.getAdapter();
							}
							adapter.setResults(searchResults);
							progress.setVisibility(View.INVISIBLE);
						}
					});
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to getStock for stocks", e);
			}
		}

		public void cancel() {
			isCanceled = true;
		}
	}


}
