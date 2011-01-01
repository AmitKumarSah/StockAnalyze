/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

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
	private boolean inPubDate = false;

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private Feed currentFeed = new Feed();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 50;

	// The possible values for targetFlag
	private static final int TARGET_FEED = 0;
	private static final int TARGET_ARTICLES = 1;

	// A flag to know if looking for Articles or Feed name
	private int targetFlag;

	private NewsSqlHelper dbHelper = null;

	// e.g. "Thu, 4 Nov 2010 16:00:13 +0100"
	DateFormat frm = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
	
	RSSHandler(Context context) {
		super();
		
		this.dbHelper = new NewsSqlHelper(context);
	}

	NewsSqlHelper getDbHelper() {
		return dbHelper;
	}
	
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
		else if (name.trim().equals("pubDate"))
			inPubDate = true;
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
		else if (name.trim().equals("pubDate"))
			inPubDate = false;

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
					currentArticle.getTitle(), currentArticle.getUrl(), currentArticle.getDescription(), currentArticle.getDate());
			currentArticle.setTitle(null);
			currentArticle.setUrl(null);
			currentArticle.setDescription(null);

			// Lets check if we've hit our limit on number of articles
			articlesAdded++;
			if (articlesAdded > ARTICLES_LIMIT)
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
				if (inDescription) {
					currentArticle.setDescription(chars);
				}
				if (inPubDate) {
					Calendar date = Calendar.getInstance();
					try {
						String strDate = String.valueOf(ch);
						Date d = this.frm.parse(strDate);
						date.setTimeInMillis(d.getTime());
					} catch (ParseException e) {
						Log.d("cz.tomas.StockAnalyze.RSSHandler", "Failed to parse news item date! " + e.getMessage());
					}
					currentArticle.setDate(date.getTimeInMillis());
				}
			}
		} catch (MalformedURLException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.getMessage());
		}

	}

	public void createFeed(URL url) {
		try {
			targetFlag = TARGET_FEED;
			currentFeed.setUrl(url);

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(url.openStream()));

		} catch (IOException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * get all articles from given feed
	 * @param feed feed to get articles from
	 */
	public void updateArticles(Feed feed) {
		try {
			targetFlag = TARGET_ARTICLES;
			currentFeed = feed;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			//InputSource source = new InputSource(currentFeed.getUrl().openStream());
			InputSource source = new InputSource(currentFeed.getUrl().openStream());
			Log.d("news encoding", feed.getTitle() + " - encoding: " + source.getEncoding());

			// if unkown encondig, try to guess
			if (source.getEncoding() == null && feed.getCountryCode().equals("cz"))
				source.setEncoding("ISO-8859-1");
			if (source.getEncoding() == null)
				source.setEncoding("UTF-8");
			
			Log.d("news encoding", feed.getTitle() + " - encoding: " + source.getEncoding());
			
			//after parser finishes his work, date will be saved to db
			xr.parse(source);

		} catch (IOException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("cz.tomas.StockAnalyze.News.RssHandler", e.toString());
			e.printStackTrace();
		}
	}

	public void done() {
		if (this.dbHelper != null)
			this.dbHelper.close();
	}

}