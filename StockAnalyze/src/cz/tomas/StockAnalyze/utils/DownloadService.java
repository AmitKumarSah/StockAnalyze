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

import android.net.http.AndroidHttpClient;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.URL;

/**
 * Service for accessing remote resources
 * 
 * @author tomas
 * 
 */
public class DownloadService {

	private static DownloadService instance;
	private AndroidHttpClient httpClient;

	public static DownloadService GetInstance() {
		if (instance == null) {
			instance = new DownloadService();
		}
		return instance;
	}	

	/**
	 * initialize http client
	 */
	public DownloadService() {
//		HttpParams params = new BasicHttpParams();
//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//		HttpProtocolParams.setUseExpectContinue(params, false);
//		ConnManagerParams.setMaxTotalConnections(params, 10);
//		HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
//		HttpConnectionParams.setSoTimeout(params, 10 * 1000);
//
//		SchemeRegistry schReg = new SchemeRegistry();
//		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		httpClient = AndroidHttpClient.newInstance("stockanalyze,gzip");
		HttpClientParams.setRedirecting(httpClient.getParams(), true);
	}

	public byte[] DownloadFromUrl(String downloadUrl, boolean compress) throws IOException {
		InputStream is = null;
		try {
			URL url = new URL(downloadUrl);

			long startTime = System.currentTimeMillis();
			if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "DownloadService: download url:" + url);
			/* Open a connection to the URL. */
			is = openHttpConnection(downloadUrl);

			// Read bytes to the Buffer until there is nothing more to read(-1).
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = is.read()) != -1) {
				baf.append((byte) current);
			}

			if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "DownloadService: download finished in"
					+ ((System.currentTimeMillis() - startTime) / 1000)
					+ " sec");
			
			return baf.toByteArray();
		} catch (IOException e) {
			Log.d(Utils.LOG_TAG, "DownloadService: Error: " + e);
			throw e;
		} finally {
			if (is != null) {
				is.close();
			}
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
			throws IOException, IllegalStateException {
		HttpGet get = new HttpGet(downloadUrl);

		AndroidHttpClient.modifyRequestToAcceptGzipResponse(get);
		HttpResponse response = httpClient.execute(get);

		InputStream is;
		HttpEntity entity = response.getEntity();
		is = AndroidHttpClient.getUngzippedContent(entity);
		return is;
	}
	
	/**
	 * open http connection to InputStream
	 * 
	 * stream must be closed manually
	 */
	public InputStream openHttpConnection(String urlString, boolean compress) throws IOException {
		InputStream in = null;

		in = this.openHttpConnection(urlString);
		return in;
	}

	public String readStream(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
