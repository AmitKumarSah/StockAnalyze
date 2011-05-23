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
package cz.tomas.StockAnalyze.test.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.Feed;
import cz.tomas.StockAnalyze.News.XmlFeedPullParseHandler;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

public class XmlPullParserTest extends AndroidTestCase {
	URL dataUrl1;
	URL dataUrl2;
	URL dataUrl3;
	
	Feed feed;
	Context context;
	
	public XmlPullParserTest() {
		String root = "http://tomas-vondracek.net/Data/upload/test/";
		String xml1 = "akcie_cz_rss.xml";
		String xml2 = "patria_cz_rss.xml";
		String xml3 = "cyrrus_cz_rss.xml";
		try {
			this.dataUrl1 = new URL(root + xml1);
			this.dataUrl2 = new URL(root + xml2);
			this.dataUrl3 = new URL(root + xml3);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.feed = new Feed();
		this.feed.setCountryCode("cz");
		this.feed.setFeedId(0);
		this.feed.setTitle("xml test feed");
		this.feed.setUrl(this.dataUrl1);
		
		this.context = new IsolatedContext(new MockContentResolver(), getContext());
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.feed = new Feed();
		this.feed.setCountryCode("cz");
		this.feed.setFeedId(0);
		this.feed.setTitle("xml test feed");
		this.feed.setUrl(this.dataUrl1);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}



	public void testPreconditions() {
        assertNotNull(this.feed);
        assertNotNull(this.dataUrl1);
        assertNotNull(this.context);
    }
	
	public void testFetchArticlesCount() throws XmlPullParserException {
		XmlFeedPullParseHandler handler = new XmlFeedPullParseHandler();
		List<Article> articles = handler.parse(this.feed);
		
		assertEquals(15, articles.size());
	}
	
	public void testArticleContent() throws XmlPullParserException {
		String articleTitle = "Technická analýza RWE";
		String articleContent = "Dnes jsme zveřejnili technickou analýzu německé společnosti RWE.brbrpAnalýzu naleznete v přiloženém souboru.ppbrDaniel Marván, Fio banka, a.s.p";
		
		XmlFeedPullParseHandler handler = new XmlFeedPullParseHandler();
		List<Article> articles = handler.parse(this.feed);
		if (articles.size() > 0) {
			Article article = articles.get(0);
			assertEquals(articleTitle, article.getTitle());
			assertEquals(articleContent, article.getDescription());
		}
	}
}
