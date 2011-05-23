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
package cz.tomas.StockAnalyze.Data.Interfaces;

import java.util.Map;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

public interface IStockDataListener {
	void OnStockDataUpdated(IStockDataProvider sender, Map<StockItem,DayData> dataMap);
	void OnStockDataUpdateBegin(IStockDataProvider sender);
	void OnStockDataNoUpdate(IStockDataProvider sender);
}
