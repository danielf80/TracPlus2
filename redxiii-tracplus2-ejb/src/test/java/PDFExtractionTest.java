import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class PDFExtractionTest {
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Logger logger = LoggerFactory.getLogger(PDFExtractionTest.class);
		
		File directory = new File("C:\\temp\\trac-attach\\");
		
		IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),FileFilterUtils.suffixFileFilter("pdf"));
		Iterator<File> iterator = FileUtils.iterateFiles(directory, filter, FileFilterUtils.trueFileFilter());
		
		Pattern textPattern = Pattern.compile("([\\d\\w-\\.]{3,99})+");
		while (iterator.hasNext()) {
			File pdfFile = iterator.next();
			try {
				logger.info("Analizing pdfFile: {}", pdfFile);
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				Writer writer = new PrintWriter(stream);
				
				PDDocument document = PDDocument.load(pdfFile);
				if( document.isEncrypted() ) {
					logger.warn("Encrypted document");
					continue;
				}
				
				AccessPermission ap = document.getCurrentAccessPermission();
                if( ! ap.canExtractContent() ) {
                	logger.warn("You do not have permission to extract text");
					continue;
                }
                
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
				
				logger.warn("Doc has {} non-visible chars of {}", noVisible, stream.size());
				if (noVisible < (stream.size() / 4) && stream.size() >= 500) {
					Matcher matcher = textPattern.matcher(stream.toString());
					while (matcher.find()) {
						logger.info(matcher.group(1));
					}
				} 
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
