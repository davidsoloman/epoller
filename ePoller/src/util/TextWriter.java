package util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;

public final class TextWriter {

	public static void initOutputFiles(ConcurrentHashMap<String, String> devices, ConcurrentHashMap<String, String> params) throws IOException {

		File outputFile = new File("data");
		RandomAccessFile emptyRandomAccessFile;

		outputFile.mkdir();

		for (String deviceName : devices.values()) {
			outputFile = new File("data/" + deviceName + ".csv");
			if (!outputFile.exists()) {
				emptyRandomAccessFile = new RandomAccessFile(outputFile, "rw");

				emptyRandomAccessFile.writeBytes("timestamp,");
				for (String paramName : params.values())
					emptyRandomAccessFile.writeBytes(paramName + ",");

				emptyRandomAccessFile.writeBytes("latency" + "\n");
				emptyRandomAccessFile.close();
			}
		}
	}

	public static synchronized void printlnToFile(String file, String value) {

		try {
			if (!file.endsWith(".log"))
				file = "data/" + file + ".csv";

			RandomAccessFile randomAccessFile = new RandomAccessFile(new File(file), "rw");
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(value + "\n");
			randomAccessFile.close();
		} catch (IOException e) {
			System.out.println("There was an error writing to the file: " + file);
			e.printStackTrace();
		}
	}

}
