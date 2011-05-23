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
