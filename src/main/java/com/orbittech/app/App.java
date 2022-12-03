package com.orbittech.app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbittech.model.SnsFile;
import com.orbittech.utils.AmazonUtils;
import com.orbittech.utils.AppUtil;
import com.orbittech.utils.Proploader;

import software.amazon.awssdk.services.sns.SnsClient;

public class App {
	
	static ObjectMapper objMapper;
	static SnsClient snsClient;	
	static String topicArn = "";

	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		
		final long startTime = System.currentTimeMillis();	
		
		AppUtil.startApplication(args);			
		File processFolder = new File(args[0]);	
		
		objMapper = new ObjectMapper();
		Properties props;
		
		if(!processFolder.isDirectory()) {
			System.out.println("Select a valid folder path to be processed.");
			System.exit(0);
		}else {
			System.out.println("Start Processing.");
		}
		
		
		
		try {
			
			props = Proploader.readProps("resources/config.properties");
			
			snsClient = AmazonUtils.getSnsClient(props);
			topicArn = props.getProperty("aws.sns_topic");			
			
			List<String> allFiles = processFolder(null, processFolder);
				
			if(allFiles.size() <= 100) {	
				System.out.println(allFiles);
			}
			
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
	
	public static List<String> processFolder(List<String> files, File currentFolder) throws JsonProcessingException{
		
		if(files == null)
			files = new LinkedList<String>();		
		
		if(!currentFolder.isDirectory()) {
			
			SnsFile afile = new SnsFile(currentFolder,objMapper, snsClient, topicArn);
			afile.publish();
			
			files.add(afile.toJson());
			return files;
		}
		
		//System.out.println(currentFolder);
		for(File f : currentFolder.listFiles())
			processFolder(files, f);
		
		return files;
	}

}
