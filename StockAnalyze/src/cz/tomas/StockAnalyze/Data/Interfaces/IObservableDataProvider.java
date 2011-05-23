/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Interfaces;

/**
 * all stock data providers should implement this interface to enable Datamanger 
 * to get notified about their update
 * @author tomas
 *
 */
public interface IObservableDataProvider {
	public void addListener(IStockDataListener listener);
}
