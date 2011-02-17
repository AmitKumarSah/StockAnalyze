/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Interfaces;

/**
 * listener for events from List Adapters
 * @author tomas
 *
 */
public interface IListAdapterListener<T> {
	
	/*
	 * adapter have begun to load items 
	 */
	void onListLoading();
	
	/*
	 * all items are loaded to the adapter
	 */
	void onListLoaded(T data);
}
