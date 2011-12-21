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
package cz.tomas.StockAnalyze.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

/**
 * Service for accessing remote resources
 * 
 * @author tomas
 * 
 */
public class DownloadService {

	private static DownloadService instance;
	private static DefaultHttpClient sDefaultHttpClient;

	public static DownloadService GetInstance() {
		if (instance == null)
			instance = new DownloadService();
		return instance;
	}	

	/**
	 * initialize http client
	 */
	public DownloadService() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, false);
		ConnManagerParams.setMaxTotalConnections(params, 10);
		HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
		HttpConnectionParams.setSoTimeout(params, 10 * 1000);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		
		sDefaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schReg), params);
	}



	public byte[] DownloadFromUrl(String downloadUrl, boolean compress) throws IOException {
		try {
			URL url = new URL(downloadUrl);

			long startTime = System.currentTimeMillis();
			Log.d(Utils.LOG_TAG, "DownloadService; download begining");
			Log.d(Utils.LOG_TAG, "DownloadService: download url:" + url);
			/* Open a connection to the URL. */
			InputStream is = openHttpConnection(downloadUrl);
			BufferedInputStream bis = null;
			if (compress) {
				try {
					InputStream gzipInput = new GZIPInputStream(is);
					bis = new BufferedInputStream(gzipInput);
				} catch (IOException e) {
					Log.d(Utils.LOG_TAG, "DownloadService: Failed to create GZIP stream, using default one");
					bis = new BufferedInputStream(is);
				}
			}
			else
				bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			Log.d(Utils.LOG_TAG, "DownloadService: download finished in"
					+ ((System.currentTimeMillis() - startTime) / 1000)
					+ " sec");
			
			return baf.toByteArray();
		} catch (IOException e) {
			Log.d(Utils.LOG_TAG, "DownloadService: Error: " + e);
			throw e;
		}
	}

	/**
	 * @param downloadUrl
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 */
	private InputStream openHttpConnection(String downloadUrl)
			throws IOException, ClientProtocolException, IllegalStateException {
		HttpClient client = sDefaultHttpClient;
		HttpGet get = new HttpGet(downloadUrl);
		HttpResponse response = client.execute(get);

		InputStream is = null;
		HttpEntity entity = response.getEntity();
		is = entity.getContent();

		return is;
	}
	
	/**
	 * open http connection to InputStream
	 * 
	 * stream must be closed manually
	 */
	public InputStream openHttpConnection(String urlString, boolean compress) throws IOException {
		InputStream in = null;

		InputStream stream = this.openHttpConnection(urlString);
		if (compress) {
			try {
				InputStream gzipInput = new GZIPInputStream(stream);
				in = gzipInput;
			} catch (IOException e) {
				Log.d(Utils.LOG_TAG, "DownloadService: Failed to create GZIP stream for " + urlString + ", using default one");
				in = stream;
			}
		} else {
			in = stream;
		}
		return in;
	}
}
