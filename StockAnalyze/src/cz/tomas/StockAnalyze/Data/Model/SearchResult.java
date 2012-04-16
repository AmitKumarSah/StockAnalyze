package cz.tomas.StockAnalyze.Data.Model;

/**
 * @author tomas
 */
public class SearchResult {

	private String symbol;
	private String name;
	private String exch;
	private String exchDisp;
	private String type;

	public String getExch() {
		return exch;
	}

	public String getExchDisp() {
		return exchDisp;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getType() {
		return type;
	}
}
