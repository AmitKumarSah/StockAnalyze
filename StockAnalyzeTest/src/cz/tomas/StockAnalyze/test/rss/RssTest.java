package cz.tomas.StockAnalyze.test.rss;

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
	
	/*
	 * xml with 12 articles
	 */
	URL mergeUrl1;
	/*
	 * same as mergeUrl1 but with 3 more articles
	 */
	URL mergeUrl2;
	
	Rss rss;
	
	public RssTest() {
		String root = "http://tomas-vondracek.net/Data/upload/test/";
		String xml1 = "akcie_cz_rss.xml";
		String xml2 = "patria_cz_rss.xml";
		String xml3 = "cyrrus_cz_rss.xml";
		
		String xmlMerge1 = "akcie_cz_rss2.xml";
		String xmlMerge2 = "akcie_cz_rss3.xml";
		try {
			this.dataUrl1 = new URL(root + xml1);
			this.dataUrl2 = new URL(root + xml2);
			this.dataUrl3 = new URL(root + xml3);
			this.mergeUrl1 = new URL(root + xmlMerge1);
			this.mergeUrl2 = new URL(root + xmlMerge2);
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
		rss = new Rss(this.context);
		
		// delete all already existing feeds
		List<Feed> feeds = rss.getFeeds();
		for (Feed feed : feeds) {
			rss.deleteArticles(feed.getFeedId());
			rss.deleteFeed(feed.getFeedId());
		}
    }
    
    @Override
	protected void tearDown() throws Exception {
    	if (this.rss != null)
    		this.rss.done();
    	
		super.tearDown();
	}

	public void testPreconditions() {
        assertNotNull(this.context);
        assertNotNull(this.dataUrl1);
        assertNotNull(this.dataUrl2);
        assertNotNull(this.rss);
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
		List<Feed> feeds = rss.getFeeds();
		assertEquals("initial feed count doesn't match", 0, feeds.size());
		//this.handler.createFeed(dataUrl);
		this.rss.insertFeed("test_feed", dataUrl, "cz");
		
		feeds = rss.getFeeds();
		
		assertEquals("feed count after creating feed doesn't match", 1, feeds.size());
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
		this.rss.insertFeed(feedName, dataUrl, countryCode);
		
		List<Feed> feeds = rss.getFeeds();
		
		if (feeds.size() > 0) {
			Feed feed = feeds.get(feeds.size() - 1);
			rss.fetchArticles(feed);
			List<Article> articles = rss.getArticles(feed.getFeedId(), 50);
			
			assertEquals(expectedArticleCount, articles.size());
		}
		else
			assertTrue("no feed to get articles from", false);
	}
	
	/*
	 * add articles to db from first url - 12 articles,
	 * then add articles from second url (using one feed),
	 * there are the same 12 articles plus 3 other
	 * check whether at finish there is 15 articles
	 */
	public void testMergeArticles() throws Exception {
		// create test feed from test server with 12 articles
		this.rss.insertFeed("test_merge_feed1", this.mergeUrl1, "cz");
		Feed feed = this.rss.getFeeds().get(0);
		//long feedId = feed.getFeedId();
		List<Article> articles = this.rss.fetchArticles(feed);
		assertEquals(12, articles.size());
		// now we have some articles in db, 
		// change feed url to download some more articles - 15 but 12 of them is common
		feed.setUrl(this.mergeUrl2);
		articles = this.rss.fetchArticles(feed);
		assertEquals(12 + 3, articles.size());
	}
	
	/*
	 * add articles from one url and then from second url,
	 * on both urls are different articles
	 */
	public void testMergeArticlesNewOnly() throws Exception {
		// create test feed from test server with 12 articles
		this.rss.insertFeed("test_merge_feed1", this.dataUrl1, "cz");
		Feed feed = this.rss.getFeeds().get(0);
		long feedId = feed.getFeedId();
		this.rss.fetchArticles(feed);
		assertEquals(15, this.rss.getArticles(feedId).size(), 30);
		// now we have some articles in db, 
		// change feed url to download some more articles and check their count
		feed.setUrl(this.mergeUrl1);
		this.rss.fetchArticles(feed);
		
		assertEquals(15 + 12, this.rss.getArticles(feedId).size(), 30);
	}
	
	/*
	 * get articles and check order
	 */
	public void testOrder() {
		// create test feed from test server with 12 articles
		this.rss.insertFeed("test_merge_feed1", this.dataUrl1, "cz");
		Feed feed = this.rss.getFeeds().get(0);
		List<Article> articles = this.rss.fetchArticles(feed);
		this.checkArticlesOrder(articles);
	}
	
	/*
	 * merge articles and check their order by date
	 */
	public void testMergeOrder() {
		// create test feed from test server with 12 articles
		this.rss.insertFeed("test_merge_feed1", this.dataUrl1, "cz");
		Feed feed = this.rss.getFeeds().get(0);
		long feedId = feed.getFeedId();
		List<Article> articles = this.rss.fetchArticles(feed);
		checkArticlesOrder(articles);
		
		// change feed url to download some more articles and check their order
		feed.setUrl(this.mergeUrl1);
		articles = this.rss.fetchArticles(feed);
		checkArticlesOrder(articles);
	}
	
	private void checkArticlesOrder(List<Article> articles) {
		Article previousArticle = null;
		for (Article article : articles) {
			if (previousArticle != null) {
				long prevDate = previousArticle.getDate();
				long currentDate = article.getDate();
				
				assertTrue(currentDate < prevDate);
			}
			previousArticle = article;
		}
	}
	
	/*
	 * test if merged articles would get stored in database,
	 * it uses the same articles as in testMergeArticles() test
	 */
	public void testInsertMergedArticles() {
		this.rss.insertFeed("test_merge_feed1", this.mergeUrl1, "cz");
		Feed feed = this.rss.getFeeds().get(0);
		this.rss.fetchArticles(feed);
		feed.setUrl(this.mergeUrl2);
		this.rss.fetchArticles(feed);
		
		// get all articles from database
		List<Article> articles = this.rss.getArticles(feed.getFeedId());
		assertEquals(15, articles.size());
	}
}
