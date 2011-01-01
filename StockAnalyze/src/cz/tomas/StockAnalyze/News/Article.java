package cz.tomas.StockAnalyze.News;

import java.net.URL;

public class Article extends Object {
    private long articleId;
    private long feedId;
    private String title;
    private URL url;
    private String description;
    private long date;
    
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	/**
	 * @return the articleId
	 */
	public long getArticleId() {
		return articleId;
	}
	/**
	 * @param articleId the articleId to set
	 */
	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}
	/**
	 * @return the feedId
	 */
	public long getFeedId() {
		return feedId;
	}
	/**
	 * @param feedId the feedId to set
	 */
	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		if (this.description != null && description != null)
			this.description = this.description.concat(description);
		else
			this.description = description;
	}
	
	/**
	 * get escaped description - no html symbols
	 */
	public String getEscapedDescription() {
		String desc = this.strippedHtml(this.description);
		return desc;
	}
	
	/**
	 * strip an html string from html tags.
	 * 
	 * @param html the html string to strip
	 * @return the stripped string
	 */
	private String strippedHtml(String html) {
		if (html == null) {
			return null;
		}
		html = html.replaceAll("&amp;", "&")
				.replaceAll("&nbsp;", " ");
		
		String pattern = "<(.|\n)*?>";

		html = html.replaceAll(pattern, "");
		
//		int pos = 0;
//		while (pos < html.length()) {
//			if (html.charAt(pos) == '<') {
//				int tagEnd = html.indexOf(">", pos);
//				
//			}
//		}
		
		return html;
	}
	
	@Override
	public String toString() {
		return "Article [articleId=" + articleId + ", feedId=" + feedId
				+ ", title=" + title + "]";
	}
	
}
