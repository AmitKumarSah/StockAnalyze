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
 * all stock data providers should implement this interface to enable Datamanger 
 * to get notified about their update
 * @author tomas
 *
 */
public interface IObservableDataProvider {
	public void addListener(IStockDataListener listener);
}
