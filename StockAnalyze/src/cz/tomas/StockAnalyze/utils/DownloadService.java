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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

/**
 * @author tomas
 * 
 */
public class DownloadService {

	private static DownloadService instance;

	public static DownloadService GetInstance() {
		if (instance == null)
			instance = new DownloadService();
		return instance;
	}

	public byte[] DownloadFromUrl(String downloadUrl, boolean compress) throws IOException {
		try {
			URL url = new URL(downloadUrl);

			long startTime = System.currentTimeMillis();
			Log.d(Utils.LOG_TAG, "DownloadService; download begining");
			Log.d(Utils.LOG_TAG, "DownloadService: download url:" + url);
			/* Open a connection to that URL. */
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
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(downloadUrl);
		HttpResponse response = client.execute(get);

		InputStream is = null;
		HttpEntity entity = response.getEntity();
		is = entity.getContent();

		return is;
	}
	
	/*
	 * open http connection to InputStream
	 * 
	 * stream must be closed manually
	 */
	public InputStream openHttpConnection(String urlString, boolean compress) throws IOException {
		InputStream in = null;
		int response = -1;

		try {
			InputStream stream = this.openHttpConnection(urlString);
			if (compress) {
				try {
					InputStream gzipInput = new GZIPInputStream(stream);
					in = gzipInput;
				} catch (IOException e) {
					Log.d(Utils.LOG_TAG, "DownloadService: Failed to create GZIP stream for " + urlString + ", using default one");
					in = stream;
				}
			}
			else
				in = stream;
		
		} catch (Exception ex) {
			throw new IOException("Error connecting: " + ex.getMessage());
		}
		return in;
	}
}
