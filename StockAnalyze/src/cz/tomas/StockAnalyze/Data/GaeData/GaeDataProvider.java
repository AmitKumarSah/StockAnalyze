package cz.tomas.StockAnalyze.Data.GaeData;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GaeDataProvider {

	private static final String[] TIME_PERIODS = { "", "1D", "1W", "1M", "3M", "6M", "1Y" };
	
	private Gson gson;
	private UrlProvider urls;
	
	GaeDataProvider(Context context) {
		this.gson = new Gson();
		this.urls = UrlProvider.getInstance(context);
	}

	/**
	 * get day data for whole market
	 * 
	 * @param countryCode e.g. "cz" or "de"
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	Map<String, DayData> getDayDataSet(String countryCode) throws JsonSyntaxException, IOException {
		if (TextUtils.isEmpty(countryCode)) {
			throw new NullPointerException("country code can't be empty!");
		}
		String baseUrl = this.urls.getUrl(UrlProvider.TYPE_DDATA, UrlProvider.ARG_MARKET);
		String url = String.format(baseUrl, countryCode);
		
		Map<String, DayData> data = getDataSet(url);
		return data;
	}

	/**
	 * generic method to get {@link DayData} for list of equities
	 * 
	 * @param url url to connect to
	 * @return daydata mapped to stock ids
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	protected Map<String, DayData> getDataSet(String url) throws IOException,
			JsonSyntaxException {
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "connecting to " + url);
		Map<String, DayData> data = new LinkedHashMap<String, DayData>();
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			try {
				Type listType = new TypeToken<Map<String, DayData>>() {}.getType();
				data = gson.fromJson(new InputStreamReader(stream, "UTF-8"), listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse data from " + url, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}

	/**
	 * get last available data for given {@link StockItem#getTicker()}
	 * @param ticker
	 * @return
	 * @throws IOException
	 */
	DayData getLastData(String ticker) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock can't be empty!");
		}
		String baseUrl = this.urls.getUrl(UrlProvider.TYPE_DDATA, UrlProvider.ARG_STOCK);
		String url = String.format(baseUrl, ticker);
		
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "connecting to " + url);
		DayData data;
		
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			try {
				Type listType = new TypeToken<DayData>() {}.getType();
				data = gson.fromJson(new InputStreamReader(stream, "UTF-8"), listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse data from " + url, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}

	/**
	 * get day data for indeces 
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	Map<String, DayData> getIndecesDataSet() throws JsonSyntaxException, IOException {
		final String url = this.urls.getUrl(UrlProvider.TYPE_INDATA, (String) null);
		return getDataSet(url);
	}
	
	/**
	 * get list of indeces, in other words, all stocks from "GLOBAL" market.
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	List<StockItem> getIndecesList() throws JsonSyntaxException, IOException {
		String url = this.urls.getUrl(UrlProvider.TYPE_INDATA, UrlProvider.ARG_IND_LIST);
		url = String.format(url, "");
		return getList(url);
	}
	
	/**
	 * get list of stocks for given country code
	 * @param countryCode e.g. "cz", "de"
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	List<StockItem> getStockList(String countryCode) throws JsonSyntaxException, IOException {
		if (TextUtils.isEmpty(countryCode)) {
			throw new NullPointerException("country code can't be empty!");
		}
		final String baseUrl = this.urls.getUrl(UrlProvider.TYPE_DDATA, UrlProvider.ARG_LIST);
		String url = String.format(baseUrl, countryCode);
		
		return getList(url);
	}

	/**
	 * get equity list - stocks or indeces, depends on url
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	protected List<StockItem> getList(String url) throws IOException,
			JsonSyntaxException {
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "connecting to " + url);
		List<StockItem> data;
		
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			try {
				Type listType = new TypeToken<List<StockItem>>() {}.getType();
				data = gson.fromJson(new InputStreamReader(stream, "UTF-8"), listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse data from " + url, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}
	
	Map<Long, Float> getHistoricalData(String stockTicker, int timePeriod) throws IOException {
		if (TextUtils.isEmpty(stockTicker)) {
			throw new NullPointerException("stock ticker can't be empty!");
		}
		
		String baseUrl = this.urls.getUrl(UrlProvider.TYPE_HDATA, UrlProvider.ARG_STOCK, UrlProvider.ARG_TIME);
		String urlString = String.format(baseUrl, stockTicker, TIME_PERIODS[timePeriod]);
		
		return getTextData(urlString);
	}

	Map<Long, Float> getIntraDayData(String ticker) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock ticker can't be null!");
		}
		String baseUrl = this.urls.getUrl(UrlProvider.TYPE_IDATA, UrlProvider.ARG_STOCK);
		String urlString = String.format(baseUrl, URLEncoder.encode(ticker));
		return getTextData(urlString);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	private Map<Long, Float> getTextData(String url) throws IOException,
			JsonSyntaxException {
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "connecting to " + url);
		Map<Long, Float> data = new LinkedHashMap<Long, Float>();
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			try {
				Type listType = new TypeToken<Map<Long, Float>>() {}.getType();
				data = gson.fromJson(new InputStreamReader(stream, "UTF-8"), listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse from " + url, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}

	public boolean refresh() {
		
		return true;
	}
}
