package com.sparkjobs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

	private static Settings settings;

	public int records;
	public int timeMultiplier;
	public int pages;
	public int visitors;
	public String inputFilePath;
	public String outputFilePath;
	public String productsFilePath;
	public String referrerFilePath;

	private Settings() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(new File("src/main/resources/config.properties"));

			prop.load(input);
			// load clickstream properties
			records = Integer.parseInt(prop.getProperty("clickstream.records"));
			timeMultiplier = Integer.parseInt(prop.getProperty("clickstream.time_multiplier"));
			pages = Integer.parseInt(prop.getProperty("clickstream.pages"));
			visitors = Integer.parseInt(prop.getProperty("clickstream.visitors"));

			// load path properties
			inputFilePath = prop.getProperty("resource.input_path");
			outputFilePath = prop.getProperty("resource.output_path");
			productsFilePath = prop.getProperty("resource.products_path");
			referrerFilePath = prop.getProperty("resource.referrers_path");

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static Settings getInstance() {
		if (settings == null)
			settings = new Settings();
		return settings;
	}

}
