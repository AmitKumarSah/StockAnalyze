/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Interfaces;

import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.Model.Market;

/**
 * listener for {@link UpdateScheduler} glonal application updates 
 * 
 * @author tomas
 *
 */
public interface IUpdateSchedulerListener {

	void onUpdateBegin(Market... markets);
	void onUpdateFinished(boolean succes);
}
