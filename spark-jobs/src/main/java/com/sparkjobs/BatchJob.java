package com.sparkjobs;

import java.io.File;
import java.io.IOException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.sparkjobs.model.Activity;
import com.sparkjobs.model.ActivityByProduct;
import com.sparkjobs.utils.LogProducer;
import com.sparkjobs.utils.Settings;
import com.sparkjobs.utils.SparkJobsUtils;

public class BatchJob {

	public static void main(String[] args) throws IOException {

		File file = new File(Settings.getInstance().inputFilePath);
		if (!file.exists()) {
			System.out.println("Generate data.csv file");
			LogProducer.load();
		}

		// setup spark session to be able to work with Dataset
		SparkSession spark = SparkJobsUtils.getSparkSesion();

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
		Dataset<ActivityByProduct> sqlResult = spark.sql("SELECT  " + "product, timestamp, "
				+ "SUM( CASE WHEN action = 'add_to_cart' THEN 1 ELSE 0 END) AS addtocartcount, "
				+ "SUM( CASE WHEN action = 'page_view' THEN 1 ELSE 0 END) AS pageviewcount, "
				+ "SUM( CASE WHEN action = 'purchase' THEN 1 ELSE 0 END) AS purchasecount " + "FROM activity "
				+ "GROUP BY product, timestamp").as(Encoders.bean(ActivityByProduct.class));
		
		sqlResult.show();
		
		CassandraJavaUtil.javaFunctions(sqlResult.rdd())
				.writerBuilder("lambda", "batch_activity_by_product", CassandraJavaUtil.mapToRow(ActivityByProduct.class)).saveToCassandra();
		
		spark.close();

	}
}
