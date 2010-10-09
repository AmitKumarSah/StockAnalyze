/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

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

	/*
	 * Download file from URL and store content to file
	 * */
	public void DownloadFromUrl(String downloadUrl, String fileName) throws IOException {
		try {
			URL url = new URL(downloadUrl);
			File file = new File(fileName);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadService", "download begining");
			Log.d("DownloadService", "download url:" + url);
			Log.d("DownloadService", "downloaded file name:" + fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

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
			Log.d("DownloadService", "download finished in"
					+ ((System.currentTimeMillis() - startTime) / 1000)
					+ " sec");

		} catch (IOException e) {
			Log.d("DownloadService", "Error: " + e);
			throw e;
		}

	}
	
	public byte[] DownloadFromUrl(String downloadUrl) throws IOException {
		try {
			URL url = new URL(downloadUrl);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadService", "download begining");
			Log.d("DownloadService", "download url:" + url);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			Log.d("DownloadService", "download finished in"
					+ ((System.currentTimeMillis() - startTime) / 1000)
					+ " sec");
			
			return baf.toByteArray();
		} catch (IOException e) {
			Log.d("DownloadService", "Error: " + e);
			throw e;
		}
	}
}
