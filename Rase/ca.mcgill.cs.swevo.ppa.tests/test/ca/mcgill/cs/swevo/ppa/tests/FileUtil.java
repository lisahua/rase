/*******************************************************************************
 * PPA - Partial Program Analysis for Java
 * Copyright (C) 2008 Barthelemy Dagenais
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library. If not, see 
 * <http://www.gnu.org/licenses/lgpl-3.0.txt>
 *******************************************************************************/
package ca.mcgill.cs.swevo.ppa.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
	static final int BUFF_SIZE = 100000;

	static final byte[] buffer = new byte[BUFF_SIZE];

//	public static void cleanDirectory(File path, boolean deleteDirectory) {
//		if (path.exists()) {
//			File[] files = path.listFiles();
//			for (int i = 0; i < files.length; i++) {
//				if (files[i].isDirectory()) {
//					cleanDirectory(files[i], true);
//				} else {
//					files[i].delete();
//				}
//			}
//		}
//
//		if (deleteDirectory) {
//			path.delete();
//		}
//	}
//
//	public static boolean moveDirectory(String currentLocation, String newLocation) {
//		return new File(currentLocation).renameTo(new File(newLocation));
//	}
//
//	public static boolean copyDirectory(String strPath, String dstPath) {
//		boolean success = true;
//
//		File src = new File(strPath);
//		File dest = new File(dstPath);
//
//		if (src.isDirectory()) {
//			// if(dest.exists()!=true)
//			dest.mkdirs();
//			String list[] = src.list();
//
//			for (String path : list) {
//				String dest1 = dest.getAbsolutePath() + File.separator + path;
//				String src1 = src.getAbsolutePath() + File.separator + path;
//				success = copyDirectory(src1, dest1);
//				if (!success) {
//					break;
//				}
//			}
//		} else {
//			try {
//				copyFile(new FileInputStream(src), new FileOutputStream(dest));
//			} catch (IOException ioe) {
//				success = false;
//			}
//		}
//
//		return success;
//	}
//
//	public static void copyFile(FileInputStream input, FileOutputStream output) throws IOException {
//		FileChannel sourceChannel = null;
//		FileChannel targetChannel = null;
//
//		try {
//			sourceChannel = input.getChannel();
//			targetChannel = output.getChannel();
//			sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
//		} finally {
//			sourceChannel.close();
//			targetChannel.close();
//		}
//	}
//
//	public static void copyToFile(InputStream input, FileOutputStream output) throws IOException {
//		if (input instanceof FileInputStream) {
//			copyFile((FileInputStream) input, output);
//		} else {
//			try {
//				while (true) {
//					synchronized (buffer) {
//						int amountRead = input.read(buffer);
//						if (amountRead == -1) {
//							break;
//						}
//						output.write(buffer, 0, amountRead);
//					}
//				}
//			} finally {
//				input.close();
//				output.close();
//			}
//		}
//	}

	/**
	 * <p>
	 * Retrieves the content of a file as a String.
	 * </p>
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 *             If there were any IO related errors while reading the file.
	 */
	public static String getContent(File file) throws IOException {
		StringBuffer buffer = new StringBuffer();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();

		while (line != null) {
			buffer.append(line);
			buffer.append("\n");
			line = br.readLine();
		}

		br.close();

		return buffer.toString();
	}
	
//	public static boolean createSubDirs(File file) {
//		String path = file.getAbsolutePath().replace("\\", "/");
//		int index = path.indexOf("/") + 1;
//		String base = path.substring(0, index);
//		path = path.substring(index);
//
//		return createSubDirs(base,path);
//	}
//	
//	public static boolean createSubDirs(String base, String path) {
//		String[] subDirs = path.split("/");
//		boolean success = true;
//		for (int i = 0; i < subDirs.length - 1; i++) {
//			base += File.separator + subDirs[i];
//			File folder = new File(base);
//			if (!folder.exists()) {
//				success = success && folder.mkdir();
//			}
//		}
//		return success;
//	}
//
//	public static void ensureFileNotEmpty(File file) throws IOException {
//		if (file.length() == 0) {
//			PrintStream printer = new PrintStream(new FileOutputStream(file));
//			printer.println(" ");
//		}
//		
//	}
}
