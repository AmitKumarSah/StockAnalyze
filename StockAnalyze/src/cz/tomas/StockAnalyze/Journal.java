package cz.tomas.StockAnalyze;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.io.*;
import java.util.*;

/**
 * @author Tomas Vondracek
 */
public class Journal {

	private static final String JOURNAL_NAME = "journal.txt";

	private final Queue<String> messagesBuffer;
	private final File journalFile;

	private String cachedJournalContent;
	private boolean isCachedContentDirty;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	Journal(Context context) {
		messagesBuffer = new LinkedList<String>();
		journalFile = new File(context.getFilesDir(), JOURNAL_NAME);
		if (! journalFile.exists()) {
			try {
				Log.d(Utils.LOG_TAG, "creating journal file");
				journalFile.createNewFile();
			} catch (IOException e) {
				Log.e(Utils.LOG_TAG, "failed to create journal file", e);
			}
		}
	}

	public void addException(String tag, String message, Exception e) {
		this.addMessage(tag, String.format("%s\nEXCEPTION:\n%s", message, e.toString()));
	}

	public synchronized void addMessage(String tag, String message) {
		Calendar cal = Calendar.getInstance();
		messagesBuffer.add(String.format("%s: %s: %s\n", FormattingUtils.formatDate(cal), tag, message));
	}

	public synchronized void flush() {
		if (journalFile == null || ! journalFile.exists() || ! journalFile.canWrite()) {
			Log.w(Utils.LOG_TAG, "we can't write to journal file");
			return;
		}
		if (this.messagesBuffer.size() == 0) {
			Log.d(Utils.LOG_TAG, "message buffer is empty");
			return;
		}
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(this.journalFile, true);

			for (int i = 0; i < messagesBuffer.size(); i++) {
				String message = messagesBuffer.poll();
				if (message != null) {
					filewriter.write(message);
				}

			}
		} catch (IOException e) {
			Log.e(Utils.LOG_TAG, "failed to flush journal", e);
		} finally {
			if (filewriter != null) {
				try {
					filewriter.close();
				} catch (IOException e) {
					Log.w(Utils.LOG_TAG, "failed to close journal file writer", e);
				}
			}
		}
		this.isCachedContentDirty = true;
	}

	public synchronized void clear() {
		if (journalFile == null || ! journalFile.exists()) {
			Log.w(Utils.LOG_TAG, "we can't clear to journal file");
			return;
		}
		boolean deleted = journalFile.delete();
		if (!deleted) {
			Log.w(Utils.LOG_TAG, "we didn't delete journal file ltough we tried to");
		}
		this.isCachedContentDirty = true;
	}

	public synchronized String getContent() {
		if (!TextUtils.isEmpty(cachedJournalContent) && ! isCachedContentDirty) {
			return cachedJournalContent;
		}
		if (journalFile == null || ! journalFile.exists() || ! journalFile.canRead()) {
			Log.w(Utils.LOG_TAG, "we can't read journal file");
			return null;
		}
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(this.journalFile));
			List<String> messages = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				messages.add(line);
			}
			for (int i = messages.size() -1; i >= 0; i--) {
				String msg = messages.get(i);
				builder.append(msg);
				builder.append("\n");
			}
		} catch (IOException e) {
			Log.e(Utils.LOG_TAG, "failed to read journal", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(Utils.LOG_TAG, "failed to close journal file reader", e);
				}
			}
		}
		final String journal = builder.toString();
		this.cachedJournalContent = journal;
		this.isCachedContentDirty = false;
		return journal;
	}
}
