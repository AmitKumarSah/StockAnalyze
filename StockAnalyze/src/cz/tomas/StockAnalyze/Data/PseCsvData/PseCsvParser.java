/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author tomas
 *
 */
public class PseCsvParser {
	
	/*
	 * known stocks - downloaded csv file contains a lot of crap, 
	 * so we need to take only stocks from SPAD
	 * (parsing for the other data is probably failing anyway)
	 */
	private final static HashSet<String> KNOWN_STOCKS = new HashSet<String>( 
		Arrays.asList(new String[] {
			"BAAAAA", "BAACETV", "BAACEZ", "BAAECM", "BAAERBAG", "BAAFOREG", "BAAKITDG","BAAKOMB", 
			"BAANWRUK", "BAAORCO", "BAAPEGAS", "BAATABAK", "BAATELEC", "BAAUNIPE", "BAAVIG", "PX"
		})
	);

	public Map<String, CsvDataRow> parse(String data) {
		String[] rows = data.split("\n");
		Map<String, CsvDataRow> dataRowList = new HashMap<String, CsvDataRow>();
		
		for (String row : rows) {
			CsvDataRow dataRow = new CsvDataRow(row);
			if (KNOWN_STOCKS.contains(dataRow.getTicker())) {
				dataRowList.put(dataRow.ticker, dataRow);
			}
		}
		return dataRowList;
	}
}
