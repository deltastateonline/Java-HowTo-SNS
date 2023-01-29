package com.orbittech.model.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishBatchRequest;
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry;
import software.amazon.awssdk.services.sns.model.PublishBatchResponse;

public class SnsBatch {

	private SnsClient snsClient;
	private String topicArn;
	
	private List<PublishBatchRequestEntry> currentbatch = null;

	public SnsBatch( SnsClient snsClient, String topicArn) {
		
		// TODO Auto-generated constructor stub
		this.snsClient = snsClient;
		this.topicArn = topicArn;
	}
	
	public void publishBatch(Collection<PublishBatchRequestEntry> batch) {
		
		PublishBatchRequest pbr = PublishBatchRequest.builder()
				.publishBatchRequestEntries(batch)
				.topicArn(this.topicArn)
				.build();
		
		
		PublishBatchResponse response =  this.snsClient				
				.publishBatch(pbr);
		
		//System.out.println(response.sdkHttpResponse().statusText());
		
		return;		
	}
	
	public void addBatchEntry(PublishBatchRequestEntry entry) {
		
		if(this.currentbatch == null)
			this.currentbatch = new ArrayList<PublishBatchRequestEntry>();
		
		this.currentbatch.add(entry);
		if(currentbatch.size() == 10) {
			
			this.publishBatch(this.currentbatch);
			this.currentbatch.clear();
		}		
		
	}

	public void flushBatch() {		
		if(this.currentbatch != null && this.currentbatch.size() > 0) {
			
			System.out.println("**********************************");
			System.out.println("Remaining Items = "+this.currentbatch.size());					
			this.publishBatch(this.currentbatch);			
		}
		
	}

}
