package com.japanzai.koroshiya.archive.extract_only;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.japanzai.koroshiya.filechooser.FileChooser;

//import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
//import org.apache.commons.compress.compressors.xz.XZUtils;

/**
 * Represents a .xz archive (aka a "tarball")
 * */
public class JXZArchive extends TarWrapper {
	
	public JXZArchive(File file){
		super(file);
	}
	
	@Override
	public InputStream extractToStream() throws IOException{
		
		/*FileInputStream fin = new FileInputStream(super.getArchive());
		BufferedInputStream bin = new BufferedInputStream(fin);
		//XZCompressorInputStream xzIn = new XZCompressorInputStream(bin);
		
		ByteArrayOutputStream boi = new ByteArrayOutputStream(ArchiveParser.BUFFER_SIZE);
		
		final byte[] buffer = new byte[ArchiveParser.BUFFER_SIZE];
		int n = 0;
		
		//while ((n = xzIn.read(buffer)) != -1) {
			//boi.write(buffer, 0, n);
		//}
		
		//xzIn.close();
		
		return new ByteArrayInputStream(boi.toByteArray());*/
		
		return null;
		
	}
	
	/**
	 * The bulk of this code was taken from http://commons.apache.org/proper/commons-compress/examples.html
	 * */
	@Override
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser fc){
		
		//String contentName = XZUtils.getUncompressedFilename(super.getArchive().getName());
		
		/*try{
		
			FileInputStream fin = new FileInputStream(super.getArchive());
			BufferedInputStream in = new BufferedInputStream(fin);
			FileOutputStream out = new FileOutputStream(pathToExtractTo + File.separator + contentName);
			BufferedOutputStream bout = new BufferedOutputStream(out);
			XZCompressorInputStream bzIn = new XZCompressorInputStream(in);
			
			final byte[] buffer = new byte[ArchiveParser.BUFFER_SIZE];
			int n = 0;
			
			//while (-1 != (n = bzIn.read(buffer))) {
			    //bout.write(buffer, 0, n);
			//}
			
			//bout.close();
			//bzIn.close();
		
		}catch (IOException ex){
			return false;
		}*/
		
		//return true;
		
		return false;
		
	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArrayList<String> list = new ArrayList<String>();
		
		//list.add(XZUtils.getUncompressedFilename(super.getArchive().getName()));
		
		return list;
		
	}
	
}
