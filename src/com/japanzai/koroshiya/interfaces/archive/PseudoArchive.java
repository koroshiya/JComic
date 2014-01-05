package com.japanzai.koroshiya.interfaces.archive;

import java.io.IOException;
import java.io.InputStream;

/**
 * Purpose: Interface for archives that can be extracted,
 * 			but hold within additional archives.
 * 			eg. .tar.gz, .tar.xz, .tar.bz2, etc.
 * */
public interface PseudoArchive extends ExtractableArchive{
	
	/**
	 * Purpose: Extracts the inner archive to a stream.
	 * 			Useful for manipulating the inner archive without writing to disk.
	 * @return Returns the inner archive as an InputStream
	 * */
	public InputStream extractToStream() throws IOException;
	
}
