package cz.tomas.StockAnalyze.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.News.Feed;
import cz.tomas.StockAnalyze.News.Rss;

public class RssTest extends AndroidTestCase {

	Context context;
	URL dataUrl1;
	URL dataUrl2;
	URL dataUrl3;
	//RSSHandler handler;
	Rss handler;
	
	public RssTest() {
		//super("cz.tomas.StockAnalyze", NewsActivity.class);
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
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //this.context = this.getActivity();
        this.context = new IsolatedContext(new MockContentResolver(), getContext());
//        File file = new File("data/akcie_cz_rss.xml");
//        this.dataUrl = file.toURL();
		//dataUrl = new URL("file://data/akcie_cz_rss.xml");
		handler = new Rss(this.context);
		
		// delete all already existing feeds
		List<Feed> feeds = handler.getFeeds();
		for (Feed feed : feeds) {
			handler.deleteArticles(feed.getFeedId());
			handler.deleteFeed(feed.getFeedId());
		}
    }
    
    @Override
	protected void tearDown() throws Exception {
    	if (this.handler != null)
    		this.handler.done();
    	
		super.tearDown();
	}

	public void testPreconditions() {
        assertNotNull(this.context);
        assertNotNull(this.dataUrl1);
        assertNotNull(this.dataUrl2);
        assertNotNull(this.handler);
    }
	
	/*
	 * test to create feed from akcie.cz rss channel
	 */
	public void testCreateFeed1() throws MalformedURLException {
		this.createFeed(this.dataUrl1);
	}
	/*
	 * test to create feed from patria.cz rss channel
	 */
	public void testCreateFeed2() throws MalformedURLException {
		this.createFeed(this.dataUrl2);
	}
	
	/*
	 * test to create feed from cyrrus.cz rss channel
	 */
	public void testCreateFeed3() throws MalformedURLException {
		this.createFeed(this.dataUrl3);
	}
	
	private void createFeed(URL dataUrl) throws MalformedURLException {
		List<Feed> feeds = handler.getFeeds();
		assertEquals("initial feed count doesn't match", 0, feeds.size());
		//this.handler.createFeed(dataUrl);
		this.handler.insertFeed("test_feed", dataUrl, "cz");
		
		feeds = handler.getFeeds();
		
		assertEquals("feed count after creating feed doesn't match", 1, feeds.size());
		
//		// delete added feed
//		if (feeds.size() > 1) {
//			long feedId = feeds.get(feeds.size() - 1).getFeedId();
//			handler.getDbHelper().deleteFeed(feedId);
//		}
	}
	
	/*
	 * test to get articles from akcie rss channel
	 */
	public void testGetArticles1() throws Exception {
		this.getArticles("test_feed1", this.dataUrl1, "cz", 15);
	}
	
	/*
	 * test to get articles from patria.cz rss channel
	 */
	public void testGetArticles2() throws Exception {
		this.getArticles("test_feed2", this.dataUrl2, "cz", 50);
	}
	
	/*
	 * test to get articles from cyrrus.cz rss channel
	 */
	public void testGetArticles3() throws Exception {
		this.getArticles("test_feed3", this.dataUrl3, "cz", 10);
	}
	
	public void getArticles(String feedName, URL dataUrl, String countryCode, int expectedArticleCount) throws Exception {
		this.handler.insertFeed(feedName, dataUrl, countryCode);
		
		List<Feed> feeds = handler.getFeeds();
		
		if (feeds.size() > 0) {
			Feed feed = feeds.get(feeds.size() - 1);
			handler.fetchArticles(feed);
			List<Article> articles = handler.getArticles(feed.getFeedId());
			
			assertEquals(expectedArticleCount, articles.size());

//			long feedId = feeds.get(feeds.size() - 1).getFeedId();
//			handler.getDbHelper().deleteFeed(feedId);
		}
		else
			assertTrue("no feed to get articles from", false);
	}
}
