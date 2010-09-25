/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 *
 */
public class PseCsvParser {

	public Map<String, CsvDataRow> parse(String data) {
		String[] rows = data.split("\n");
		//List<CsvDataRow> dataRowList = new ArrayList<CsvDataRow>(rows.length);
		Map<String, CsvDataRow> dataRowList = new HashMap<String, CsvDataRow>();
		
		for (String row : rows) {
			CsvDataRow dataRow = new CsvDataRow(row);
			dataRowList.put(dataRow.ticker, dataRow);
		}
		return dataRowList;
	}
}
