package com.tydic.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 文件工具箱 
 */
public class FileUtil {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(FileUtil.class);

	/**
	 * 往文件中写入内容
	 * @param fileName 文件path
	 * @param content 内容
	 * @param additional 是否追加
	 */
    public static void writeFile(String fileName, String content,boolean additional) {
    	if(fileName == null || fileName.equals("")){
    		return;
    	}
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, additional);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            log.error("错误--->"+e);
        }
    }
    
    /**
	 * 往文件中追加写入内容
	 * @param fileName 文件path
	 * @param content 内容
	 */
    public static void writeFile(String fileName, String content) {
    	if(fileName == null || fileName.equals("")){
    		return;
    	}
        try {
        	
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            log.error("错误--->"+e);
        }
    }
    /**  
     * 判断一个文件是否存在  
     *  
     * @param filePath 文件路径  
     * @return 存在返回true，否则返回false  
     */   
    public static boolean isExist(String filePath){
    	File file = new File(filePath);
    	if(file.exists()){
    		return true;
    	}
    	return false;
    }

    /** 
     * 复制文件或者目录,复制前后文件完全一样。 
     * 
     * @param resFilePath 源文件路径 
     * @param distFolder    目标文件夹 
     * @IOException 当操作发生异常时抛出 
     */ 
    public static void copyFile(String resFilePath, String distFolder) throws IOException { 
            File resFile = new File(resFilePath); 
            File distFile = new File(distFolder); 
            if (resFile.isDirectory()) { 
                    FileUtils.copyDirectoryToDirectory(resFile, distFile); 
            } else if (resFile.isFile()) { 
                    FileUtils.copyFileToDirectory(resFile, distFile, true); 
            } 
    }

    /** 
     * 删除一个文件或者目录 
     * 
     * @param targetPath 文件或者目录路径 
     * @IOException 当操作发生异常时抛出 
     */ 
    public static void deleteFile(String targetPath) throws IOException { 
            File targetFile = new File(targetPath); 
            if (targetFile.isDirectory()) { 
                    FileUtils.deleteDirectory(targetFile); 
            } else if (targetFile.isFile()) { 
                    targetFile.delete(); 
            } 
    }

    /** 
     * 移动文件或者目录,移动前后文件完全一样,如果目标文件夹不存在则创建。 
     * 
     * @param resFilePath 源文件路径 
     * @param distFolder    目标文件夹 
     * @IOException 当操作发生异常时抛出 
     */ 
    public static void moveFile(String resFilePath, String distFolder) throws IOException { 
            File resFile = new File(resFilePath); 
            File distFile = new File(distFolder); 
            if (resFile.isDirectory()) { 
                    FileUtils.moveDirectoryToDirectory(resFile, distFile, true); 
            } else if (resFile.isFile()) { 
                    FileUtils.moveFileToDirectory(resFile, distFile, true); 
            } 
    }


    /** 
     * 重命名文件或文件夹 
     * 
     * @param oldFilePath 源文件（全路径）
     * @param newFilePath 目标文件（全路径）
     * @return 操作成功标识 
     */ 
    public static boolean renameFile(String oldFilePath, String newFilePath) { 
            File oldFile = new File(oldFilePath); 
            File newFile = new File(newFilePath); 
            return oldFile.renameTo(newFile); 
    }

    /** 
     * 读取文件或者目录的大小 
     * 
     * @param distFilePath 目标文件或者文件夹 
     * @return 文件或者目录的大小，如果获取失败，则返回-1 
     */ 
    public static long genFileSize(String distFilePath) { 
            File distFile = new File(distFilePath); 
            if (distFile.isFile()) { 
                    return distFile.length(); 
            } else if (distFile.isDirectory()) { 
                    return FileUtils.sizeOfDirectory(distFile); 
            } 
            return -1L; 
    }


    /** 
     * 本地某个目录下的文件列表（不递归） 
     * 
     * @param folder ftp上的某个目录 
     * @param suffix 文件的后缀名（比如.mov.xml) 
     * @return 文件名称列表 
     */ 
    public static String[] listFilebySuffix(String folder, String suffix) { 
            IOFileFilter fileFilter1 = new SuffixFileFilter(suffix); 
            IOFileFilter fileFilter2 = new NotFileFilter(DirectoryFileFilter.INSTANCE); 
            FilenameFilter filenameFilter = new AndFileFilter(fileFilter1, fileFilter2); 
            return new File(folder).list(filenameFilter); 
    }

    /** 
     * 将字符串写入指定文件(当指定的父路径中文件夹不存在时，会最大限度去创建，以保证保存成功！) 
     * 
     * @param res            原字符串 
     * @param filePath 文件路径 
     * @return 成功标记 
     */ 
    public static boolean string2File(String res, String filePath) { 
            boolean flag = true; 
            BufferedReader bufferedReader = null; 
            BufferedWriter bufferedWriter = null; 
            try { 
                    File distFile = new File(filePath); 
                    if (!distFile.getParentFile().exists()) distFile.getParentFile().mkdirs(); 
                    bufferedReader = new BufferedReader(new StringReader(res)); 
                    bufferedWriter = new BufferedWriter(new FileWriter(distFile)); 
                    char buf[] = new char[1024];         //字符缓冲区 
                    int len; 
                    while ((len = bufferedReader.read(buf)) != -1) { 
                            bufferedWriter.write(buf, 0, len); 
                    } 
                    bufferedWriter.flush(); 
                    bufferedReader.close(); 
                    bufferedWriter.close(); 
            } catch (IOException e) { 
                    flag = false; 
                    log.error("错误--->"+e); 
            } 
            return flag; 
    }


	/**
	 * 读取文件
	 * @param inputstream 文件流对象
	 * @return
	 */
	public static String readInputStream(InputStream inputstream){

		StringBuffer stringBuffer=new StringBuffer();
		InputStreamReader reader = null;
		try {
			// 一次读多个字符
			char[] tempchars = new char[1024];
			int charread = 0;
			reader = new InputStreamReader(inputstream);
			// 读入多个字符到字符数组中，charread为一次读取字符数
			while ((charread = reader.read(tempchars)) != -1) {
				if(charread<tempchars.length){
					char[] tmp= Arrays.copyOf(tempchars, charread);
					stringBuffer.append(new String(tmp));
				}else{
					stringBuffer.append(new String(tempchars));
				}
			}

		} catch (Exception e1) {
			log.error("读取文件错误："+e1);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (inputstream != null) {
				try {
					inputstream.close();
				} catch (IOException e1) {
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * 对读取的文件内容进行替换
	 * @param inputstream 文件内容
	 * @param flagLine 获取要替换的文件行
	 * @param replaceStr 替换后字段
	 * @param breplaceStr 需要替换的字段
	 * @return
	 */
	public static String readInputStream(InputStream inputstream, String flagLine,String breplaceStr, String replaceStr){
		StringBuffer stringBuffer=new StringBuffer();
		BufferedReader bufferedReader = null;
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(inputstream);
			bufferedReader = new BufferedReader(reader);
			String lineStr = null;
			while ((lineStr = bufferedReader.readLine()) != null) {
				//if (lineStr.startsWith(flagLine) && lineStr.indexOf(breplaceStr) > 0) {
					//lineStr = lineStr.substring(0, lineStr.lastIndexOf(breplaceStr)) + replaceStr + lineStr.substring(lineStr.lastIndexOf(breplaceStr) + breplaceStr.length());
				//}

				//将选中行所有的字段后面加上网卡
				//sentinel monitor fe80::1618:77ff:fe39:19bd%em1:5003 fe80::1618:77ff:fe39:19bd%em1 5003 2
				if (lineStr.startsWith(flagLine)) {
					String prefixStr = lineStr.substring(0, lineStr.lastIndexOf(":"));
					String suffixStr = lineStr.substring(lineStr.lastIndexOf(":"));
					String suffixSepStr = suffixStr.substring(0, suffixStr.indexOf(" ")) + replaceStr;
					String suffixSep2Str = suffixStr.substring(suffixStr.indexOf(" "));
					lineStr = prefixStr + suffixSepStr + suffixSep2Str;
				}
				stringBuffer.append(lineStr).append("\n");
			}
		} catch (Exception e1) {
			log.error("读取文件错误：", e1);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (inputstream != null) {
				try {
					inputstream.close();
				} catch (IOException e1) {
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * 将字符串转化为流输出
	 * @param contentStr
	 * @return
	 */
	public static InputStream readStr2InputStream(String contentStr) {
		if (StringUtils.isEmpty(contentStr)) {
			contentStr = "";
		}
		ByteArrayInputStream inputStream = null;
		try {
			inputStream= new ByteArrayInputStream(contentStr.getBytes());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return inputStream;
	}

    /**
     * 读取文件
     * @param filePath 文件路径
     * @return
     */
    public static String readFileUnicode(String filePath){
    	File file = new File(filePath);
    	if(!file.exists()){
    		return "";
    	}
    	StringBuffer stringBuffer=new StringBuffer();
    	InputStreamReader reader = null;
    	
    	
    	  try {
    		  
    		  String code=EncodingDetect.getJavaEncode(filePath) == null ? "UTF-8" : EncodingDetect.getJavaEncode(filePath) ;
    		  if(code.toUpperCase().equals("ASCII")){
     			 code="UTF-8";
     		 }
    		  log.debug("读入文件："+file.getAbsolutePath()+",编码格式:"+code);
              // 一次读多个字符
              char[] tempchars = new char[1024];
              int charread = 0;
              reader = new InputStreamReader(new FileInputStream(filePath),code);
              // 读入多个字符到字符数组中，charread为一次读取字符数
              while ((charread = reader.read(tempchars)) != -1) {
            	  if(charread<tempchars.length){
            		  char[] tmp= Arrays.copyOf(tempchars, charread);
            		  stringBuffer.append(new String(tmp));
            	  }else{
            		  stringBuffer.append(new String(tempchars));
            	  }
              }

          } catch (Exception e1) {
              log.error("读取文件错误："+e1);
          } finally {
              if (reader != null) {
                  try {
                      reader.close();
                  } catch (IOException e1) {
                  }
              }
          }
    	return stringBuffer.toString();
    }
    
    public static boolean writeFileUnicode(String filePath,String content,boolean append){
    	File file = new File(filePath);
    	
    	OutputStreamWriter  writer=null;
    	BufferedWriter bufw=null;
    	try {
    		File parentFile = file.getParentFile();
    		if(!parentFile.exists()){
    			parentFile.mkdirs();
        	}
    		 String code=EncodingDetect.getJavaEncode(filePath) == null ? "UTF-8" : EncodingDetect.getJavaEncode(filePath) ;
    		 if(code.toUpperCase().equals("ASCII")){
    			 code="UTF-8";
    		 }
  		    log.debug("写入文件："+file.getAbsolutePath()+",编码格式:"+code);
			writer=new OutputStreamWriter(new FileOutputStream(filePath,append),code);
		    bufw = new BufferedWriter(writer);
		    bufw.write(content);
		    bufw.flush(); 
		} catch (Exception e) {
			log.error("错误--->"+e);
			return false;
		}finally{
			try {
				bufw.close();
				writer.close();
			} catch (IOException e) {
				log.error("错误--->"+e);
			}
		}
    	
    	return true;
    }
   
    
    public static List<String> getFiles(String path) throws Exception{
    	File file = new File(path);
    	List<String> fileNames = new ArrayList<>();
    	if(file.isDirectory()){
    		File[] array = file.listFiles();   
    		for(int i=0;i<array.length;i++){   
    			fileNames.add(array[i].getName());
    		}
    	}else{
    		throw new Exception("目标不是目录");
    	}
    	return fileNames;
    }
    /**
     * 根据文件类型指定目录得到文件
     * @param path 目录路径
     * @param fileType 文件类型
     * @return
     * @throws Exception
     */
    public static List<String> getFiles(String path,String fileType) throws Exception{
    	File file = new File(path);
    	List<String> fileNames = new ArrayList<>();
    	if(file.isDirectory()){
    		File[] array = file.listFiles();   
    		for(int i=0;i<array.length;i++){   
    			if(array[i].getName().lastIndexOf(fileType)>-1){
    				fileNames.add(array[i].getName());
    			}
    		}
    	}else{
    		throw new Exception("目标不是目录");
    	}
    	return fileNames;
    }
    
    public static void inputStreamtofile(InputStream ins,File file) throws Exception{
    	OutputStream os = null;
		try {
			if(!file.exists()){
				if(!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new Exception(e.getMessage());
			 
		}
    	int bytesRead = 0;
    	byte[] buffer = new byte[1024 * 256];
    	try {
			while ((bytesRead = ins.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			os.flush();
			os.close();
	    	ins.close();
		}
    	
    	}
    
	/**
	 * 为目录添加分割符
	 * 
	 * @param path
	 * @return
	 */
	public static String exactPath(String path) {
		if (path == null) {
			return null;
		} else if (path.endsWith(java.io.File.separator)) {
			return path;
		} else if (path.endsWith("/") || path.endsWith("\\")) {
			return path;
		} else {
			//			return path + java.io.File.separator;
            return path + "/";
		}
	}
	
    public static void main(String[] args) throws Exception{
    	String fileContent=FileUtil.readFileUnicode("F:\\TMP\\topology_v1.94\\conf\\dcstorm.conf");
		 System.out.println(fileContent);
        String aa = "11aa中国";
        ByteArrayInputStream stream = new ByteArrayInputStream(aa.getBytes());
         FileUtil.inputStreamtofile(stream, new File("D:\\aa\\a.txt"));
}

}

