/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
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
			"BAANWR", "BAAORCO", "BAAPEGAS", "BAATABAK", "BAATELEC", "BAAUNIPE", "BAAVIG", "PX"
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
