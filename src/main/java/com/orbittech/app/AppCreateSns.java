package com.orbittech.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.orbittech.utils.AmazonUtils;
import com.orbittech.utils.Proploader;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sns.model.DeleteTopicResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueResponse;

public class AppCreateSns {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		Properties props;
		try {
			props = Proploader.readProps("resources/config.properties");
			
			Map<String, List<String>> aws_resources = new HashMap<String, List<String>>();
			
			aws_resources.put("topics", new ArrayList<String>() );
			aws_resources.put("queueurl", new ArrayList<String>() );
			aws_resources.put("queuearn", new ArrayList<String>() );
			aws_resources.put("subscriptions",  new ArrayList<String>() );
			
			
			
			SnsClient snsClient = AmazonUtils.getSnsClient(props);
			SqsClient sqsClient  = AmazonUtils.getSqsClient(props);
			
			String sqsQueuePrefix = props.getProperty("aws.sqs_arn_queue_prefix");
			String accountId = props.getProperty("aws.account_id");
			
			String topicName = "youtube-dev-topic";
			String snsTopArn = AmazonUtils.createSns(snsClient, topicName, aws_resources);
			String queueName = AmazonUtils.createQueue(sqsClient,topicName,  "queue-all",aws_resources,accountId);
			String filterPolicy = "{}";
			
			String sqsArn = sqsQueuePrefix+queueName;
			AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
			
			queueName = AmazonUtils.createQueue(sqsClient,topicName, "queue-jpegs",aws_resources,accountId);
			 filterPolicy = "{\r\n"
			 		+ "   \"mimeType\": [\"image/jpeg\"]\r\n"
			 		+ "}";
			 sqsArn = sqsQueuePrefix+queueName;
			 AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
			 
			 queueName = AmazonUtils.createQueue(sqsClient,topicName, "queue-images",aws_resources,accountId);
			 filterPolicy = "{\r\n"
			 		+ "   \"mimeType\": [{\"prefix\" :\"image\"}]\r\n"
			 		+ "}";
			 sqsArn = sqsQueuePrefix+queueName;
			 AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
			 
			 queueName = AmazonUtils.createQueue(sqsClient,topicName, "queue-application",aws_resources,accountId);
			 filterPolicy = "{\r\n"
			 		+ "   \"mimeType\": [{\"prefix\" :\"application\"}]\r\n"
			 		+ "}";
			 sqsArn = sqsQueuePrefix+queueName;
			 AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
			 

			 queueName = AmazonUtils.createQueue(sqsClient,topicName, "queue-null",aws_resources,accountId);
			 filterPolicy = "{\r\n"
			 		+ "   \"mimeType\": [\"null\"]\r\n"
			 		+ "}";
			 sqsArn = sqsQueuePrefix+queueName;
			 AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
			
			
			
			
			System.out.println("Press any button to continue");			
			Scanner input = new Scanner(System.in);
			String line = input.nextLine();
			
			System.out.println("Deleting Resources .");
			
			System.out.println("\nDeleting Queues.");
			System.out.println("+++++++++++++++++++++++++++++++");
			
			for(String s : aws_resources.get("queueurl")) {	
				
				DeleteQueueRequest deleteRequest = DeleteQueueRequest.builder().queueUrl(s).build();
				DeleteQueueResponse deleteResponse = sqsClient.deleteQueue(deleteRequest);							
					
				System.out.println("Deleted " + s + "\t Status Code:" + deleteResponse.sdkHttpResponse().statusCode());	
				Thread.sleep(1000);
			}	
			
			System.out.println("\nDeleting Subscription.");
			System.out.println("+++++++++++++++++++++++++++++++");
			
			for(String s : aws_resources.get("subscriptions")) {	
				
				UnsubscribeRequest deleteRequest = UnsubscribeRequest.builder().subscriptionArn(s).build();
				UnsubscribeResponse deleteResponse = snsClient.unsubscribe(deleteRequest);						
					
				System.out.println("Deleted " + s + "\t Status Code:" + deleteResponse.sdkHttpResponse().statusCode());	
				Thread.sleep(1000);
			}	
			
			
			System.out.println("\nDeleting Sns Topic.");
			System.out.println("+++++++++++++++++++++++++++++++");
			
			for(String s : aws_resources.get("topics")) {			
				DeleteTopicRequest deleteRequest = DeleteTopicRequest.builder().topicArn(s).build();			
				DeleteTopicResponse deleteResponse = snsClient.deleteTopic(deleteRequest);			
				System.out.println("Deleted " + s + "\t Status Code:" + deleteResponse.sdkHttpResponse().statusCode());	
				Thread.sleep(1000);
			}	
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Done");
		
	}

}
