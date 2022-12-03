package com.orbittech.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class SnsFile {
	
	private  ObjectMapper objMapper ;
	
	private String filename;
	private String s3Key;
	private String filesize;
	private String mimetype;

	private SnsClient snsClient;

	private String topicArn;

	public SnsFile(File currentFile, ObjectMapper objMapper, SnsClient snsClient, String topicArn) {
		// TODO Auto-generated constructor stub
		
		this.filename = currentFile.toString();
		this.s3Key = getS3Key(currentFile);
		this.filesize = getFileSizeMegaBytes(currentFile);
		
		this.objMapper = objMapper;
		
		try {
			this.mimetype = Files.probeContentType(currentFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Getting Mimetype.");
			this.mimetype = null;
		}
				
		if(this.mimetype == null) {
			this.mimetype = "null";
    	}
		
		this.snsClient = snsClient;
		this.topicArn = topicArn;
		
	}

	

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public String getFilesize() {
		return filesize;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	private String getFileSizeMegaBytes(File f) {
		// TODO Auto-generated method stub
		return String.format("%.2f bytes", (double) f.length());
	}

	private String getS3Key(File f) {
		// TODO Auto-generated method stub
		URI uri = f.toURI();
		String fPath = uri.getPath();			
		String beforeColon = fPath.substring(fPath.indexOf(":"));		
		return beforeColon.substring(beforeColon.indexOf("/")+1 );	
	}

	public String toJson() throws JsonProcessingException {
		
		
		// TODO Auto-generated method stub
		return this.objMapper.writeValueAsString(this);
	}



	public void publish() {
		// TODO Auto-generated method stub
		
		try {
			
			MessageAttributeValue attribute = MessageAttributeValue.builder()
					.dataType("String").stringValue(mimetype).build();
			
			Map<String,MessageAttributeValue > attributeMap = new HashMap<String, MessageAttributeValue>();
			attributeMap.put("mimeType", attribute);
			
			PublishRequest request = PublishRequest
					.builder().message(this.toJson())
					.messageAttributes(attributeMap)
					.topicArn(topicArn)
					.build();
			
			PublishResponse response =  this.snsClient.publish(request);
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
