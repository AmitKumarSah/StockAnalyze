package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GaeTextProcessor {

	static Map<Long, Float> process(InputStream stream) {
		Map<Long, Float> result = new LinkedHashMap<Long, Float>();
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(stream));
		
		//reader
		
		return result;
	}
}
