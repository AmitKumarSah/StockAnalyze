/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.News;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.FormattingUtils;

/*
 * rss xml pull parser with data storing to database
 * based on http://www.ibm.com/developerworks/opensource/library/x-android/#list10
 */
public class XmlFeedPullParseHandler {
	private static final String ITEM = "item";
	private static final String LINK = "link";
	private static final String DESCRIPTION = "description";
	private static final String PUB_DATE = "pubDate";
	private static final String TITLE = "title";
	private static final String CHANNEL = "channel";
	
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
	
	XmlFeedPullParseHandler(Context context) {
		super();
		
		this.dbHelper = new NewsSqlHelper(context);
	}
	public XmlFeedPullParseHandler() {
		super();
	}
	
	NewsSqlHelper getDbHelper() {
		return dbHelper;
	}
	
	private boolean inContent() {
		return this.inDescription || this.inLink || this.inPubDate || this.inTitle;
	}
    
	/*
	 * open connection to feed, download and parse an rss xml file and create Articles
	 */
	 public List<Article> parse(Feed feed) throws XmlPullParserException {
         	List<Article> messages = null;
	        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        
	        XmlPullParser parser = factory.newPullParser();
	        InputStream stream = null;
	        
	        try {
	        	stream = DownloadService.GetInstance().openHttpConnection(feed.getUrl().toString(), true);
	        	if (feed.getCountryCode().equalsIgnoreCase("cz"))
	        		parser.setInput(stream, "windows-1250");
	        	else
	        		parser.setInput(stream, null);			// auto-detect the encoding from the stream
	            int eventType = parser.getEventType();
	            boolean done = false;
	    
	            Article currentMessage = null;
	            while (eventType != XmlPullParser.END_DOCUMENT && !done){
	                String name = null;
	                switch (eventType){
	                    case XmlPullParser.START_DOCUMENT:
	                    	messages = new ArrayList<Article>();
	                        break;
	                    case XmlPullParser.START_TAG:
	                        name = parser.getName();
	                        if (name.equalsIgnoreCase(ITEM)){
	                            currentMessage = new Article();
	                        } else if (currentMessage != null){
	                            if (name.equalsIgnoreCase(LINK)){
	                                currentMessage.setUrl(new URL(parser.nextText()));
	                            } else if (name.equalsIgnoreCase(DESCRIPTION)){
	                                currentMessage.setDescription(parser.nextText());
	                            } else if (name.equalsIgnoreCase(PUB_DATE)){
	                            	currentMessage.setDate(FormattingUtils.parse(parser.nextText()).getTime());
	                            } else if (name.equalsIgnoreCase(TITLE)){
	                                currentMessage.setTitle(parser.nextText());
	                            }    
	                        }
	                        break;
	                    case XmlPullParser.END_TAG:
	                        name = parser.getName();
	                        if (name.equalsIgnoreCase(ITEM) && currentMessage != null){
	                            messages.add(currentMessage);
	                        } else if (name.equalsIgnoreCase(CHANNEL)){
	                            done = true;
	                        }
	                        break;
	                }
	                if (! done)
						try {
							eventType = parser.next();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
	            }
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        } finally {
	        	if (stream != null)
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        }
			return messages;
	    }
	
    public void parseAndSave(Feed feed) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        InputStream stream = null;
        try {
        	stream = DownloadService.GetInstance().openHttpConnection(feed.getUrl().toString(), true);
        	if (feed.getCountryCode().equalsIgnoreCase("cz"))
        		parser.setInput(stream, "windows-1250");
        	else
        		parser.setInput(stream, null);			// auto-detect the encoding from the stream
            int eventType = parser.getEventType();
            boolean done = false;
            String currentTagName = null;
            
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        currentTagName = parser.getName();
                        this.startElement(null, currentTagName, null, null);
                    	if (inContent()) {
	                    	String text = parser.nextText();
	                    	if (text != null)
	                    		this.characters(text.toCharArray(), 0, text.length());
                    	}
                        break;
                    case XmlPullParser.END_TAG:
                    	currentTagName = parser.getName();
                        this.endElement(null, currentTagName, null);
                        if (currentTagName.equalsIgnoreCase(CHANNEL)){
                            done = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
//                    	if (inContent()) {
//	                    	String text = parser.getText();
//	                    	this.characters(text.toCharArray(), 0, text.length());
//                    	}
                    	break;
                }
                if (! done)
                	eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        	if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
    }
    
    public void startElement(String uri, String name, String qName, Attributes atts) {
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
				throw new RuntimeException("failed to insert feed");
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
				throw new RuntimeException("too many articles");
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
						Date d = FormattingUtils.parse(strDate);
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
	
	/*
	 * parse and save articles from xml
	 */
	public void updateArticles(Feed feed) throws XmlPullParserException {
		this.currentFeed = feed;
		targetFlag = TARGET_ARTICLES;
		
		this.parseAndSave(feed);
	}
	
	/*
	 * download feed xml, parse it to articles and return the list with articles
	 */
	public List<Article> fetchArticles(Feed feed) throws XmlPullParserException {
		return this.parse(feed);
	}

	/*
	 * close db
	 */
	public void done() {
		this.dbHelper.close();
	}
}
