package org.ensembl.healthcheck;

import java.net.URL;
import java.net.URLClassLoader;

public class Debug {

	public static String classpathToString() {
		//Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        StringBuffer buf = new StringBuffer(); 

        //Get the URLs
        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

        for(int i=0; i< urls.length; i++) {
        	buf.append(urls[i].getFile());
        	buf.append("\n");
        }
        return buf.toString();
	}
	
}
