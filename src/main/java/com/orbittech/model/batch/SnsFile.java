package com.orbittech.model.batch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry;

public class SnsFile extends com.orbittech.model.SnsFile {
	
	@Override
	public int hashCode() {
		return super.hashCode();
		
	}

	public SnsFile(File currentFile, ObjectMapper objMapper, SnsClient snsClient, String topicArn) {
		super(currentFile, objMapper, snsClient, topicArn);
		// TODO Auto-generated constructor stub
	}
	
	public SnsFile(File currentFile, ObjectMapper objMapper) {
		super(currentFile, objMapper);
		// TODO Auto-generated constructor stub
	}
	
	public PublishBatchRequestEntry batchEntry() {		
		
		try {			
			
			MessageAttributeValue attribute = MessageAttributeValue.builder()
					.dataType("String").stringValue(this.getMimetype()).build();
			
			Map<String,MessageAttributeValue > attributeMap = new HashMap();
			attributeMap.put("mimeType", attribute);
			
			
			PublishBatchRequestEntry anEntry = PublishBatchRequestEntry					
					.builder()
					.id(Integer.toString(this.hashCode()))
					.message(this.toJson())
					.messageAttributes(attributeMap)
					.build();
			
			return anEntry;
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Occured Creating Entry "+ e);
		}
		
		
		
		return null;
	}


}
