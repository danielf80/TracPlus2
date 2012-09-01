package com.redxiii.tracplus.ejb.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PDFExtraction {
	
	private static final Logger logger = LoggerFactory.getLogger(PDFExtraction.class);
	
	private static final Pattern textPattern = Pattern.compile("([\\d\\w-\\.]{3,99})+");
	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static String extract(String path, String filename) throws IOException{
		
		URI pdfFile = new File(new File(path), filename).toURI();
		//http://stackoverflow.com/questions/573184/java-convert-string-to-valid-uri-object
		//http://stackoverflow.com/questions/724043/http-url-address-encoding-in-java
		PDDocument document = null;
		
		try {
			document = PDDocument.load(pdfFile.toURL());
		} catch (IOException e) {
			logger.warn("File '{}' does not exist", pdfFile);
			return null;
		}
		
		
		if( document.isEncrypted() ) {
			logger.warn("File '{}' is encrypted", pdfFile);
			return null;
		}
				
		AccessPermission ap = document.getCurrentAccessPermission();
        if( !ap.canExtractContent() ) {
        	logger.warn("Cound not read file '{}'", pdfFile);
			return null;
        }
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer writer = new PrintWriter(stream);
                
		PDFTextStripper stripper = new PDFTextStripper("UTF-8");
		stripper.writeText(document, writer);
		
		writer.flush();
		writer.close();
		
		document.close();
		
		byte[] pdfTextData = stream.toByteArray();
		long noVisible = 0;
		for (int c = 0; c < stream.size(); c++) {
			if (pdfTextData[c] < 32 || pdfTextData[c] > 127)
				noVisible++;
		}

		StringBuilder buffer = new StringBuilder(); 
		if (noVisible < (stream.size() / 4) && stream.size() >= 500) {
			logger.debug("Parsing file '{}'", pdfFile);
			Matcher matcher = textPattern.matcher(stream.toString());
			while (matcher.find()) {
				buffer.append(matcher.group(1));
				buffer.append(" ");
			}
		} else {
			logger.warn("File '{}' skipped. '{}' non-visible data of '{}'", new Object[]{pdfFile, noVisible, stream.size()});
		}
		
		return buffer.toString();
	}

}
