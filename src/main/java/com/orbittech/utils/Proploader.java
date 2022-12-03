package com.orbittech.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Proploader {
	
	public static Properties readProps(String fname) throws IOException {
		
		Properties props = new Properties();
		
		try(InputStream input = new FileInputStream(fname)){
			props.load(input);
		}catch(IOException ex) {
			System.out.println(ex);
			//System.exit(0);
			throw ex;
		}
		return props;
		
		
	}

}
