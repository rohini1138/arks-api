package org.wordcounthbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.cbsa.api.controller.metadata.MetadataManager;
import org.cbsa.api.model.FileMetadata;
import org.cbsa.api.model.Keyword;

public class JobManager {

	private Job job;
	public static List<Keyword> keywordList;
	private static int fileCounter = 0;

	static {

		keywordList = new ArrayList<Keyword>();
		fileCounter++;
	}

	public void createJob(String inputPath, String outputPath)
			throws ClassNotFoundException, IOException, InterruptedException {

		MetadataManager metadataManager = new MetadataManager();

		Configuration configuration = new Configuration();
		String jobName = "job_" + inputPath;

		try {

			job = Job.getInstance(configuration, jobName);
			// job.addFileToClassPath(new Path(Dependencies.PDFBOX_PATH));
			// job.addFileToClassPath(new Path(Dependencies.FONTBOX_PATH));
			job.setJarByClass(this.getClass());

			job.setInputFormatClass(PdfInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);

			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(LongWritable.class);

			FileInputFormat.setInputPaths(job, new Path(inputPath));
			FileOutputFormat.setOutputPath(job, new Path(outputPath));

			job.setMapperClass(PdfMapper.class);
			job.setCombinerClass(PdfReducer.class);
			job.setReducerClass(PdfReducer.class);

		} catch (IOException e) {

			e.printStackTrace();
		}

		if (job.waitForCompletion(true)) {

			FileMetadata fileMetadata = new FileMetadata(
					String.valueOf(fileCounter), inputPath, inputPath, "50",
					"600", "programming", keywordList);

			metadataManager.addNewFileMetadata(fileMetadata);
			System.out.println(jobName + "completed");
			keywordList = new ArrayList<Keyword>();
			fileCounter++;
		}
	}
}
