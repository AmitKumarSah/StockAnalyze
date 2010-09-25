package cz.tomas.StockAnalyze.test;

import java.io.*;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.PseCsvData.CsvDataRow;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataProvider;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvParser;
import junit.framework.TestCase;

public class CsvDataTest extends TestCase {
	String row;
	String content;

	protected void setUp() throws Exception {
		super.setUp();

		row = "\"US4824702009\",\"KITD              \",\"BAAKITDG\",\"2010/09/24\",206.00         ,.59      ,204.80         ,163.50         ,290.00         ,3753         ,767451.80          ,\"2010/09/24\",\"3\",\"2\",\"A\"";

		File file = new File("ExampleData.csv");
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
		//TODO other fields
	}
	
	public void testCsvRowCount() {
		assertNotNull(this.content);
		
		PseCsvParser parser = new PseCsvParser();
		
		Map<String, CsvDataRow> rows = parser.parse(this.content);
		
		assertEquals(217, rows.size());
	}

}
