package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;

public final class GaeDataProvider {

	private static final String URL_HDATA = "http://backend-stockanalyze.appspot.com/HData?stockId=%s&timePeriod=%s";
	private static final String URL_IDATA = "http://backend-stockanalyze.appspot.com/IData?stockId=%s";
	private static final String URL_DDATA = "http://backend-stockanalyze.appspot.com/DData?stockId=%s";
	private static final String URL_LIST = "http://backend-stockanalyze.appspot.com/DData?stockList=%s";
	
	private static final String[] TIME_PERIODS = { "", "1D", "1W", "1M", "3M", "6M", "1Y" };
	
	private Gson gson;
	
	GaeDataProvider() {
		this.gson = new Gson();
	}
	

	public DayData getLastData(String ticker) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock can't be empty!");
		}
		String url = String.format(URL_DDATA, ticker);
		
		Log.d(Utils.LOG_TAG, "connecting to " + url);
		DayData data;
		
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			String content = null;
			//this.builder.setLength(0);
			try {
				content = readStream(stream);
				Type listType = new TypeToken<DayData>() {}.getType();
				data = gson.fromJson(content, listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse " + content, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}

	public List<StockItem> getStockList(String countryCode) throws JsonSyntaxException, IOException {
		if (TextUtils.isEmpty(countryCode)) {
			throw new NullPointerException("stock can't be empty!");
		}
		String url = String.format(URL_LIST, countryCode);
		
		Log.d(Utils.LOG_TAG, "connecting to " + url);
		List<StockItem> data;
		
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			String content = null;
			//this.builder.setLength(0);
			try {
				content = readStream(stream);
				Type listType = new TypeToken<List<StockItem>>() {}.getType();
				data = gson.fromJson(content, listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse " + content, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}
	
	public Map<Long, Float> getHistoricalData(String stockTicker, int timePeriod) throws IOException {
		if (TextUtils.isEmpty(stockTicker)) {
			throw new NullPointerException("stock ticker can't be empty!");
		}
		
		String urlString = String.format(URL_HDATA, stockTicker, TIME_PERIODS[timePeriod]);
		
		return getTextData(urlString);
	}

	public Map<Long, Float> getIntraDayData(String ticker) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock can't be null!");
		}
		
		String urlString = String.format(URL_IDATA, ticker);
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
		Log.d(Utils.LOG_TAG, "connecting to " + url);
		Map<Long, Float> data = new LinkedHashMap<Long, Float>();
		
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			String content = null;
			//this.builder.setLength(0);
			try {
				content = readStream(stream);
				//content = content.substring(content.indexOf("\n"));
				//reader = new InputStreamReader(stream, "UTF-8");
				Type listType = new TypeToken<Map<Long, Float>>() {}.getType();
				data = gson.fromJson(content, listType);
			} catch (IOException ex) {
				Log.e(Utils.LOG_TAG, "failed to parse " + content, ex);
				throw ex;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}
	
	private static String readStream(InputStream is) throws IOException {
        if (is != null) {
	        Writer writer = new StringWriter();
	
	        char[] buffer = new char[1024];
	        try {
	            Reader reader = new BufferedReader(new InputStreamReader(is));
	            int n;
	            while ((n = reader.read(buffer)) != -1) {
	                writer.write(buffer, 0, n);
	            }
	        } finally {
	            is.close();
	        }
	        return writer.toString();
	    } else {        
	        return "";
	    }
	}


	public boolean refresh() {
		
		return true;
	}
}
