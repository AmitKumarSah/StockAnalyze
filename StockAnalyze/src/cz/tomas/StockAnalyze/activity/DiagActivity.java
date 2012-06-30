package cz.tomas.StockAnalyze.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Journal;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;

/**
 * @author Tomas Vondracek
 */
public class DiagActivity extends BaseActivity {

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.diagnostics);
		TextView txtJournal = (TextView) findViewById(R.id.diagJournal);

		ReadJournalTask task = new ReadJournalTask(txtJournal);
		task.execute();
	}

	private class ReadJournalTask extends AsyncTask<Void, Integer, String> {

		private final TextView txtJournal;

		public ReadJournalTask(TextView txtJournal) {
			super();
			this.txtJournal = txtJournal;
		}

		@Override
		protected String doInBackground(Void... voids) {
			Journal journal = (Journal) getApplicationContext().getSystemService(Application.JOURNAL_SERVICE);
			return journal.getContent();
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			txtJournal.setText(s);
		}
	}
}