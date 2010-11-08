/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cz.tomas.StockAnalyze.Data.DownloadService;

import android.util.Log;

/**
 * @author tomas
 *
 */
public class PsePatriaXmlParser {
	
	String url;
	boolean isClosePhase;
	int xmlRefreshInterval;
	Calendar date;
	
	public PsePatriaXmlParser(String url) {
		this.url = url;
	}
	
	public List<PsePatriaDataItem> parse() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<PsePatriaDataItem> items = new ArrayList<PsePatriaDataItem>();
		InputStream stream = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			stream = DownloadService.GetInstance().OpenHttpConnection(this.url);
			Document doc = builder.parse(stream);
			
			Element root = doc.getDocumentElement();
			Node confNode = root.getElementsByTagName("Configuration").item(0);
			this.processConfNode(confNode);
			
			Node pxNode = root.getElementsByTagName("PX").item(0);
			items.add(this.processEquityNode(pxNode));
			
			NodeList stockElements = root.getElementsByTagName("Equity");
			for (int i = 0; i < stockElements.getLength(); i++) {
				Node node = stockElements.item(i);
				
				items.add(this.processEquityNode(node));
			}
			
		} catch (Exception e) {
			String message = "Failed to process patria data xml! ";
			if (e.getMessage() != null)
				message += e.getMessage();
			e.printStackTrace();
			Log.d("PsePatriaXmlParser", message);
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return items;
	}
	
	private void processConfNode(Node node) {
		
	}

	private PsePatriaDataItem processEquityNode(Node node) {
		PsePatriaDataItem item = new PsePatriaDataItem();
		
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node childNode = children.item(i);
				String nodeName = childNode.getNodeName().toLowerCase();
				String nodeValue = childNode.getChildNodes().item(0).getNodeValue();
				
				if (nodeValue == null)
					continue;
				
				if (nodeName.equals("name")) {
					item.setIndex(nodeValue.toUpperCase().equals("PX"));
					item.setName(nodeValue);
				}
				else if (nodeName.equals("value")) {
					item.setValue(Float.parseFloat(nodeValue));
				}
				else if (nodeName.equals("percentagechange")) {
					item.setPercentableChange(Float.parseFloat(nodeValue));
				}
				else if (nodeName.equals("link")) {
					item.setLink(nodeValue);
				}
			}
		}
		return item;
	}
	
}
