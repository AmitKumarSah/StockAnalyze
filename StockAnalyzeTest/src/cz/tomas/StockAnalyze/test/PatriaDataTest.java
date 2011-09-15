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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.test.AndroidTestCase;

import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaDataItem;
import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaXmlParser;

public class PatriaDataTest extends AndroidTestCase {

	PsePatriaXmlParser parser;
	List<PsePatriaDataItem> expectedItems;
	

	private final String ADDRESS = "http://tomas-vondracek.net/Data/upload/test/ExamplePatriaData.xml";
	
	public PatriaDataTest() {
		super();
	}

	@Override
	public void setUp() throws Exception {
		//this.parser = new PsePatriaXmlParser("http://www.patria.cz/dataexport/VistaGadget.ashx?guid=D88B6094-E9C7-11DF-A5A1-05E4DED72085");
		this.parser = new PsePatriaXmlParser(ADDRESS);
		//URL url = new URL("file://data/ExamplPatriaData.xml");
		//this.parser = new PsePatriaXmlParser(url.toString());
		this.expectedItems = new ArrayList<PsePatriaDataItem>();
		
		PsePatriaDataItem itemPx = new PsePatriaDataItem();
		itemPx.setIndex(true);
		itemPx.setLink("http://www.patria.cz/akcie/.PX/px-index/graf.html");
		itemPx.setName("PX");
		itemPx.setPercentableChange(-0.63f);
		itemPx.setValue(1166.20f);
		
		this.expectedItems.add(itemPx);
		
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/AAAAsp.PR/data/online.html", 18.42f, -1.34f, "AAA"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/CETVsp.PR/data/online.html", 409.00f, -0.97f, "CETV"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/CEZPsp.PR/data/online.html", 765f, -1.67f, "ČEZ"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/ECMPsp.PR/data/online.html", 122.41f, -4.25f, "ECM"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/ERSTsp.PR/data/online.html", 822.30f, 0.46f, "ERSTE"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/FOREsp.PR/data/online.html", 102.10f, -0.39f, "Fortuna"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/BKOMsp.PR/data/online.html", 4160.00f, -0.24f, "KB"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/KITDsp.PR/data/online.html", 253.00f, 2.22f, "KITD"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/NWRSsp.PR/data/online.html", 217.00f, -1.36f, "NWR"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/ORCOsp.PR/data/online.html", 187.54f, 1.32f, "ORCO"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/PGSNsp.PR/data/online.html", 421.00f, 0.36f, "Pegas"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/TABKsp.PR/data/online.html", 10000.00f, -0.5f, "PM"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/SPTTsp.PR/data/online.html", 394.30f, -1.25f, "TEL. O2"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/UNPEsp.PR/data/online.html", 196.70f, -1.06f, "UNI"));
		this.expectedItems.add(createItem("http://www.patria.cz/akcie/VIGRsp.PR/data/online.html", 956.10f, 0.08f, "VIG"));
	}

	/**
	 * local helper method to create expected items
	 * @param link
	 * @param value
	 * @param change
	 * @param name
	 */
	private PsePatriaDataItem createItem(String link, float value, float change, String name) {
		PsePatriaDataItem item = new PsePatriaDataItem();
		item.setIndex(false);
		item.setLink(link);
		item.setName(name);
		item.setPercentableChange(change);
		item.setValue(value);
		
		return item;
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		this.parser = null;
	}

	public void testPreconditions() {
        assertNotNull(this.expectedItems);
        assertNotNull(this.parser);
    }

	public void testParse() throws Exception {
		List<PsePatriaDataItem> actualItems = null;
		actualItems = this.parser.parse();
		
		assertEquals(this.expectedItems.size(), actualItems.size());
		
		for	(int i = 0; i < actualItems.size(); i++) {
			assertEquals(this.expectedItems.get(i), actualItems.get(i));
		}
	}
	
	/*
	 * test xml conf part - containing market phase and date
	 */
	public void testParseConf() throws Exception {
		List<PsePatriaDataItem> actualItems = this.parser.parse();
				
		assertEquals(true, this.parser.isClosePhase());
		assertEquals(10, this.parser.getXmlRefreshInterval());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2010);
		cal.set(Calendar.MONTH, Calendar.NOVEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Calendar actual = this.parser.getDate();
		assertEquals(cal.getTimeInMillis(), actual.getTimeInMillis());
		TimeZone tz = TimeZone.getTimeZone("Europe/Prague");
		assertEquals(tz.getID(), actual.getTimeZone().getID());
	}
}
