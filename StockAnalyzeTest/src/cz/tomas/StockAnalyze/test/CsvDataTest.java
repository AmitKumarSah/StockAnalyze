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
package cz.tomas.StockAnalyze.test;

import java.io.*;

import android.test.AndroidTestCase;

import cz.tomas.StockAnalyze.Data.PseCsvData.CsvDataRow;

public class CsvDataTest extends AndroidTestCase {
	String row;
	String content;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		row = "\"US4824702009\",\"KITD              \",\"BAAKITDG\",\"2010/09/24\",206.00         ,.59      ,204.80         ,163.50         ,290.00         ,3753         ,767451.80          ,\"2010/09/24\",\"3\",\"2\",\"A\"";

		File file = new File("data/ExampleData.csv");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {

				// this statement reads the line from the file and print it to
				// the console.
				this.content += (dis.readLine()) + "\n";
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	public void testCsvDataRow() {
		CsvDataRow dataRow = new CsvDataRow(row);

		System.out.println(dataRow.toString());

		assertEquals("US4824702009", dataRow.getCode());
		assertEquals("BAAKITDG", dataRow.getTicker());
		assertEquals("KITD", dataRow.getName());
		assertEquals("206.00", dataRow.getClosePrice());
		assertEquals("0.59", dataRow.getChange());
		assertEquals("204.80", dataRow.getOpenPrice());
		assertEquals("3753", dataRow.getTradedPieces());
		assertEquals("767451.80", dataRow.getDayVolume());
	}
	
//	public void testCsvRowCount() {
//		assertNotNull(this.content);
//		
//		PseCsvParser parser = new PseCsvParser();
//		
//		Map<String, CsvDataRow> rows = parser.parse(this.content);
//		
//		assertEquals("Actual row size is " + rows.size(), 217, 	rows.entrySet().size());
//	}

}
