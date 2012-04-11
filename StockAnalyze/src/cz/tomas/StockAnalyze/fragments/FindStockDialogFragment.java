package cz.tomas.StockAnalyze.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import cz.tomas.StockAnalyze.R;

/**
 * @author tomas
 */
public class FindStockDialogFragment extends DialogFragment {

	private static final String URL_FIND = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback";

	public static FindStockDialogFragment newInstance(int titleId) {
		FindStockDialogFragment frag = new FindStockDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", titleId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new RuntimeException("use newInstance(...) static method to initialize this dialog");
		}
		int title = getArguments().getInt("title");
		Dialog dialog = new Dialog(getActivity());
		dialog.setTitle(title);
		dialog.setContentView(R.layout.dialog_find_stock);

		EditText edit = (EditText) dialog.findViewById(R.id.findEdit);
		edit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int i, KeyEvent keyEvent) {
				return false;
			}
		});
		View btn = dialog.findViewById(R.id.findAdd);

		return dialog;
	}
}
