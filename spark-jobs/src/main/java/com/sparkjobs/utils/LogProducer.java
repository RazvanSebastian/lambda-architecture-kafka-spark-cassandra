package com.sparkjobs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogProducer {

	private enum Action {
		add_to_cart, page_view, purchase
	}

	public static void load() {

		PrintWriter printWriter = null;

		BufferedReader inputProducts = null;
		BufferedReader inputReferrers = null;

		List<String> products = new ArrayList<String>();
		List<String> referrers = new ArrayList<String>();
		List<String> visitors = new ArrayList<String>();
		List<String> pages = new ArrayList<String>();

		StringBuilder builder = new StringBuilder();

		try {
			InputStream is1 = new FileInputStream(new File(Settings.getInstance().productsFilePath));
			inputProducts = new BufferedReader(new InputStreamReader(is1, "UTF-8"));
			toStringArray(products, inputProducts);

			InputStream is2 = new FileInputStream(new File(Settings.getInstance().referrerFilePath));
			inputReferrers = new BufferedReader(new InputStreamReader(is2, "UTF-8"));
			toStringArray(referrers, inputReferrers);

			generatePages(pages, Settings.getInstance().pages);
			generateVisitors(visitors, Settings.getInstance().visitors);

			Random random = new Random();
			long timestamp = System.currentTimeMillis();
			long adjustedTimestamp = timestamp;

			for (int i = 0; i < Settings.getInstance().records; i++) {

				// set-up adjustedtimestamp
				adjustedTimestamp = adjustedTimestamp
						+ (System.currentTimeMillis() - timestamp) * Settings.getInstance().timeMultiplier;
				timestamp = System.currentTimeMillis();

				// set-up action
				String randomAction;
				switch (random.nextInt(3 - 1) + 1) {
				case 1:
					randomAction = Action.page_view.name();
					break;
				case 2:
					randomAction = Action.add_to_cart.name();
					break;
				case 3:
					randomAction = Action.purchase.name();
					break;
				default:
					randomAction = Action.page_view.name();
					break;
				}

				// pickup random visitor
				String randomVisitor = visitors.get(random.nextInt(Settings.getInstance().visitors));

				// pickup random page
				String randomPage = pages.get(random.nextInt(Settings.getInstance().pages));

				// pickup random referrer
				String randomReferrer = referrers.get(random.nextInt(referrers.size() - 1));

				// pickup random product
				String randomProduct = products.get(random.nextInt(products.size() - 1));

				System.out.println(randomReferrer);
				builder.append(adjustedTimestamp + "," + randomReferrer + "," + randomAction + "," + " " + ","
						+ randomVisitor + "," + randomPage + "," + randomProduct + '\n');
				try {
					int timeSleep = random.nextInt((Settings.getInstance().records - 1) + 1);
					System.out.println("LogProducer " + timeSleep);
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			File file = new File(Settings.getInstance().inputFilePath);
			file.getParentFile().mkdirs();
			file.createNewFile();
			printWriter = new PrintWriter(file);
			printWriter.print(builder);
			printWriter.flush();

			System.out.println("File data.csv created!");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputProducts != null)
				try {
					inputProducts.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (inputReferrers != null)
				try {
					inputReferrers.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (printWriter != null)
				printWriter.close();
		}
	}

	private static void toStringArray(List<String> list, BufferedReader reader) throws IOException {
		String line = "";
		while ((line = reader.readLine()) != null) {
			list.add(line);
		}
	}

	private static void generateVisitors(List<String> list, int visitorsNo) {
		for (int i = 0; i < Settings.getInstance().visitors; i++) {
			list.add("Visitor-" + i);
		}
	}

	private static void generatePages(List<String> list, int noPages) {
		for (int i = 0; i < Settings.getInstance().pages; i++) {
			list.add("Page-" + i);
		}
	}

}
