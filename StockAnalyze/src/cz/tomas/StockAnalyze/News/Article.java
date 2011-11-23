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
package cz.tomas.StockAnalyze.News;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import cz.tomas.StockAnalyze.utils.Utils;

import android.util.Log;

public class Article implements Serializable {
    
	public static final String BASE_URL = "http://google.com/";
	static final String GWT_URL = BASE_URL + "gwt/x?ct=url&u=%s";
	
	private static final long serialVersionUID = 8350602351455117248L;
	
	private long articleId;
    private long feedId;
    private String title;
    private URL url;
    private String mobUrl;
    private String content;
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
	
	public String getMobilizedUrl() {
		if (mobUrl == null) {
			try {
				mobUrl = String.format(GWT_URL, URLEncoder.encode(this.url.toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(Utils.LOG_TAG, "failed to decode article url", e);
			}
		}
		return mobUrl;
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
	public void setContent(String html) {
		this.content = html;
	}
	public String getContent() {
		return content;
	}
	
}
