/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PseCsvData;

/**
 * model class for csv file downloaded from ftp.pse.cz
 * 
 * @author tomas
 *
 */
public class CsvDataRow {

	String code;
	String name;
	String ticker;
	String date;
	String closePrice;
	String openPrice;
	String dayVolume;
	String change;
	String tradedPieces;
	String market;
	String yearMax;
	String yearMin;
	
	public CsvDataRow(String row) {
		this.parseRow(row);
	}
	
	private void parseRow(String row) {
		row = row.replaceAll("\"", "");
		row = row.replaceAll("\t", "");
		row = row.replaceAll(" ", "");
		String[] cells = row.split(",");
		this.code = cells[0];
		this.name = cells[1];
		this.ticker = cells[2];
		this.date = cells[3];
		this.closePrice = cells[4];
		this.change = cells[5];
		if (this.change.startsWith("."))
			this.change = "0" + this.change;
		this.openPrice = cells[6];
		this.yearMin = cells[7];
		this.yearMax = cells[8];
		this.tradedPieces = cells[9];
		this.dayVolume = cells[10];
	}
	
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getTicker() {
		return ticker;
	}
	public String getDate() {
		return date;
	}
	public String getClosePrice() {
		return closePrice;
	}
	public String getOpenPrice() {
		return openPrice;
	}
	public String getDayVolume() {
		return dayVolume;
	}
	public String getChange() {
		return change;
	}
	public String getTradedPieces() {
		return tradedPieces;
	}
	public String getMarket() {
		return market;
	}
	public String getYearMax() {
		return yearMax;
	}
	public String getYearMin() {
		return yearMin;
	}

	@Override
	public String toString() {
		return "CsvDataRow [change=" + change + ", closePrice=" + closePrice
				+ ", code=" + code + ", date=" + date + ", dayVolume="
				+ dayVolume + ", market=" + market + ", name=" + name
				+ ", openPrice=" + openPrice + ", ticker=" + ticker
				+ ", tradedPieces=" + tradedPieces + "]";
	}
	
	
}
