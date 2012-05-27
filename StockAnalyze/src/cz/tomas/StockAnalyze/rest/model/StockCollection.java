package cz.tomas.StockAnalyze.rest.model;

import cz.tomas.StockAnalyze.Data.Model.StockItem;

import java.util.Collection;

/**
 * @author tomas
 */
public class StockCollection {

	private Collection<StockItem> stocks;

	public Collection<StockItem> getStocks() {
		return stocks;
	}
}
