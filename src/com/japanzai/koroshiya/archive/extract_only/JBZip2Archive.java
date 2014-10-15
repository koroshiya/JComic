package com.japanzai.koroshiya.archive.extract_only;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;

import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.io_utils.ArchiveParser;

public class JBZip2Archive extends TarWrapper {
	
	public JBZip2Archive(File file){
		super(file);
	}
		
	@Override
	public InputStream extractToStream() throws IOException{
		
		FileInputStream fin = new FileInputStream(super.getArchive());
		BufferedInputStream bin = new BufferedInputStream(fin);
		BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bin);
		
		ByteArrayOutputStream boi = new ByteArrayOutputStream(ArchiveParser.BUFFER_SIZE);
		
		final byte[] buffer = new byte[ArchiveParser.BUFFER_SIZE];
		int n = 0;
		
		while ((n = bzIn.read(buffer)) != -1) {
			boi.write(buffer, 0, n);
		}
		
		bzIn.close();
		
		return new ByteArrayInputStream(boi.toByteArray());
		
	}
	
	/**
	 * The bulk of this code was taken from http://commons.apache.org/proper/commons-compress/examples.html
	 * */
	@Override
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser fc){
		
		String contentName = BZip2Utils.getUncompressedFilename(super.getArchive().getName());
		
		try{
		
			FileInputStream fin = new FileInputStream(super.getArchive());
			BufferedInputStream in = new BufferedInputStream(fin);
			FileOutputStream out = new FileOutputStream(pathToExtractTo + File.separator + contentName);
			BufferedOutputStream bout = new BufferedOutputStream(out);
			BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
			
			final byte[] buffer = new byte[ArchiveParser.BUFFER_SIZE];
			int n = 0;
			
			while (-1 != (n = bzIn.read(buffer))) {
			    bout.write(buffer, 0, n);
			}
			
			bout.close();
			bzIn.close();
		
		}catch (IOException ex){
			return false;
		}
		
		return true;
		
	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(BZip2Utils.getUncompressedFilename(super.getArchive().getName()));
		
		return list;
		
	}
	
}
