/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import org.apache.http.util.ByteArrayBuffer;

import cz.tomas.StockAnalyze.utils.Utils;

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

	/*
	 * Download file from URL and store content to file
	 * */
	public void DownloadFromUrl(String downloadUrl, String fileName, boolean compress) throws IOException {
		try {
			URL url = new URL(downloadUrl);
			File file = new File(fileName);

			long startTime = System.currentTimeMillis();
			Log.d(Utils.LOG_TAG, "DownloadService: download begining");
			Log.d(Utils.LOG_TAG, "DownloadService: download url:" + url);
			Log.d(Utils.LOG_TAG, "DownloadService: downloaded file name:" + fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
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

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			Log.d(Utils.LOG_TAG, "DownloadService: download finished in"
					+ ((System.currentTimeMillis() - startTime) / 1000)
					+ " sec");

		} catch (IOException e) {
			Log.d(Utils.LOG_TAG, "DownloadService: Error: " + e);
			throw e;
		}

	}
	
	public byte[] DownloadFromUrl(String downloadUrl, boolean compress) throws IOException {
		try {
			URL url = new URL(downloadUrl);

			long startTime = System.currentTimeMillis();
			Log.d(Utils.LOG_TAG, "DownloadService; download begining");
			Log.d(Utils.LOG_TAG, "DownloadService: download url:" + url);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
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
	
	/*
	 * open http connection to InputStream
	 * 
	 * stream must be closed manually
	 */
	public InputStream OpenHttpConnection(String urlString, boolean compress) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				InputStream stream = httpConn.getInputStream();
				if (compress) {
					try {
						InputStream gzipInput = new GZIPInputStream(stream);
						in = gzipInput;
					} catch (IOException e) {
						Log.d(Utils.LOG_TAG, "DownloadService: Failed to create GZIP stream, using default one");
						in = stream;
					}
				}
				else
					in = stream;
			}
		} catch (Exception ex) {
			throw new IOException("Error connecting: " + ex.getMessage());
		}
		return in;
	}
}
