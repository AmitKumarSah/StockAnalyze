package cz.tomas.StockAnalyze.rest;

import com.google.gson.reflect.TypeToken;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * infrastructure for executing REST requests
 *
 * @author tomas
 */
@SuppressWarnings("unchecked")
public class Infrastructure {

	private static final String ROOT_URL = "https://backend-stockanalyze.appspot.com/";

	private final HttpEntity<?> requestEntity;

	private final RestTemplate stocksTemplate;
	private final RestTemplate chartTemplate;
	private final RestTemplate dayDataTemplate;

	private final StringBuilder builder;

	public Infrastructure() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);

		requestEntity = new HttpEntity<Object>(requestHeaders);

		stocksTemplate = new RestTemplate();
		stocksTemplate.setMessageConverters(Collections.<HttpMessageConverter<?>>
				singletonList(new GsonHttpMessageConverter()));

		chartTemplate = new RestTemplate();
		final GsonHttpMessageConverter chartConverter = new GsonHttpMessageConverter();
		Type chartDataType = new TypeToken<Map<Long, Float>>() {}.getType();
		chartConverter.setType(chartDataType);
		chartTemplate.setMessageConverters(Collections.<HttpMessageConverter<?>>
				singletonList(chartConverter));

		dayDataTemplate = new RestTemplate();
		final GsonHttpMessageConverter dayDataConverter = new GsonHttpMessageConverter();
		Type datasetType = new TypeToken<Map<String, DayData>>() {}.getType();
		dayDataConverter.setType(datasetType);
		dayDataTemplate.setMessageConverters(Collections.<HttpMessageConverter<?>>
				singletonList(dayDataConverter));

		this.builder = new StringBuilder();
	}

	public Collection<StockItem> getStockList(Market market) throws IOException {
		final String url = ROOT_URL.concat("stocks?marketId=".concat(market.getId()));
		ResponseEntity<StockItem[]> entity = stocksTemplate.exchange(url, HttpMethod.GET, this.requestEntity,
				StockItem[].class);
		return Arrays.asList(entity.getBody());
	}

	public StockItem getStock(String ticker) throws IOException {
		final String url = ROOT_URL.concat("stocks/{ticker}");
		ResponseEntity<StockItem> entity = stocksTemplate.exchange(url, HttpMethod.GET, this.requestEntity,
				StockItem.class, Collections.singletonMap("ticker", ticker));
		return entity.getBody();
	}

	public Map<Long, Float> getChartData(String ticker, String timePeriod) throws IOException {
		final String url = ROOT_URL.concat("stocks/{ticker}/chart?timePeriod=").concat(timePeriod);
		ResponseEntity<Map> data = chartTemplate.exchange(url, HttpMethod.GET, this.requestEntity, Map.class,
				Collections.singletonMap("ticker", ticker));
		return (Map<Long, Float>) data.getBody();
	}

	public Map<String, DayData> getDataSet(Market market) throws IOException {
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
}
