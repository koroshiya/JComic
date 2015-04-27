/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression
 * algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */
package de.innosystec.unrar;

import android.annotation.TargetApi;
import android.os.Build;

import com.japanzai.koroshiya.reader.MainActivity;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.innosystec.unrar.io.ReadOnlyAccessFile;
import de.innosystec.unrar.rarfile.BaseBlock;
import de.innosystec.unrar.rarfile.BlockHeader;
import de.innosystec.unrar.rarfile.FileHeader;
import de.innosystec.unrar.rarfile.MainHeader;
import de.innosystec.unrar.rarfile.MarkHeader;
import de.innosystec.unrar.rarfile.SubBlockHeader;
import de.innosystec.unrar.rarfile.UnrarHeadertype;
import de.innosystec.unrar.unpack.ComprDataIO;
import de.innosystec.unrar.unpack.Unpack;

/**
 * The Main Rar Class; represents a rar Archive
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class Archive implements Closeable {

	private File file;

	private ReadOnlyAccessFile rof;

	private final UnrarCallback unrarCallback;

	private final ComprDataIO dataIO;

	private final List<BaseBlock> headers = new ArrayList<>();

	private MarkHeader markHead = null;

	private MainHeader newMhd = null;

	private Unpack unpack;

	private int currentHeaderIndex;

	/** Size of packed data in current file. */
	private long totalPackedSize = 0L;

	/** Number of bytes of compressed data read from current file. */
	private long totalPackedRead = 0L;

	/**
	 * create a new archive object using the given file
	 * 
	 * @param file
	 *            the file to extract
	 */
	public Archive(File file) throws IOException {
		dataIO = new ComprDataIO(this);
		setFile(file);
		this.unrarCallback = null;
	}

	public File getFile() {
		return file;
	}

	void setFile(File file) throws IOException {
		this.file = file;
		totalPackedSize = 0L;
		totalPackedRead = 0L;
		close();
		rof = new ReadOnlyAccessFile(file);

        readHeaders();
		// Calculate size of packed data
		for (BaseBlock block : headers) {
			if (block.getHeaderType() == UnrarHeadertype.FileHeader) {
				totalPackedSize += ((FileHeader) block).getFullPackSize();
			}
		}
		if (unrarCallback != null) {
			unrarCallback.volumeProgressChanged(totalPackedRead, totalPackedSize);
		}
	}

	public void bytesReadRead(int count) {
		if (count > 0) {
			totalPackedRead += count;
			if (unrarCallback != null) {
				unrarCallback.volumeProgressChanged(totalPackedRead, totalPackedSize);
			}
		}
	}

	public ReadOnlyAccessFile getRof() {
		return rof;
	}

	/**
	 * @return returns all file headers of the archive
	 */
	public List<FileHeader> getFileHeaders() {
		List<FileHeader> list = new ArrayList<>();
		for (BaseBlock block : headers) {
			if (block.getHeaderType().equals(UnrarHeadertype.FileHeader)) {
				list.add((FileHeader) block);
			}
		}
		return list;
	}

	public FileHeader nextFileHeader() {
		int n = headers.size();
		while (currentHeaderIndex < n) {
			BaseBlock block = headers.get(currentHeaderIndex++);
			if (block.getHeaderType() == UnrarHeadertype.FileHeader) {
				return (FileHeader) block;
			}
		}
		return null;
	}

	public UnrarCallback getUnrarCallback() {
		return unrarCallback;
	}

	/**
	 * Read the headers of the archive
	 * 
	 * @throws IOException
	 */
	private void readHeaders() throws IOException {
		markHead = null;
		newMhd = null;
		headers.clear();
		currentHeaderIndex = 0;
		int toRead;

		long fileLength = this.file.length();

		while (true) {
            int size;
            long newpos;
            byte[] baseBlockBuffer = new byte[BaseBlock.BaseBlockSize];

            long position = rof.getPosition();

			if (position >= fileLength) {
				break;
			}

            size = rof.readFully(baseBlockBuffer, BaseBlock.BaseBlockSize);
            if (size == 0) {
                break;
            }
            BaseBlock block;
            try {
                block = new BaseBlock(baseBlockBuffer);
            }catch (IOException e){
                e.printStackTrace();
                continue;
            }

            block.setPositionInFile(position);
            
            if (block.getHeaderType() == null){
            	break;
            }
            switch (block.getHeaderType()) {

                case MarkHeader:
                        markHead = new MarkHeader(block);
                        if (!markHead.isSignature()) {
                            throw new IOException("Corrupt RAR archive");
                        }
                        headers.add(markHead);
                        break;

                case MainHeader:
                        toRead = block.hasEncryptVersion() ? MainHeader.mainHeaderSizeWithEnc : MainHeader.mainHeaderSize;
                        byte[] mainbuff = new byte[toRead];
                        rof.readFully(mainbuff, toRead);
                        MainHeader mainhead = new MainHeader(block, mainbuff);
                        headers.add(mainhead);
                        this.newMhd = mainhead;
                        break;

                case SignHeader:
                        toRead = BaseBlock.signHeaderSize;
                        byte[] signBuff = new byte[toRead];
                        rof.readFully(signBuff, toRead);
                        BaseBlock signHead = new BaseBlock(block);
                        headers.add(signHead);
                        break;

                case AvHeader:
                        toRead = BaseBlock.avHeaderSize;
                        byte[] avBuff = new byte[toRead];
                        rof.readFully(avBuff, toRead);
                        BaseBlock avHead = new BaseBlock(block);
                        headers.add(avHead);
                        break;

                case CommHeader:
                        toRead = BaseBlock.commentHeaderSize;
                        byte[] commBuff = new byte[toRead];
                        rof.readFully(commBuff, toRead);
                        BaseBlock commHead = new BaseBlock(block);
                        headers.add(commHead);
                        newpos = commHead.getPositionInFile() + commHead.getHeaderSize();
                        rof.setPosition(newpos);

                        break;
                case EndArcHeader:

                        toRead = 0;
                        if (block.hasArchiveDataCRC()) {
                                toRead += BaseBlock.endArcArchiveDataCrcSize;
                        }
                        if (block.hasVolumeNumber()) {
                                toRead += BaseBlock.endArcVolumeNumberSize;
                        }
                        BaseBlock endArcHead;
                        if (toRead > 0) {
                            byte[] endArchBuff = new byte[toRead];
                            rof.readFully(endArchBuff, toRead);
                        }
                        endArcHead = new BaseBlock(block);

                        headers.add(endArcHead);
                        return;

                default:
                        byte[] blockHeaderBuffer = new byte[BlockHeader.blockHeaderSize];
                        rof.readFully(blockHeaderBuffer, BlockHeader.blockHeaderSize);
                        BlockHeader blockHead = new BlockHeader(block,  blockHeaderBuffer);

                        switch (blockHead.getHeaderType()) {
                            case NewSubHeader:
                            case FileHeader:
                                    toRead = blockHead.getHeaderSize()
                                                    - BlockHeader.BaseBlockSize
                                                    - BlockHeader.blockHeaderSize;
                                    byte[] fileHeaderBuffer = new byte[toRead];
                                    rof.readFully(fileHeaderBuffer, toRead);

                                    FileHeader fh = new FileHeader(blockHead, fileHeaderBuffer);
                                    headers.add(fh);
                                    newpos = fh.getPositionInFile() + fh.getHeaderSize()
                                                    + fh.getFullPackSize();
                                    rof.setPosition(newpos);
                                    break;

                            case ProtectHeader:
                                    toRead = blockHead.getHeaderSize()
                                                    - BlockHeader.BaseBlockSize
                                                    - BlockHeader.blockHeaderSize;
                                    byte[] protectHeaderBuffer = new byte[toRead];
                                    rof.readFully(protectHeaderBuffer, toRead);
                                BlockHeader ph = new BlockHeader(blockHead, protectHeaderBuffer);

                                    newpos = ph.getPositionInFile() + ph.getHeaderSize() + ph.getDataSize();
                                    rof.setPosition(newpos);
                                    break;

                            case SubHeader: {
                                    byte[] subHeadbuffer = new byte[SubBlockHeader.SubBlockHeaderSize];
                                    rof.readFully(subHeadbuffer,
                                                    SubBlockHeader.SubBlockHeaderSize);
                                    SubBlockHeader subHead = new SubBlockHeader(blockHead,
                                                    subHeadbuffer);
                                    subHead.print();
                                    switch (subHead.getSubType()) {
                                    case MAC_HEAD: {
                                            byte[] macHeaderbuffer = new byte[BaseBlock.MacInfoHeaderSize];
                                            rof.readFully(macHeaderbuffer, BaseBlock.MacInfoHeaderSize);
                                            BaseBlock macHeader = new BaseBlock(subHead);
                                            macHeader.print();
                                            headers.add(macHeader);

                                            break;
                                    }
                                    // TODO implement other subheaders
                                    case BEEA_HEAD:
                                            break;
                                    case EA_HEAD: {
                                            byte[] eaHeaderBuffer = new byte[BaseBlock.EAHeaderSize];
                                            rof.readFully(eaHeaderBuffer, BaseBlock.EAHeaderSize);
                                            BaseBlock eaHeader = new BaseBlock(subHead);
                                            eaHeader.print();
                                            headers.add(eaHeader);

                                            break;
                                    }
                                    case NTACL_HEAD:
                                            break;
                                    case STREAM_HEAD:
                                            break;
                                    case UO_HEAD:
                                            toRead = subHead.getHeaderSize();
                                            toRead -= BaseBlock.BaseBlockSize;
                                            toRead -= BlockHeader.blockHeaderSize;
                                            toRead -= SubBlockHeader.SubBlockHeaderSize;
                                            byte[] uoHeaderBuffer = new byte[toRead];
                                            rof.readFully(uoHeaderBuffer, toRead);
                                            SubBlockHeader uoHeader = new SubBlockHeader(subHead);
                                            uoHeader.print();
                                            headers.add(uoHeader);
                                            break;
                                    default:
                                            break;
                                    }

                                    break;
                            }
                            default:
                                    System.out.println("Unknown Header");
                                    throw new IOException("Not a RAR archive");
                    }
            }
		}
	}

	/**
	 * Extract the file specified by the given header and write it to the
	 * supplied output stream
	 * 
	 * @param hd
	 *            the header to be extracted
	 * @param os
	 *            the outputstream
	 * @throws IOException
	 */
	public void extractFile(FileHeader hd, OutputStream os) throws IOException {
		if (!headers.contains(hd)) {
			throw new IOException("Header not found in archive");
		}
		doExtractFile(hd, os);
	}
	
	/**
         * Returns an {@link InputStream} that will allow to read the file and
         * stream it. Please note that this method will create a new Thread and an a
         * pair of Pipe streams.
         * 
         * @param hd the header to be extracted
         * @throws IOException
         *             if any IO error occur
         */
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public InputStream getInputStream(final FileHeader hd) throws IOException {
            final PipedInputStream in = new PipedInputStream(32 * 1024);
            final PipedOutputStream out = new PipedOutputStream(in);

            // creates a new thread that will write data to the pipe. Data will be
            // available in another InputStream, connected to the OutputStream.
            new Thread(new Runnable() {
                    public void run() {
                            try {
                                extractFile(hd, out);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    }
            }).start();

            return in;
        }	

	private void doExtractFile(FileHeader hd, OutputStream os) throws IOException {
		dataIO.init(os);
		dataIO.init(hd);
		dataIO.setUnpFileCRC(this.isOldFormat() ? 0 : 0xffFFffFF);
		if (unpack == null) {
			unpack = new Unpack(dataIO);
		}
		if (!hd.isSolid()) {
			unpack.init();
		}
		unpack.setDestSize(hd.getFullUnpackSize());
		try {
			unpack.doUnpack(hd.getUnpVersion(), hd.isSolid());
			// Verify file CRC
			hd = dataIO.getSubHeader();
			long actualCRC = hd.isSplitAfter() ? ~dataIO.getPackedCRC()	: ~dataIO.getUnpFileCRC();
			int expectedCRC = hd.getFileCRC();
			if (actualCRC != expectedCRC) {
				throw new IOException("Encrypted archive header");
			}
		} catch (IOException e) {
			unpack.cleanUp();
            throw e;
		} catch (OutOfMemoryError e) {
			unpack.cleanUp();
			throw new IOException(e.getLocalizedMessage());
		}
	}

	/**
	 * @return returns the main header of this archive
	 */
	public MainHeader getMainHeader() {
		return newMhd;
	}

	/**
	 * @return whether the archive is old format
	 */
	public boolean isOldFormat() {
		return markHead.isOldFormat();
	}

	/** Close the underlying compressed file. */
	public void close() throws IOException {
		if (rof != null) {
			rof.close();
			rof = null;
		}
		if (unpack != null) {
			unpack.cleanUp();
		}
	}

	/**
	 * First, checks if a file is encrypted.
	 * Second, checks if the file's contents are password protected.
	 * Returns true on either account, otherwise false
	 *
	 * @return True if encrypted/password protected, otherwise false
	 * */
	public boolean isPasswordProtected(){

        if (this.getFileHeaders().size() > 0){
            FileHeader h = this.getFileHeaders().get(0);
            File f = null;
            try {
                f = File.createTempFile(h.getFileNameString(), null, MainActivity.mainActivity.getTempDir());
                extractFile(h, new FileOutputStream(f));
                f.delete();
            } catch (Exception e) {
                e.printStackTrace();
                if (f != null) f.delete();
                return true;
            }
        }
        return false;
	}
	
}
