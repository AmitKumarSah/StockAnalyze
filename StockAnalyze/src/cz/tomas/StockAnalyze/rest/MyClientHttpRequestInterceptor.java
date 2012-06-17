package cz.tomas.StockAnalyze.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.Utils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.List;

/**
 * @author tomas
 */
public class MyClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final String AUTH_COOKIE_NAME = "ac";
	private static final String SET_COOKIE = "set-cookie";
	private static final String COOKIE = "Cookie";

	private final SharedPreferences pref;
	private String cookie;
	private final ResponseErrorHandler errorHandler;

	MyClientHttpRequestInterceptor(Context context, ResponseErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.pref = context.getSharedPreferences(Utils.PREF_NAME, 0);
		this.cookie = this.pref.getString(Utils.PREF_COOKIE, null);
	}

	/* (non-Javadoc)
	 * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[], org.springframework.http.client.ClientHttpRequestExecution)
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] byteArray,
	                                    ClientHttpRequestExecution execution) throws IOException {

		List<String> cookies = request.getHeaders().get(AUTH_COOKIE_NAME);
		// if the header doesn't exist, add any existing, saved cookies
		if (cookies == null) {
			// if we have stored cookie, add them to the headers
			if (this.cookie != null) {
				request.getHeaders().add(COOKIE, cookie);
			}
		}
		// execute the request
		ClientHttpResponse response = execution.execute(request, byteArray);

		if (! errorHandler.hasError(response)) {
			// pull auth cookie off and store them
			cookies = response.getHeaders().get(SET_COOKIE);
			if (cookies != null) {
				for (String cookie : cookies) {
					if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "response cookie = " + cookie);
					String[] keyValue = cookie.split("=");

					if (keyValue != null && keyValue.length == 2 && AUTH_COOKIE_NAME.equals(keyValue[0])) {
						this.cookie = cookie;
						this.pref.edit().putString(Utils.PREF_COOKIE, cookie).commit();
					}
				}
			}
		}
		return response;
	}

}