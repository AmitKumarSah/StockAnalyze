package cz.tomas.StockAnalyze.Data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.tomas.StockAnalyze.Data.GaeData.UrlProvider;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.utils.DownloadService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 */
public class MarketProvider {
	
	private UrlProvider urlProvider;

	Map<String, Market> getMarkets(Context context) throws IOException {
		if (urlProvider == null) {
			urlProvider = UrlProvider.getInstance(context);
		}
		String url = urlProvider.getUrl(UrlProvider.TYPE_MDATA);
		Map<String, Market> markets;
		InputStream stream = null;
		try {
			stream = DownloadService.GetInstance().openHttpConnection(url, true);
			Gson gson = new Gson();
			Type listType = new TypeToken<List<Market>>() {}.getType();
			List<Market> marketList = gson.fromJson(new InputStreamReader(stream), listType);

			markets = new LinkedHashMap<String, Market>(marketList.size());
			for (Market market : marketList) {
				markets.put(market.getId(), market);
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		return markets;
	}
}
