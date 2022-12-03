package com.orbittech.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

public class AmazonUtils {
	
	public static SnsClient getSnsClient(Properties prop) {	

		 
		 AwsCredentials credentials = AwsBasicCredentials.create(prop.getProperty("aws.aws_access_key_id"), 
   				prop.getProperty("aws.aws_secret_access_key"));
		 
		 
		 Region awsRegion = Region.of(prop.getProperty("aws.region"));
		 
		 SnsClient snsClient = SnsClient.builder().region(awsRegion).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
		 
		 
		 return snsClient;
	}
	public static SqsClient getSqsClient(Properties prop) {
		// TODO Auto-generated method stub
		AwsCredentials credentials = AwsBasicCredentials.create(prop.getProperty("aws.aws_access_key_id"), 
   				prop.getProperty("aws.aws_secret_access_key"));
		 
		 
		 Region awsRegion = Region.of(prop.getProperty("aws.region"));
		 
		 SqsClient sqsClient = SqsClient.builder().region(awsRegion).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
		 
		 
		return sqsClient;
	}


	public static String createSns(SnsClient snsClient, String topicName, Map<String, List<String>> aws_resources) {
		// TODO Auto-generated method stub
		
		try {
			
			CreateTopicRequest createTopic = CreateTopicRequest.builder().name(topicName).build();
			CreateTopicResponse createResponse = snsClient.createTopic(createTopic);			
			
			System.out.println("Topic Created "+ createResponse.topicArn() + "\tStatus code : "+ createResponse.sdkHttpResponse().statusCode());			
			aws_resources.get("topics").add(createResponse.topicArn());			
			Thread.sleep(500, 0);			
			return createResponse.topicArn();	
			
		}catch (Exception e) {
			
			System.out.println("Error Occured Creating Topic");
			System.out.println(e);
		}
		return null;
	}
	public static String createQueue(SqsClient sqsClient,String topicName,  String queueName, Map<String, List<String>> aws_resources, String accountId) {
		// TODO Auto-generated method stub
		
		try {
			Map<QueueAttributeName, String> queueAttrib = new HashMap<QueueAttributeName, String>();
			queueAttrib.put(QueueAttributeName.POLICY, getPolicy(queueName, topicName, accountId) );
			
			
			CreateQueueRequest queueRequest  = CreateQueueRequest.builder()
					.attributes(queueAttrib)
					.queueName(queueName).build();
			CreateQueueResponse createResponse =  sqsClient.createQueue(queueRequest);			
			aws_resources.get("queueurl").add(createResponse.queueUrl());			
			System.out.println("Queue Created "+ createResponse.queueUrl() + "\tStatus code : "+ createResponse.sdkHttpResponse().statusCode());			
			Thread.sleep(500, 0);	
			
			return queueName;
			
		}catch (Exception e) {			
			System.out.println("Error Occured Creating Queue");
			System.out.println(e);
		}
		return "";
	}
	
	private static String getPolicy(String queueName, String topicName, String accountId) {
		
		String s = "{\r\n"
				+ "  \"Version\": \"2008-10-17\",\r\n"
				+ "  \"Id\": \"__default_policy_ID\",\r\n"
				+ "  \"Statement\": [\r\n"
				+ "    {\r\n"
				+ "      \"Sid\": \"__owner_statement\",\r\n"
				+ "      \"Effect\": \"Allow\",\r\n"
				+ "      \"Principal\": {\r\n"
				+ "        \"AWS\": \"arn:aws:iam::{accountId}:root\"\r\n"
				+ "      },\r\n"
				+ "      \"Action\": \"SQS:*\",\r\n"
				+ "      \"Resource\": \"arn:aws:sqs:ap-southeast-1:{accountId}:%s\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"Sid\": \"topic-subscription-arn:aws:sns:ap-southeast-1:{accountId}:%s\",\r\n"
				+ "      \"Effect\": \"Allow\",\r\n"
				+ "      \"Principal\": {\r\n"
				+ "        \"AWS\": \"*\"\r\n"
				+ "      },\r\n"
				+ "      \"Action\": \"SQS:SendMessage\",\r\n"
				+ "      \"Resource\": \"arn:aws:sqs:ap-southeast-1:{accountId}:%s\",\r\n"
				+ "      \"Condition\": {\r\n"
				+ "        \"ArnLike\": {\r\n"
				+ "          \"aws:SourceArn\": \"arn:aws:sns:ap-southeast-1:{accountId}:%s\"\r\n"
				+ "        }\r\n"
				+ "      }\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";
		
		s = s.replace("{accountId}", accountId);
		
		return String.format(s, queueName, topicName, queueName, topicName);
	}
	public static String createSubscription(SnsClient snsClient, String snsTopArn, String sqsArn, String filterPolicy,
			Map<String, List<String>> aws_resources) {
		// TODO Auto-generated method stub
		
		try {		
					
			SubscribeRequest request = SubscribeRequest.builder()
					.protocol("sqs")
					.endpoint(sqsArn)
					.returnSubscriptionArn(true)
					.topicArn(snsTopArn)
					.build();
			
			SubscribeResponse result = snsClient.subscribe(request);			
			System.out.println("Subscription ARN: " + result.subscriptionArn() + ".\tStatus is " + result.sdkHttpResponse().statusCode());  
			 
			 aws_resources.get("subscriptions").add(result.subscriptionArn().toString());	
			 
			 usePolicy(snsClient , result.subscriptionArn(), filterPolicy);
			 
			 
			 Thread.sleep(500);
			 return result.subscriptionArn();
		 } catch (Exception e) {
        	System.out.println("Error Subscribing to SNS topic");
            System.out.println(e);         
        }
        
        return "";
		
	}
	
	public static void usePolicy(SnsClient snsClient , String subscriptionArn, String filterPolicy) {
		
		try {			
			
			SetSubscriptionAttributesRequest re = SetSubscriptionAttributesRequest.builder()
					.subscriptionArn(subscriptionArn)
					.attributeName("FilterPolicy")
					.attributeValue(filterPolicy)
					.build();			
			
			snsClient.setSubscriptionAttributes(re);			
			System.out.println("Filter Added to "+subscriptionArn);
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error Attaching Policy");
			System.out.println(e);
		}
		
	}
	


}
