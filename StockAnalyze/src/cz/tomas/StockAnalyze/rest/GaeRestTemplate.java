package cz.tomas.StockAnalyze.rest;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author tomas
 */
public class GaeRestTemplate extends RestTemplate {

	public GaeRestTemplate(Type responseType, ResponseErrorHandler errorHandler, List<ClientHttpRequestInterceptor> interceptors) {
		this.setErrorHandler(errorHandler);
		this.setInterceptors(interceptors);

		final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
		if (responseType != null) {
			converter.setType(responseType);
		}

		this.setMessageConverters(Collections.<HttpMessageConverter<?>>singletonList(converter));
	}
}
