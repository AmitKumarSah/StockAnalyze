package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
	
//	private static final String URL_HDATA 			= "http://backend-stockanalyze.appspot.com/HData?stockId=%s&timePeriod=%s";
//	private static final String URL_IDATA			= "http://backend-stockanalyze.appspot.com/IData?stockId=%s";
//	private static final String URL_DDATA 			= "http://backend-stockanalyze.appspot.com/DData?stockId=%s";
//	private static final String URL_DDATA_MARKET 	= "http://backend-stockanalyze.appspot.com/DData?marketCode=%s";
//	private static final String URL_LIST 			= "http://backend-stockanalyze.appspot.com/DData?stockList=%s";
//	private static final String URL_INDECES_LIST	= "http://backend-stockanalyze.appspot.com/IndData?indList";
//	private static final String URL_INDECES_SET		= "http://backend-stockanalyze.appspot.com/IndData";
	
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
	
	private static final String URL_CROSSROAD = "http://stockanalyzeserverenv-upk2bxu5a5.elasticbeanstalk.com/Crossroads";
	private static final String URL_CROSSROAD_BACKUP = "http://backend-stockanalyze.appspot.com/Crossroads";	

	private Gson gson;
	private Map<String, String> urls;
	private StringBuilder builder;
	
	private static UrlProvider instance;
	
	public static UrlProvider getInstance() {
		if (instance == null) {
			instance = new UrlProvider();
		}
		return instance;
	}
	
	private UrlProvider() {
		this.urls = new HashMap<String, String>();
		this.gson = new Gson();
	}
	
	public synchronized String getUrl(String type, String... args) {
		if (this.urls == null || this.urls.size() == 0) {
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
	 * to URLs to get data from
	 */
	private void downloadUrls() {
		try {
			InputStream stream = null;
			try {
				stream = DownloadService.GetInstance().openHttpConnection(URL_CROSSROAD, false);
			} catch (Exception e) {
				Log.w(Utils.LOG_TAG, "failed to get URLs from priamry crossroad, trying backup");
				stream = DownloadService.GetInstance().openHttpConnection(URL_CROSSROAD_BACKUP, false);
			}
			
			Type listType = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> downloadedUrls = gson.fromJson(new InputStreamReader(stream), listType);
			this.urls = downloadedUrls;
		} catch (IOException e) {
			Log.e(Utils.LOG_TAG, "failed to get urls from crossroad");
		}
	}
}
