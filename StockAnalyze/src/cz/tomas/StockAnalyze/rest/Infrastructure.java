package cz.tomas.StockAnalyze.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * infrastructure for executing REST requests
 *
 * @author tomas
 */
@SuppressWarnings("unchecked")
public class Infrastructure {

	private static final String ROOT_URL = "https://backend-stockanalyze.appspot.com/";
//	private static final String ROOT_URL = "http://10.0.2.2:8888";

	private final HttpEntity<?> requestEntity;

	private final RestTemplate stocksTemplate;
	private final RestTemplate chartTemplate;
	private final RestTemplate dayDataTemplate;

	private final StringBuilder builder;
	private final List<ClientHttpRequestInterceptor> interceptors;
	private final String fingerprint;
	private final SharedPreferences preferences;

	public Infrastructure(Context context) {
		this.preferences = context.getSharedPreferences(Utils.PREF_NAME, 0);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);

		final ErrorHandler errorHandler = new ErrorHandler(this.preferences);

		interceptors = new ArrayList<ClientHttpRequestInterceptor>(1);
		interceptors.add(new MyClientHttpRequestInterceptor(context, errorHandler));

		requestEntity = new HttpEntity<Object>(requestHeaders);


		stocksTemplate = new GaeRestTemplate(null, errorHandler, this.interceptors);

		Type chartDataType = new TypeToken<Map<Long, Float>>() {}.getType();
		chartTemplate = new GaeRestTemplate(chartDataType, errorHandler, this.interceptors);

		Type datasetType = new TypeToken<Map<String, DayData>>() {}.getType();
		dayDataTemplate = new GaeRestTemplate(datasetType, errorHandler, this.interceptors);

		this.builder = new StringBuilder();
		this.fingerprint = this.readCertificate(context);
	}

	public Collection<StockItem> getStockList(Market market) throws IOException {
		checkIsSignIn();
		final String url = ROOT_URL.concat("stocks?marketId=".concat(market.getId()));
		ResponseEntity<StockItem[]> entity = stocksTemplate.exchange(url, HttpMethod.GET, this.requestEntity,
				StockItem[].class);
		return Arrays.asList(entity.getBody());
	}

	public StockItem getStock(String ticker) throws IOException {
		checkIsSignIn();
		final String url = ROOT_URL.concat("stocks/{ticker}");
		ResponseEntity<StockItem> entity = stocksTemplate.exchange(url, HttpMethod.GET, this.requestEntity,
				StockItem.class, Collections.singletonMap("ticker", ticker));
		return entity.getBody();
	}

	public Map<Long, Float> getChartData(String ticker, String timePeriod) throws IOException {
		checkIsSignIn();
		final String url = ROOT_URL.concat("stocks/{ticker}/chart?timePeriod=").concat(timePeriod);
		ResponseEntity<Map> data = chartTemplate.exchange(url, HttpMethod.GET, this.requestEntity, Map.class,
				Collections.singletonMap("ticker", ticker));
		return (Map<Long, Float>) data.getBody();
	}

	public Map<String, DayData> getDataSet(Market market) throws IOException {
		checkIsSignIn();
		final String url = ROOT_URL.concat("dayData?marketId=").concat(market.getId());
		ResponseEntity<Map> data = dayDataTemplate.exchange(url, HttpMethod.GET, this.requestEntity, Map.class);

		return (Map<String, DayData>) data.getBody();
	}

	public Map<String, DayData> getDataSet(Collection<StockItem> stocks) {
		final String params;
		synchronized (builder) {
			builder.setLength(0);
			for (StockItem stock : stocks) {
				builder.append(stock.getTicker()).append(",");
			}
			if (builder.length() > 0) {
				builder.setLength(builder.length() - 1);
			}
			params = builder.toString();
		}
		final String url = ROOT_URL.concat("dayData?stockList=").concat(params);
		ResponseEntity<Map> data = dayDataTemplate.exchange(url, HttpMethod.GET, this.requestEntity, Map.class);

		return (Map<String, DayData>) data.getBody();
	}

	private synchronized void checkIsSignIn() {
		String cookie = this.preferences.getString(Utils.PREF_COOKIE, null);
		if (TextUtils.isEmpty(cookie)) {
			postForSignIn();
		}
	}

	private void postForSignIn() {
		RestTemplate signInTemplate = new RestTemplate();
		signInTemplate.setInterceptors(interceptors);
		final String url = ROOT_URL.concat("signIn");
		String stringEntity = String.format("client=%s", this.fingerprint);
		HttpEntity<?> entity = new HttpEntity<Object>(stringEntity);

		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "signing in...");
		signInTemplate.postForObject(url, entity, String.class);
	}


	private String readCertificate(Context context) {
		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();
		int flags = PackageManager.GET_SIGNATURES;

		PackageInfo packageInfo;

		try {
			packageInfo = pm.getPackageInfo(packageName, flags);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(Utils.LOG_TAG, "we haven't found ourselves", e);
			// that would be weird...
			return null;
		}
		Signature[] signatures = packageInfo.signatures;

		byte[] cert = signatures[0].toByteArray();

		InputStream input = new ByteArrayInputStream(cert);

		CertificateFactory cf;
		X509Certificate c;
		try {
			cf = CertificateFactory.getInstance("X509");
			c = (X509Certificate) cf.generateCertificate(input);
		} catch (CertificateException e) {
			Log.e(Utils.LOG_TAG, "failed to process certificate", e);
			return packageName;
		}

		final StringBuilder hexString = Utils.certToString(c);
		return hexString.toString();
	}

	private static final class ErrorHandler extends DefaultResponseErrorHandler {

		private final SharedPreferences pref;

		ErrorHandler(SharedPreferences pref) {
			this.pref = pref;
		}

		@Override
		public boolean hasError(ClientHttpResponse response) throws IOException {
			return super.hasError(response);
		}

		@Override
		public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
			// clear our cookie in case of 401
			final int status = clientHttpResponse.getStatusCode().value();
			if (status == HttpStatus.UNAUTHORIZED.value() ||
					status == HttpStatus.FORBIDDEN.value()) {
				if (Utils.DEBUG) Log.w(Utils.LOG_TAG, "clearing ac, because of 401");
				this.pref.edit().putString(Utils.PREF_COOKIE, null).commit();
			}
			super.handleError(clientHttpResponse);
		}
	}
}
