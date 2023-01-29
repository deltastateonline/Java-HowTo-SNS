package com.orbittech.app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbittech.model.batch.SnsBatch;
import com.orbittech.model.batch.SnsFile;
import com.orbittech.utils.AmazonUtils;
import com.orbittech.utils.AppUtil;
import com.orbittech.utils.Proploader;

import software.amazon.awssdk.services.sns.SnsClient;

public class AppBatch1 {
	
	static ObjectMapper objMapper;
	static SnsClient snsClient;	
	static String topicArn = "";

	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		
		final long startTime = System.currentTimeMillis();	
		
		AppUtil.startApplication(args);			
		File currentFolder = new File(args[0]);	
		
		objMapper = new ObjectMapper();
		Properties props;
		
		if(!currentFolder.isDirectory()) {
			System.out.println("Select a valid folder path to be processed.");
			System.exit(0);
		}else {
			System.out.println("Start Processing.");
		}
		
		
		
		try {
			
			props = Proploader.readProps("resources/config.properties");
			
			snsClient = AmazonUtils.getSnsClient(props);
			topicArn = props.getProperty("aws.sns_topic");	
			
			SnsBatch snsBatchProcessor = new SnsBatch(snsClient, topicArn);
			
			List<String> allFiles = processFolder(null, currentFolder, snsBatchProcessor);
				
			if(allFiles.size() <= 100) {	
				System.out.println(allFiles);
			}
			
			snsBatchProcessor.flushBatch();
			
			System.out.println("Number of Files Processed :" + allFiles.size());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} 		
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total Execution time :" + (endTime - startTime) + " ms");
		

	}
	
	public static List<String> processFolder(List<String> files, File currentFolder, SnsBatch snsBatchProcessor) throws JsonProcessingException{
		
		if(files == null)
			files = new LinkedList<String>();
		
		if(!currentFolder.isDirectory()) {
			
			SnsFile afile = new SnsFile(currentFolder,objMapper);			
			files.add(afile.toJson());			
			snsBatchProcessor.addBatchEntry(afile.batchEntry());			
			return files;
		}
		
		System.out.println(currentFolder);
		for(File f : currentFolder.listFiles())
			processFolder(files, f, snsBatchProcessor);
		
		return files;
	}

}
