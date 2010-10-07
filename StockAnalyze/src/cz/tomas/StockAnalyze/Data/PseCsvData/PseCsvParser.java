/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tomas
 *
 */
public class PseCsvParser {

	public Map<String, CsvDataRow> parse(String data) {
		String[] rows = data.split("\n");
		Map<String, CsvDataRow> dataRowList = new HashMap<String, CsvDataRow>();
		
		for (String row : rows) {
			CsvDataRow dataRow = new CsvDataRow(row);
			dataRowList.put(dataRow.ticker, dataRow);
		}
		return dataRowList;
	}
}
