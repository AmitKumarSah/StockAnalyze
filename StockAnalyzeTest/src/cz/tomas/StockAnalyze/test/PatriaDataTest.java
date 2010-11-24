package cz.tomas.StockAnalyze.test;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaDataItem;
import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaXmlParser;

public class PatriaDataTest {

	PsePatriaXmlParser parser;
	List<PsePatriaDataItem> expectedItems;
	
	@Before
	public void setUp() throws Exception {
		this.parser = new PsePatriaXmlParser("http://www.patria.cz/dataexport/VistaGadget.ashx?guid=D88B6094-E9C7-11DF-A5A1-05E4DED72085");
		URL url = new URL("file://ExamplPatriaData.xml");
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

	@Test
	public void testParse() {
		List<PsePatriaDataItem> actualItems = null;
		try {
			actualItems = this.parser.parse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
		assertEquals(this.expectedItems.size(), actualItems.size());
		
		for	(int i = 0; i < actualItems.size(); i++) {
			assertEquals(this.expectedItems.get(i), actualItems.get(i));
		}
	}
	
	@Test
	public void testParseConf() {
		try {
			List<PsePatriaDataItem> actualItems = this.parser.parse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			assertTrue(false);
			e.printStackTrace();
		}
		
		assertEquals(true, this.parser.isClosePhase());
		assertEquals(10, this.parser.getXmlRefreshInterval());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2010);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		assertEquals(cal.getTimeInMillis(), cal.getTimeInMillis());
		TimeZone tz = TimeZone.getTimeZone("Europe/Prague");
		assertEquals(tz, cal.getTimeZone());
	}
}