/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 */
public class PsePatriaDataProvider implements IStockDataProvider {
	
	@Override
	public DayData getLastData(String ticker) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DayData[] getIntraDayData(String ticker, Date date,
			int minuteInterval) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StockItem> getAvailableStockList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescriptiveName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		return false;
	}

}
