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
package cz.tomas.StockAnalyze.News;

import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * rss xml pull parser 
 */
public class RssProcessor {
	
	private static final String ITEM = "item";
	private static final String LINK = "link";
	private static final String DESCRIPTION = "description";
	private static final String PUB_DATE = "pubDate";
	private static final String TITLE = "title";
	private static final String CHANNEL = "channel";
	
	/**
	 * open connection to feed, download and parse an rss xml file and create Articles
	 * @throws Exception from connection or parser
	 */
	 public Collection<Article> parse(Feed feed) throws Exception {
         	List<Article> messages = null;
	        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        
	        XmlPullParser parser = factory.newPullParser();
	        InputStream stream = null;
	        
	        try {
	        	stream = DownloadService.GetInstance().openHttpConnection(feed.getUrl().toString(), false);
	        	parser.setInput(stream, null);			// auto-detect the encoding from the stream
	            int eventType = parser.getEventType();
	            boolean done = false;
	    
	            Article currentMessage = null;
	            while (eventType != XmlPullParser.END_DOCUMENT && !done){
	                String name;
	                switch (eventType){
	                    case XmlPullParser.START_DOCUMENT:
	                    	messages = new ArrayList<Article>();
	                        break;
	                    case XmlPullParser.START_TAG:
	                        name = parser.getName();
	                        if (name.equalsIgnoreCase(ITEM)){
	                            currentMessage = new Article();
	                        } else if (currentMessage != null){
	                            String text = parser.nextText();
								if (name.equalsIgnoreCase(LINK)){
	                                currentMessage.setUrl(new URL(text));
	                            } else if (name.equalsIgnoreCase(DESCRIPTION)){
	                            	text = text.trim();
	                            	text = text.replaceAll("<[^>]+>", " ").replace('\n', ' ');
	                                currentMessage.setDescription(text);
	                            } else if (name.equalsIgnoreCase(PUB_DATE)){
	                            	currentMessage.setDate(FormattingUtils.parse(text).getTime());
	                            } else if (name.equalsIgnoreCase(TITLE)){
	                                currentMessage.setTitle(text);
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
	 
	/**
	 * download feed xml, parse it to articles and return the list with articles
	 */
	public Collection<Article> fetchArticles(Feed feed) throws Exception {
		Collection<Article> articles = this.parse(feed);
		return articles;
	}
}
