package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * provider for URLs for remote backend
 * @author tomas
 *
 */
public final class UrlProvider {
		
	static final String ARG_MARKET = "marketCode";
	static final String ARG_LIST = "stockList";
	static final String ARG_IND_LIST = "indList";
	static final String ARG_STOCK = "stockId";
	static final String ARG_TIME = "timePeriod";
	
	/**
	 * day data
	 */
	static final String TYPE_DDATA = "DDATA";
	/**
	 * intraday chart data
	 */
	static final String TYPE_IDATA = "IDATA";
	/**
	 * historical chart data
	 */
	static final String TYPE_HDATA = "HDATA";
	/**
	 * indeces data
	 */
	static final String TYPE_INDATA = "INDATA";
	
	private static final String FILE_CACHE = "gae-url.list";
	
	private static final String URL_CROSSROAD = "http://stockanalyzeserverenv-upk2bxu5a5.elasticbeanstalk.com/Crossroads";
	private static final String URL_CROSSROAD_BACKUP = "http://backend-stockanalyze.appspot.com/Crossroads";
	
	private static final long MAX_URL_VALID_TIME = 24L * 60L * 60L * 1000L;	// a day in ms

	private Gson gson;
	private Map<String, String> urls;
	private StringBuilder builder;
	
	private static UrlProvider instance;
	private Context context;

	private SharedPreferences pref;

	private final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
	
	public static UrlProvider getInstance(Context context) {
		if (instance == null) {
			instance = new UrlProvider(context);
		}
		return instance;
	}
	
	private UrlProvider(Context context) {
		this.gson = new Gson();
		this.context = context;
		this.pref = this.context.getSharedPreferences(Utils.PREF_NAME, 0);
		
		File cacheFile = getCacheFile();
		this.urls = readFromCache(cacheFile);
	}

	private File getCacheFile() {
		File cacheDir = this.context.getCacheDir();
		File[] files = cacheDir.listFiles();
		File cacheFile = null;
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (FILE_CACHE.equals(file.getName())) {
					cacheFile = file;
					break;
				}
			}
		} else {
			cacheFile = new File(cacheDir, FILE_CACHE);
		}
		return cacheFile;
	}

	/**
	 * read url map from cache file
	 * @param file
	 * @return
	 */
	private Map<String, String> readFromCache(File file) {
		if (file == null || ! file.exists()) {
			return null;
		}
		Reader reader = null;
		try {
			reader = new FileReader(file);
			return this.gson.fromJson(reader, this.mapType);
		} catch (IOException e) {
			Log.e(Utils.LOG_TAG, "failed to access cache file", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(Utils.LOG_TAG, "failed to close file reader", e);
				}
			}
		}
		return null;
	}
	
	/**
	 * Get url for given service type appended with all given arguments.
	 * 
	 * @param type see {@link #TYPE_DDATA}, {@link #TYPE_HDATA}, {@link #TYPE_IDATA}, {@link #TYPE_INDATA}
	 * @param args
	 * @return url ready to be formatted in {@link String#format(String, Object...)} with arguments values
	 */
	public synchronized String getUrl(String type, String... args) {
		final long diff = System.currentTimeMillis() - this.pref.getLong(Utils.PREF_URLS_TIME, 0);

		if (this.urls == null || this.urls.size() == 0 || 
				diff > MAX_URL_VALID_TIME) {
			this.downloadUrls();
		}
		String url = this.urls.get(type);
		if (! TextUtils.isEmpty(url) && args != null && args.length > 0) {
			if (this.builder == null) {
				this.builder = new StringBuilder("?");
			} else {
				this.builder.setLength(0);
				this.builder.append("?");
			}
			
			for (String arg : args) {
				if (! TextUtils.isEmpty(arg)) {
					if (builder.length() > 1) {
						builder.append('&');
					}
					builder.append(String.format("%s=%%s", arg));
				}
			}
			url += builder.toString();
		}
		return url;
	}

	/**
	 * ask {@link #URL_CROSSROAD} or {@link #URL_CROSSROAD_BACKUP} 
	 * for URLs to get data from
	 */
	private void downloadUrls() {
		try {
			String content = null;
			try {
				byte[] bytes = DownloadService.GetInstance().DownloadFromUrl(URL_CROSSROAD, false);
				content = new String(bytes, "UTF-8");
			} catch (Exception e) {
				Log.w(Utils.LOG_TAG, "failed to get URLs from primary crossroad, trying backup");
				byte[] bytes = DownloadService.GetInstance().DownloadFromUrl(URL_CROSSROAD_BACKUP, false);
				content = new String(bytes, "UTF-8");
			}
			Map<String, String> downloadedUrls = gson.fromJson(content, this.mapType);
			if (downloadedUrls != null) {
				this.cacheData(content);
				this.urls = downloadedUrls;
			}
		} catch (IOException e) {
			Log.e(Utils.LOG_TAG, "failed to get urls from crossroad");
		}
	}

	/**
	 * save url map to cache file and
	 * save the time to preferences
	 * so we know when the cached file is outdated
	 * @param content
	 */
	private void cacheData(String content) {
		File cacheFile = this.getCacheFile();
		Writer writer = null;
		try {
			writer = new FileWriter(cacheFile);
			writer.write(content);
			writer.flush();
			Editor edit = this.pref.edit();
			edit.putLong(Utils.PREF_URLS_TIME, System.currentTimeMillis());
			edit.commit();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to access cache file", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					Log.e(Utils.LOG_TAG, "failed to close file writer", e);
				}
			}
		}
	}
}
