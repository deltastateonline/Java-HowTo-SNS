# Working with AWS SNS using Java Sdk V2

Most of the youtube tutorials show you how to work with AWS SNS using the first version of the SDK and AWS has provided a few examples on working with sns using the java sdk v2.

This git repository is a companion to the [Youtube tutorial on working with AWS SNS using the java Sdk v2 library][playlist].

In order to use the sdk , it has to be included in the pom.xml file as shown below.

```
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sns</artifactId>
    <version>2.18.16</version>    
</dependency>
```

The repository contains the following.
### AmazonUtils.java
A simple java class which contains methods to return the SnsClient and SqsClient. These clients use the AWS programmatic access user details to interact with the AWS infrastrucure.

```
public static SnsClient getSnsClient(Properties prop) {	
		 
    AwsCredentials credentials = AwsBasicCredentials.create(prop.getProperty("aws.aws_access_key_id"), 
        prop.getProperty("aws.aws_secret_access_key"));        
    
    Region awsRegion = Region.of(prop.getProperty("aws.region"));        
    SnsClient snsClient = SnsClient.builder().region(awsRegion).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();        
    
    return snsClient;
}
public static SqsClient getSqsClient(Properties prop) {
    
    AwsCredentials credentials = AwsBasicCredentials.create(prop.getProperty("aws.aws_access_key_id"), 
            prop.getProperty("aws.aws_secret_access_key"));        
        
        Region awsRegion = Region.of(prop.getProperty("aws.region"));        
        SqsClient sqsClient = SqsClient.builder().region(awsRegion).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();        
        
    return sqsClient;
}

```

### AppCreateSns.java
A java program which can be used to create and delete aws resources. 
Including 
1. SNS topics,  subscriptions and filter policies 
2. SQS queue,

Below is a sample of the program.

```
SnsClient snsClient = AmazonUtils.getSnsClient(props);
SqsClient sqsClient  = AmazonUtils.getSqsClient(props);

String topicName = "youtube-dev-topic";
String snsTopArn = AmazonUtils.createSns(snsClient, topicName, aws_resources);
String sqsArn = AmazonUtils.createQueue(sqsClient,topicName,  "queue-all",aws_resources);
String filterPolicy = "{}";
AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );

    sqsArn = AmazonUtils.createQueue(sqsClient,topicName, "queue-jpegs",aws_resources);
    filterPolicy = "{\r\n"
        + "   \"mimeType\": [\"image/jpeg\"]\r\n"
        + "}";
    AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
    
    sqsArn = AmazonUtils.createQueue(sqsClient,topicName, "queue-images",aws_resources);
    filterPolicy = "{\r\n"
        + "   \"mimeType\": [{\"prefix\" :\"image\"}]\r\n"
        + "}";
    AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
    
    sqsArn = AmazonUtils.createQueue(sqsClient,topicName, "queue-application",aws_resources);
    filterPolicy = "{\r\n"
        + "   \"mimeType\": [{\"prefix\" :\"application\"}]\r\n"
        + "}";
    AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
    

    sqsArn = AmazonUtils.createQueue(sqsClient,topicName, "queue-null",aws_resources);
    filterPolicy = "{\r\n"
        + "   \"mimeType\": [\"null\"]\r\n"
        + "}";
    AmazonUtils.createSubscription(snsClient,snsTopArn, sqsArn,filterPolicy, aws_resources );
```

### App.java
A java program that recursively traverses a folder and generates a json representation of the file details and then publishes the json to an sns topic.
Each message also has a message attribute added to it, which can be used to filter the messages to different queues.
The json representation is shown below
```
{"filename":"H:\\Pics\\visitors\\IMG_1053.JPG","s3Key":"Pics/visitors/IMG_1053.JPG","filesize":"3444516.00 bytes","mimetype":"image/jpeg"}
```
The mimetype is used as filter to direct the message to different queues. ie all file info with "image/jpeg" can be directed to a "queue_jpeg" queue.

Messages are published to the sns topic using the publish method in the SnsFile Class as follows.
```
public void publish() {       
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
        e.printStackTrace();
    }		
}
```
The java v2 sdk using a builder pattern extensively.
Publishing a large number of messages to an sns topic can be time consuming when done sequentially, since a connection has to be made every time a publish is request is initiated.

A solution is to use Java Threads or publish messages in batches.

### AppRunnable.java
This java program uses a pool of 10 java threads to publish messages to a sns topic.
```
    ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    .
    .
    SnsFile afile = new SnsFile(currentFolder,objMapper, snsClient, topicArn);			
	taskExecutor.execute(new Thread(afile));			
```

The ```ExecutorService``` execute method takes a java Thread and the java thread takes a class that implements the runnable interface.
To make the SnsFile class runnable, we have to implement the run method, which in this case is just a wrapper for the publish method.
```
public class SnsFile extends com.orbittech.model.SnsFile implements Runnable {

	public SnsFile(File currentFile, ObjectMapper objMapper, SnsClient snsClient, String topicArn) {
		super(currentFile, objMapper, snsClient, topicArn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub		
		this.publish();
	}
}
```

### Using Batches
To publish messages in batches to SNS, these messages have to be constructed as 
 ```PublishBatchRequestEntry```.
This can be done extending the existing SnsFile and adding a new method.

```
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
			System.out.println("Error Occured Creating Entry "+ e);
		}		
		return null;
	}
```
Another requirement is that only a max of 10 entries which are added to a java collection object can be published in a batch.
```
	public void addBatchEntry(PublishBatchRequestEntry entry) {
		
		if(this.currentbatch == null)
			this.currentbatch = new ArrayList<PublishBatchRequestEntry>();
		
		this.currentbatch.add(entry);
		if(currentbatch.size() == 10) {
			
			this.publishBatch(this.currentbatch);
			this.currentbatch.clear();
		}		
		
	}
```

And finally any remaining messages must be flushed.
```
	public void flushBatch() {		
		if(this.currentbatch != null && this.currentbatch.size() > 0) {
			
			System.out.println("**********************************");
			System.out.println("Remaining Items = "+this.currentbatch.size());					
			this.publishBatch(this.currentbatch);			
		}
		
	}
```

[playlist]: https://www.youtube.com/playlist?list=PLtPSv_jWEuonMHkHcFyw41pBNmN5otdFy