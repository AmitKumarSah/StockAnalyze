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
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Utils;


/**
 * @author tomas
 *
 */
public final class PsePatriaXmlParser {
	
	private String url;
	private boolean isClosePhase;
	private int xmlRefreshInterval;	//minutes
	private Calendar date;

	private SimpleDateFormat format;
	
	public PsePatriaXmlParser(String url) {
		this.url = url;
		
		format = (SimpleDateFormat) DateFormat.getDateTimeInstance();
		format.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
	}
	
	/**
	 * @return the isClosePhase
	 */
	public boolean isClosePhase() {
		return isClosePhase;
	}

	/**
	 * @return the xmlRefreshInterval
	 */
	public int getXmlRefreshInterval() {
		return xmlRefreshInterval;
	}

	/**
	 * @return the date
	 */
	public Calendar getDate() {
		return date;
	}

	public List<PsePatriaDataItem> parse() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<PsePatriaDataItem> items = new ArrayList<PsePatriaDataItem>();
		InputStream stream = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			stream = DownloadService.GetInstance().openHttpConnection(this.url, true);
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
			Log.e(Utils.LOG_TAG, message, e);
			throw new Exception(message, e);
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					Log.e(Utils.LOG_TAG, "failed to close stream",e);
				}
		}
		return items;
	}
	
	/*
	 * parse configuration tag
	 * example:
	 * <Configuration RefreshInterval="10" Phase="CLOSE" Date="2010-11-05T00:00:00"/>
	 */
	private void processConfNode(Node node) throws Exception {
		NamedNodeMap attributes = node.getAttributes();
		Node refreshNode = attributes.getNamedItem("RefreshInterval");
		Node phaseNode = attributes.getNamedItem("Phase");
		Node dateNode = attributes.getNamedItem("Date");
		
		this.xmlRefreshInterval = Integer.valueOf(refreshNode.getNodeValue());
		this.isClosePhase = phaseNode.getNodeValue().equalsIgnoreCase("CLOSE");

		try {
			this.date = Calendar.getInstance();
			this.date.setTime(this.format.parse(dateNode.getNodeValue()));
			this.date.setTimeZone(Utils.PRAGUE_TIME_ZONE);
			
		} catch (Exception e) {
			String message = "Failed to process patria data xml configuration tag! ";
			if (e.getMessage() != null)
				message += e.getMessage();
			//e.printStackTrace();
			//Log.d("PsePatriaXmlParser", message);
			throw new Exception(message, e);
		}
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
