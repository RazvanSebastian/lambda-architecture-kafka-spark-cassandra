package com.sparkjobs;

import java.io.File;
import java.io.IOException;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import com.sparkjobs.model.Activity;
import com.sparkjobs.utils.LogProducer;
import com.sparkjobs.utils.Settings;

public class BatchJob {

	public static void main(String[] args) throws IOException {

		File file = new File(Settings.getInstance().inputFilePath);
		if (!file.exists()) {
			System.out.println("Generate data.csv file");
			LogProducer.load();
		}

		// get spark confgiruation
		SparkConf sparkConf = new SparkConf().setAppName("Example Spark App").setMaster("local");

		// setup spark session to be able to work with Dataset
		SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

		// import data
		Dataset<Row> input = spark.read().csv(Settings.getInstance().inputFilePath);
		input.show();

		// map to Dataset of Activity
		Dataset<Activity> activityDataset = input.map((row) -> {
			if (row.size() != 8)
				throw new RuntimeException("Row must have size of 8!");
			return new Activity(Long.parseLong(row.getString(0)), row.getString(1), row.getString(2), row.getString(3),
					row.getString(4), row.getString(5), row.getString(6), row.getString(7));
		}, Encoders.bean(Activity.class));

		/*
		 * Actions & Transformations
		 */
		activityDataset.createOrReplaceTempView("activity");
		Dataset<Row> sqlResult = spark.sql("SELECT  " + "product, timestamp, referrer, "
				+ "SUM( CASE WHEN action = 'page_view' THEN 1 ELSE 0 END) AS page_view_count, "
				+ "SUM( CASE WHEN action = 'add_to_cart' THEN 1 ELSE 0 END) AS add_to_cart_count, "
				+ "SUM( CASE WHEN action = 'purchase' THEN 1 ELSE 0 END) AS purchase_count " + "FROM activity "
				+ "GROUP BY product, timestamp, referrer").cache();
		sqlResult.write().partitionBy("referrer").mode(SaveMode.Append).parquet(Settings.getInstance().outputFilePath);

		spark.close();

	}
}
