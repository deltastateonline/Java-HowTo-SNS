package com.orbittech.model.runnable;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sns.SnsClient;

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
