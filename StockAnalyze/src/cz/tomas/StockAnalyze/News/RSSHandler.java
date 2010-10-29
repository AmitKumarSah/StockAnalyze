/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

import cz.tomas.StockAnalyze.Data.StockDataSqlStore;

/**
 * @author tomas
 * 
 */
public class RSSHandler extends DefaultHandler {

	// Used to define what elements we are currently in
	private boolean inItem = false;
	private boolean inTitle = false;
	private boolean inLink = false;
	private boolean inDescription = false;

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private Feed currentFeed = new Feed();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 15;

	// The possible values for targetFlag
	private static final int TARGET_FEED = 0;
	private static final int TARGET_ARTICLES = 1;

	// A flag to know if looking for Articles or Feed name
	private int targetFlag;

	private NewsSqlHelper dbHelper = null;

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (name.trim().equals("title"))
			inTitle = true;
		else if (name.trim().equals("item"))
			inItem = true;
		else if (name.trim().equals("link"))
			inLink = true;
		else if (name.trim().equals("description"))
			inDescription = true;
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {
		if (name.trim().equals("title"))
			inTitle = false;
		else if (name.trim().equals("item"))
			inItem = false;
		else if (name.trim().equals("link"))
			inLink = false;
		else if (name.trim().equals("description"))
			inDescription = false;

		// Check if looking for feed, and if feed is complete
		if (targetFlag == TARGET_FEED && currentFeed.getUrl() != null
				&& currentFeed.getTitle() != null) {

			// We know everything we need to know, so insert feed and exit
			// TODO country code
			Boolean result = dbHelper.insertFeed(currentFeed.getTitle(), currentFeed.getUrl(), "cz");
			if (! result)
				throw new SAXException();
		}

		// Check if looking for article, and if article is complete
		if (targetFlag == TARGET_ARTICLES && currentArticle.getUrl() != null
				&& currentArticle.getTitle() != null) {
			dbHelper.insertArticle(currentFeed.getFeedId(),
					currentArticle.getTitle(), currentArticle.getUrl(), null);
			currentArticle.setTitle(null);
			currentArticle.setUrl(null);

			// Lets check if we've hit our limit on number of articles
			articlesAdded++;
			if (articlesAdded >= ARTICLES_LIMIT)
				throw new SAXException();
		}

	}

	public void characters(char ch[], int start, int length) {

		String chars = (new String(ch).substring(start, start + length));

		try {
			// If not in item, then title/link refers to feed
			if (!inItem) {
				if (inTitle)
					currentFeed.setTitle(chars);
			} else {
				if (inLink)
					currentArticle.setUrl(new URL(chars));
				if (inTitle)
					currentArticle.setTitle(chars);
				if (inDescription)
					currentArticle.setDescription(chars);
			}
		} catch (MalformedURLException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.getMessage());
		}

	}

	public void createFeed(Context ctx, URL url) {
		try {
			targetFlag = TARGET_FEED;
			dbHelper = new NewsSqlHelper(ctx);
			currentFeed.setUrl(url);

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(url.openStream()));

		} catch (IOException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		} catch (SAXException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		}
	}

	public void updateArticles(Context ctx, Feed feed) {
		try {
			targetFlag = TARGET_ARTICLES;
			dbHelper = new NewsSqlHelper(ctx);
			currentFeed = feed;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(currentFeed.getUrl().openStream()));

		} catch (IOException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		} catch (SAXException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
		}
	}

	public void done() {
		if (this.dbHelper != null)
			this.dbHelper.close();
	}

}