package cz.tomas.StockAnalyze.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.Utils;

public abstract class AbstractSqlHelper extends SQLiteOpenHelper {

	protected static final String TABLE_DROP = "DROP TABLE IF EXISTS ";
	/**
	 * db counter - only for diag purpose so far
	 */
	private int acquireCounter = 0;
	private boolean keepDbOpen = false;
	/**
	 * determine deffered db close
	 * if set to true, in next successful db release, 
	 * will close the db
	 */
	private boolean closeDb = false;

	public AbstractSqlHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/**
	 * Close any open database object, if database isn't acquired
	 */
	@Override
	public synchronized void close() {
//		acquireCounter--;
//		if (acquireCounter == 0) {
//			if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "database released, counter: " + acquireCounter);
//			keepDbOpen = false;
//		}
		if (! keepDbOpen) {
			if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "Closing database...");
			super.close();
		}
	}

//	@Override
//	public void onOpen(SQLiteDatabase db) {
//		super.onOpen(db);
//
//		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "db open request, counter: " + acquireCounter);
//
//		keepDbOpen = true;
//		acquireCounter++;
//	}

	/**
	 * calling this method will cause the database connection to be open until 
	 * releaseDb() will be called
	 * The purpose of this method is to allow service to access database and not get 
	 * interrupted - closed - by user interaction in UI.
	 * This calling won't open the connection.
	 * 
	 * @param applicant object acquiring the database - only informative purpose
	 */
	public synchronized void acquireDb(Object applicant) {
		keepDbOpen = true;
		acquireCounter++;
		if (applicant == null) {
			applicant = "unknown";
		}
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, String.format("database acquired by %s ... %d", applicant.toString(), acquireCounter));
	}

	/**
	 * Release previously acquired database. If database wasn't acquired,
	 * nothing would happen
	 * @param close true to invoke close method
	 * @param applicant object releasing database - only informative
	 */
	public synchronized void releaseDb(boolean close, Object applicant) {
		acquireCounter--;
		if (applicant == null)
			applicant = "unknown";
		if (acquireCounter == 0){
			Log.d(Utils.LOG_TAG, "database released by " + applicant.toString());
			keepDbOpen = false;

			if (close || closeDb) {
				this.close();
				closeDb = false;
			}
		} else {
			Log.d(Utils.LOG_TAG, String.format("request from %s: can NOT release db, still acquired... %d", applicant.toString(), acquireCounter));
			closeDb |= close;
		}
	}
}