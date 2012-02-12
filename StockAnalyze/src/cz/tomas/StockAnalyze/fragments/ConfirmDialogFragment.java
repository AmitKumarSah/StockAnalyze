/**
 * *****************************************************************************
 * Copyright (c) 2012, Inmite s.r.o. (www.inmite.eu).
 *
 * All rights reserved. This source code can be used only for purposes specified
 * by the given license contract signed by the rightful deputy of Inmite s.r.o.
 * This source code can be used only by the owner of the license.
 *
 * Any disputes arising in respect of this agreement (license) shall be brought
 * before the Municipal Court of Prague.
 *****************************************************************************
 */
package cz.tomas.StockAnalyze.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cz.tomas.StockAnalyze.R;

/**
 * fragment with ok button, use {@link #newInstance(int, ConfirmDialogFragment.IConfirmListener)} to instantiate the fragment
 */
public class ConfirmDialogFragment extends DialogFragment {

	public static final String ARG_TITLE_STRING = "titleString";
	public static final String ARG_TITLE = "title";

	public interface IConfirmListener {
		void onConfirmed(ConfirmDialogFragment fragment);
	}
	
	private IConfirmListener mListener;

	public static ConfirmDialogFragment newInstance(String title, IConfirmListener listener) {
		ConfirmDialogFragment frag = new ConfirmDialogFragment();
		frag.setListener(listener);
		frag.setCancelable(false);

		Bundle args = new Bundle();
		args.putString(ARG_TITLE_STRING, title);
		frag.setArguments(args);
		return frag;
	}

	public static ConfirmDialogFragment newInstance(int title, IConfirmListener listener) {
		ConfirmDialogFragment frag = new ConfirmDialogFragment();
		frag.setListener(listener);
		frag.setCancelable(false);
		
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, title);
		frag.setArguments(args);
		return frag;
	}

	private ConfirmDialogFragment() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mListener != null) {
							mListener.onConfirmed(ConfirmDialogFragment.this);
						}
					}
				});
		if (getArguments().containsKey(ARG_TITLE)) {
			int title = getArguments().getInt(ARG_TITLE);
			builder.setTitle(title);
		} else if (getArguments().containsKey(ARG_TITLE_STRING)) {
			String titleString = getArguments().getString(ARG_TITLE_STRING);
			builder.setTitle(titleString);
		}
		return builder.create();
	}

	private void setListener(IConfirmListener listener) {
		this.mListener = listener;
	}
}
