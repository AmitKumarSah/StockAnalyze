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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Dialog fragment with {@link android.app.ProgressDialog}
 * 
 * @author tomas
 * 
 */
public class ProgressDialogFragment extends DialogFragment {

	private DialogInterface.OnCancelListener cancelListener;
	
	public static ProgressDialogFragment newInstance(int titleId, int messageId) {
		ProgressDialogFragment frag = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", titleId);
		args.putInt("message", messageId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new RuntimeException("use newInstance(...) static method to initialize this dialog");
		}
		int title = getArguments().getInt("title");
		int messageId = getArguments().getInt("message");

		ProgressDialog dlg = new ProgressDialog(getActivity());
		dlg.setTitle(title);
		dlg.setMessage(getText(messageId));
		dlg.setCancelable(true);
		dlg.setIndeterminate(true);
		return dlg;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (this.cancelListener != null) {
			this.cancelListener.onCancel(dialog);
		}
	}

	public void setCancelListener(DialogInterface.OnCancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		this.cancelListener = null;
	}
	
}
