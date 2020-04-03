package com.tydic.dcm.ftran.impl;

import com.alibaba.fastjson.util.IOUtils;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.ftran.FileRecord;
import com.tydic.dcm.ftran.Trans;
import com.tydic.dcm.util.tools.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.Vector;

/**
 * Ftp连接类
 *
 * @author Yuanh
 */
public class FtpTran implements Trans {
    //传输编码方式
    public static final String FTP_ENCODE_UTF8 = "UTF-8";
    public static final String FTP_ENCODE_GBK = "GBK";
    public static final String FTP_ENCODE_ISO = "ISO-8859-1";
    //传输缓存大小
    public static final int FTP_BUFFER_SIZE = 1024 * 1024;
    //日志对象
    private static Logger logger = Logger.getLogger(FtpTran.class);
    //ftpClient对象
    public FTPClient tranClient = null;
    //主机IP
    private String ip;
    //主机端口
    private int port;
    //登录用户名
    private String userName;
    //登录密码
    private String password;
    //ftp连接超时时间
    private int timeout;
    //链路ID,主要是用来输出日志
    private String devId;

    //主被动模式
    private Boolean isPasvMode = true;

    /**
     * Ftp对象
     *
     * @param ip       主机IP
     * @param port     主机端口
     * @param userName 登录账户名称
     * @param password 登录账户密码
     */
    public FtpTran(String ip, int port, String userName, String password, int timeout) {
        this(ip, port, userName, password, timeout, "");
    }

    /**
     * Ftp对象
     *
     * @param ip       主机IP
     * @param port     主机端口
     * @param userName 登录账户名称
     * @param password 登录账户密码
     */
    public FtpTran(String ip, int port, String userName, String password, int timeout, String devId) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.timeout = timeout;
        this.devId = devId;
    }

    public static void main(String[] args) {
//        String ip = "192.168.161.89";
//        int port = 21;
//        String userName = "bp";
//        String password = "bp";
//        int timeout = 30000;
//
//        String remotePath = "/public/bp/DCM_SERVER/SOURCE_COLL";

        String ip = "192.168.161.26";
        int port = 21;
        String userName = "bp_dcf";
        String password = "dic123";
        int timeout = 30000;

        String remotePath = "/home/bp_dcf/DCM_SERVER/";
        FtpTran tran = new FtpTran(ip, port, userName, password, timeout);
        try {
            tran.login();
            System.out.println("登录成功...");
            for (int i= 0; i<1000; i++) {
                boolean isExistPath = tran.isExistPath(remotePath);
                System.out.println("输出结果--->" + isExistPath);
                Thread.sleep(100);
            }


//            Vector<FileRecord> fileList = tran.getFileList(remotePath, "");
//            //System.out.println("结果: " + ObjectUtils.toString(fileList));
//            for (int i=0; i<fileList.size(); i++) {
//                String filename = fileList.get(i).getFileName();
//                String filepath = fileList.get(i).getFilePath();
//
//            }

            //ByteArrayOutputStream bos = tran.getFileStream("/a.json");

            //IOUtils.close(bos);

            //bos = tran.getFileStream("/opt/data2/a.json");

            //IOUtils.close(bos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断目录是否存在
     *
     * @param filePath
     * @return
     */
    public boolean isExistPath(String filePath) {
        logger.debug("begin check path exist, path: " + filePath + ", devId:" + devId);
        Boolean existFlag = false;
        try {
            //先切换到根目录
            tranClient.changeWorkingDirectory("/");
            //切换到参数对应的目录，用来判断目录是否存在
            existFlag = tranClient.changeWorkingDirectory(filePath);
        } catch (IOException e) {
            //throw new RuntimeException("check path exists, filePath: " + filePath + " fail, devId: " + devId, e);
            logger.error("check path exists fail, filePath: " + filePath + ", devId: " + devId, e);
        }
        logger.debug("end check path exist, result: " + existFlag + ", devId: " + devId);
        return existFlag;
    }

    /**
     * 获取文件路径
     * @param filePath
     * @param fileNamePattern:文件名匹配规则，列举所有设置为null，ftp设置此规则有可能失败
     * @return
     * @throws Exception
     */
    public Vector<FileRecord> getFileList(String filePath,String fileNamePattern) throws Exception {
        logger.debug("begin get ftp remote file list, path: " + filePath + ",fileNamePattern:" + fileNamePattern + ", devId: " + devId);
        Vector<FileRecord> fileVectors = new Vector<FileRecord>();
        try {
            FTPFile[] ftpFiles = null;

            // 如果尝试3次获取远程文件列表失败，切换模式进行再次获取
            boolean convertMode = PropertiesUtil.getValueByKey(ParamsConstant.FTP_AUTO_CONVERT_MODE, true);
            boolean success = false;
            try {
                ftpFiles = this.retryGetFileList(filePath,fileNamePattern, ParamsConstant.FTP_GET_PUT_TRY_COUNT);

                success = true;
            } catch (Exception e) {
                logger.error("FTP getFileList fail,devId:" + devId, e);
            }

            //切换模式重试
            if (!success && convertMode) {
                this.isPasvMode = !this.isPasvMode;
                logger.info("ftp mode convert,isPasvMode:" + isPasvMode + ",devId:" + devId);
                boolean isConnected = this.reconnect();
                if (isConnected) {
                    ftpFiles = this.retryGetFileList(filePath, fileNamePattern, ParamsConstant.FTP_GET_PUT_TRY_COUNT);
                    success = true;
                }
            }

            logger.info("get ftp remote file list ok,ftpFiles size:" + ArrayUtil.getSize(ftpFiles) + ",filePath: "
                    + filePath + ",devId:" + devId);
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (int i = 0; i < ftpFiles.length; i++) {
                    String fileName = StringUtils.trim(ftpFiles[i].getName());
                    long fileLength = ftpFiles[i].getSize();
                    Date fileTime = ftpFiles[i].getTimestamp().getTime();

                    // 排除父目录
                    if (".".equals(fileName) || "..".equals(fileName)) {
                        continue;
                    }

                    //获取当前时间最新修改时间，精确到秒
                    String fileNameNew = StringUtils.removeEnd(filePath, "/") + "/" + fileName;
                    String modifyTime = tranClient.getModificationTime(fileNameNew);
                    System.out.println("file last modify time: " + modifyTime + ", devId: " + devId);
                    if (StringUtils.isNotBlank(modifyTime)) {
                        int zoneTimes = ftpFiles[i].getTimestamp().getTimeZone().getOffset(0);
                        long modifyTimes = DateUtil.parse(modifyTime, "yyyyMMddHHmmss").getTime();
                        long currZoneTimes = modifyTimes + zoneTimes;
                        fileTime = DateUtil.formatToDate(new Date(currZoneTimes), DateUtil.allPattern);
                    }
                    logger.info("fileName: " + fileNameNew + ", fileTime: " + fileTime + ", devId: " + devId);
                    System.out.println("fileName: " + fileNameNew + ", fileTime: " + fileTime + ", devId: " + devId);

                    FileRecord fileRecord = new FileRecord();
                    fileRecord.setFileName(fileName);

                    // 只获取普通文件或者目录文件
                    if (ftpFiles[i].isFile()) {
                        fileRecord.setFileType(FileRecord.FILE);
                    } else if (ftpFiles[i].isDirectory()) {
                        fileRecord.setFileType(FileRecord.DIR);
                    } else {
                        logger.debug("file type is error, fileName:" + fileName + ", devId: " + devId);
                        continue;
                    }

                    fileRecord.setFileLength(fileLength);
                    fileRecord.setFilePath(FileTool.exactPath(filePath));
                    fileRecord.setTime(fileTime);
                    fileVectors.add(fileRecord);
                }
            }
        } catch (Exception e) {
            logger.error("get ftp remote file list fail,devId: " + devId, e);
            throw e;
        }
        logger.debug("end get ftp remote file list, file list size:" + ArrayUtil.getSize(fileVectors) + ", devId: " + devId);
        return fileVectors;
    }

    /**
     * Ftp登录
     */
    public void login() throws Exception {
        if (tranClient != null && tranClient.isConnected()) {
            return;
        }
        try {
            //登录重试3次
            boolean isLoginSuccess = false;
            for (int retry = 0; retry < ParamsConstant.FTP_LOGIN_TRY_COUNT; ++retry) {
                logger.debug("[" + (retry + 1) + "] times ftp login, IP:" + this.ip + ", PORT: " + this.port + ", USERNAME: " + this.userName + ", devId: " + this.devId);

                tranClient = new FTPClient();
                //将ftp发送命名和接收命名输出到log4j
                tranClient.addProtocolCommandListener(new ProtocolCmdListenerImpl(devId));

                logger.debug("config ftp timeout:" + timeout + ",devId:" + devId);
                // 重要：设置ftp命令socket读取数据超时时间
                this.tranClient.setDefaultTimeout(timeout);
                // 设置ftp数据socket连接超时时间
                this.tranClient.setConnectTimeout(timeout);
                // 设置ftp数据socket读取数据超时时间
                tranClient.setDataTimeout(timeout);

                //连接Ftp
                tranClient.connect(ip, port);

                // 设置ftp命令socket读取数据超时时间
                tranClient.setSoTimeout(timeout);

                //ftp登录
                isLoginSuccess = tranClient.login(userName, password);
                if (isLoginSuccess) {
                    break;
                } else {
                    //关闭连接
                    this.close();
                }
            }

            //判断ftp登录是否成功
            if (!isLoginSuccess) {
                throw new RuntimeException("ftp login fail,devId:" + this.devId);
            }

            //设置ftp传输模式,Java中通常内网用被动模式,外网连接用主动模式
            if (this.isPasvMode) {
                tranClient.enterLocalPassiveMode();
            } else {
                tranClient.enterLocalActiveMode();
            }

            //设置读取文件缓存
            tranClient.setBufferSize(FTP_BUFFER_SIZE);
            //FTP编码方式
            tranClient.setControlEncoding(FTP_ENCODE_UTF8);
            //Ftp文件传输方式
            tranClient.setFileType(FTP.BINARY_FILE_TYPE);
            //由于apache不支持中文语言环境，通过定制类解析中文日期类型
            tranClient.configure(new FTPClientConfig("com.tydic.dcm.ftran.impl.UnixFTPEntryParser"));

            logger.debug("ftp server connection success, IP:" + this.ip + ", PORT:" + this.port + ", devId:" + devId);
        } catch (Exception e) {
            logger.error("ftp server connection fail, IP:" + this.ip + ", PORT:" + this.port + ", devId: " + devId, e);
            this.close();
            throw e;
        }
    }

    /**
     * ftp重连操作
     *
     * @Title: reconnect
     * @Description: ftp重连操作
     * @return: void
     * @author: tianjc
     * @date: 2017年12月3日 下午4:12:16
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    public boolean reconnect() {
        boolean result = false;
        logger.info("begin reconnect ftp,devId:" + this.devId);
        try {
            this.close();

            this.login();

            result = true;
            logger.info("finished reconnect ftp,devId:" + this.devId);
        } catch (Exception e) {
            logger.error("ftp reconnect fail,devId:" + this.devId, e);
        }

        return result;
    }

    /**
     * 重试下载文件
     *
     * @param remotePath
     * @param localPath
     * @return
     */
    private boolean retryGet(String remotePath, String localPath, int retryTimes) {
        boolean isSuccess = false;
        //使用链路默认主被动模式
        for (int i = 0; i < retryTimes; ++i) {
            logger.info("[" + (i + 1) + "] times get file, remotePath: " + remotePath + ",localPath:" + localPath + ",devId:" + this.devId);

            if (i != 0) {
                boolean isConnected = this.reconnect();
                if (!isConnected) {
                    continue;
                }
            }

            FileOutputStream fos = null;
            try {
                File localFile = new File(localPath);
                if (localFile.exists()) {
                    logger.warn("local file exists:" + localPath + ",devId:" + devId);
                } else {
                    localFile.createNewFile();
                }

                fos = new FileOutputStream(localFile);
                boolean isTransferOK = tranClient.retrieveFile(remotePath, fos);
                logger.debug("retry get file, fileName:" + remotePath + ", result:" + isTransferOK + ", devId: " + devId);

                if (!isTransferOK) {
                    throw new RuntimeException("ftp file transfer fail. devId: " + devId);
                }
                fos.flush();

                isSuccess = true;
                break;
            } catch (Exception e) {
                logger.error("retry get file fail, fileName: " + remotePath + ", devId: " + devId, e);
            } finally {
                IOUtils.close(fos);
            }
        }

        return isSuccess;
    }

    /**
     * 重试获取文件列表
     * @param filePath
     * @param fileNamePattern
     * @param retryTimes
     * @return
     */
    private FTPFile[] retryGetFileList(String filePath,String fileNamePattern, int retryTimes) {
        FTPFile[] ftpFiles = null;

        boolean isSuccess = false;
        //使用链路默认主被动模式
        for (int i = 0; i < retryTimes; ++i) {
            logger.info("[" + (i + 1) + "] times get file list, filePath: " + filePath + ",isPasvMode:" + this.isPasvMode + ",devId:" + this.devId);

            if (i != 0) {
                boolean isConnected = this.reconnect();
                if (!isConnected) {
                    continue;
                }
            }

            try {
                boolean isChanged = tranClient.changeWorkingDirectory(filePath);
                //切换目录成功
                if (isChanged) {
                    //是否对文件名进行过滤,需要ftp支持
                    if(StringUtils.isNotBlank(fileNamePattern)){
                        ftpFiles = tranClient.listFiles(fileNamePattern);
                    } else {
                        ftpFiles = tranClient.listFiles();
                    }

                    isSuccess = true;
                    break;
                } else {
                    logger.error("changeWorkingDirectory fail, filePath: " + filePath + ", devId: " + devId);
                }
            } catch (Exception e) {
                logger.error("retry get file list fail, filePath: " + filePath + ", devId: " + devId, e);
            }
        }

        if (!isSuccess) {
            throw new RuntimeException("retry get file list fail,devId:" + devId);
        }

        return ftpFiles;
    }

    /**
     * 重试上传文件
     *
     * @param localPath
     * @param remotePath
     * @return
     */
    private boolean retryPut(String localPath, String remotePath, int retryTimes) {
        boolean isSuccess = false;
        //使用链路默认主被动模式
        for (int i = 0; i < retryTimes; ++i) {
            logger.info("[" + (i + 1) + "] times put file,localPath: " + localPath + ",remotePath:" + remotePath + ",devId:" + this.devId);

            if (i != 0) {
                boolean isConnected = this.reconnect();
                if (!isConnected) {
                    continue;
                }
            }

            FileInputStream fis = null;
            try {
                // 本地文件
                File file = new File(localPath);
                // 将本地文件转化为文件输入流
                fis = new FileInputStream(file);
                // 向远程主机Ftp写入
                Boolean isUploadOk = tranClient.storeFile(new String(remotePath.getBytes(FTP_ENCODE_GBK), FTP_ENCODE_ISO), fis);
                if (!isUploadOk) {
                    throw new RuntimeException("put file fail,devId: " + devId);
                }

                isSuccess = true;
                break;
            } catch (Exception e) {
                logger.error("put file fail,localPath: " + localPath + ",remotePath:" + remotePath + ", devId:" + devId, e);
            } finally {
                IOUtils.close(fis);
            }
        }

        return isSuccess;
    }

    /**
     * 下载远程文件到本地目录
     *
     * @param remotePath 远程目录+文件名称
     * @param localPath  本地目录+文件名称
     */
    public void get(String remotePath, String localPath) throws Exception {
        logger.debug("begin get ftp remote files, remotePath: " + remotePath + ", localPath: " + localPath + ", devId: "
                + devId);
        try {
            // 如果尝试3次获取远程文件列表失败，切换模式进行再次获取
            boolean convertMode = PropertiesUtil.getValueByKey(ParamsConstant.FTP_AUTO_CONVERT_MODE, false);
            boolean success = false;
            try {
                success = this.retryGet(remotePath, localPath, ParamsConstant.FTP_GET_PUT_TRY_COUNT);
            } catch (Exception e) {
                logger.error("FTP get fail,devId:" + devId, e);
            }

            //切换模式重试
            if (!success && convertMode) {
                this.isPasvMode = !this.isPasvMode;
                logger.info("ftp mode convert,isPasvMode:" + isPasvMode + ",devId:" + devId);
                boolean isConnected = this.reconnect();
                if (isConnected) {
                    try {
                        success = this.retryGet(remotePath, localPath, ParamsConstant.FTP_GET_PUT_TRY_COUNT);
                    } catch (Exception e) {
                        logger.error("FTP get fail,devId:" + devId, e);
                    }
                }
            }

            if (!success) {
                throw new RuntimeException("ftp file transfer fail. devId: " + devId);
            }
        } catch (Exception e) {
            logger.error("ftp file transfer fail. devId: " + devId, e);
            throw e;
        }
        logger.debug("end get ftp remote file to local ok. devId: " + devId);
    }

    /**
     * 上传本地文件到远程主机
     *
     * @param localPath  本地文件(包含文件绝对路径+文件名)
     * @param remotePath 远程文件(包含文件绝对路径+文件名)
     * @return
     */
    @Override
    public void put(String localPath, String remotePath) throws Exception {
        logger.debug("ftp begin put file to remote path, localPath: " + localPath + ", remotePath: " + remotePath
                + ", devId: " + devId);
        try {
            // 如果尝试3次获取远程文件列表失败，切换模式进行再次获取
            boolean convertMode = PropertiesUtil.getValueByKey(ParamsConstant.FTP_AUTO_CONVERT_MODE, false);
            boolean success = false;
            try {
                success = this.retryPut(localPath, remotePath, ParamsConstant.FTP_GET_PUT_TRY_COUNT);
            } catch (Exception e) {
                logger.error("FTP put fail,devId:" + devId, e);
            }

            //切换模式重试
            if (!success && convertMode) {
                this.isPasvMode = !this.isPasvMode;
                logger.info("ftp mode convert,isPasvMode:" + isPasvMode + ",devId:" + devId);
                boolean isConnected = this.reconnect();
                if (isConnected) {
                    try {
                        success = this.retryPut(localPath, remotePath, ParamsConstant.FTP_GET_PUT_TRY_COUNT);
                    } catch (Exception e) {
                        logger.error("FTP put fail,devId:" + devId, e);
                    }
                }
            }

            if (!success) {
                throw new RuntimeException("ftp file upload fail,devId: " + devId);
            }
        } catch (Exception e) {
            logger.error("ftp put file to remote path fail,devId: " + devId, e);
            throw e;
        }
        logger.debug("ftp end put file to remote path. devId: " + devId);
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    public boolean delete(String filePath) {
        logger.debug("begin ftp delete remote file, file path:" + filePath + ", devId: " + devId);
        boolean isDelOK = false;
        try {
            isDelOK = tranClient.deleteFile(filePath);
        } catch (IOException e) {
            logger.error("delete remote fail fail, devId: " + devId, e);
        }
        logger.debug("end ftp delete remote file, result:" + isDelOK + ", devId: " + devId);
        return isDelOK;
    }

    /**
     * 文件重命令
     *
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public boolean rename(String sourceFile, String targetFile) {
        logger.debug("begin rename file, source file:" + sourceFile + ", target file:" + targetFile + ", devId: " + devId);
        boolean result = false;
        try {
            if (StringUtils.equalsIgnoreCase(sourceFile, targetFile)) {
                result = true;
            } else {
                result = tranClient.rename(sourceFile, targetFile);
            }
        } catch (IOException e) {
            logger.error("file rename fail. devId: " + devId, e);
        }
        logger.debug("end rename file, result:" + result + ", devId: " + devId);
        return result;
    }

    /**
     * 切换目录
     *
     * @param remotePath
     * @return
     */
    public boolean cd(String remotePath) {
        logger.debug("ftp begin change file dir, remotePath:" + remotePath + ", devId: " + devId);
        boolean result = true;
        try {
            result = tranClient.changeWorkingDirectory(remotePath);
        } catch (IOException e) {
            result = false;
            logger.error("change file dir fail. LINK ID: " + devId, e);
            e.printStackTrace();
        }
        logger.debug("ftp end change file dir, result:" + result + ", devId: " + devId);
        return result;
    }

    /**
     * 创建目录(可以创建多级目录)
     *
     * @param dirName
     */
    public boolean mkdir(String dirName) {
        logger.debug("ftp begin mkdir, dir:" + dirName + ", devId: " + devId);
        boolean rst = true;
        try {
            Vector<String> filePaths = StringTool.tokenStringChar(dirName, File.separator + "//\\");
            if (filePaths != null && filePaths.size() > 0) {
                String tempPath = "";
                for (int i = 0; i < filePaths.size(); i++) {
                    tempPath += filePaths.get(i);
                    tempPath = FileTool.exactPath(tempPath);
                    if (!isExistPath(tempPath)) {
                        tranClient.makeDirectory(tempPath);
                    }
                }
            }
            String finalDirName = FileTool.exactPath(dirName);
            rst = isExistPath(finalDirName);
        } catch (IOException e) {
            rst = false;
            logger.error("mkdir fail. LINK ID: " + devId, e);
        }
        logger.debug("ftp end mkdir, result:" + rst + ", devId: " + devId);
        return rst;
    }

    /**
     * 关闭Ftp连接
     */
    public void close() {
        try {
            if (tranClient != null) {
                try {
                    if (!FTPReply.isPositiveCompletion(this.tranClient.quit())) {
                        logger.warn("ftp quit fail");
                    }
                } catch (Exception e) {
                    logger.error("send quit command to ftp fail", e);
                }

                this.tranClient.disconnect();
            }
        } catch (IOException e) {
            logger.error("close ftp fail, IP:" + this.ip + ", PORT:" + this.port + ", devId: " + devId, e);
        }
        logger.debug("close ftp success, IP:" + this.ip + ", PORT:" + this.port + ", devId: " + devId);
    }

    /**
     * 获取当前ftp传输模式
     *
     * @return 当前传输模式
     */
    @Override
    public boolean getPasvMode() throws Exception {
        return this.isPasvMode;
    }

    /**
     * 设置当前ftp传输模式
     *
     * @param isPasvMode 传输模式
     */
    @Override
    public void setPasvMode(Boolean isPasvMode) throws Exception {
        this.isPasvMode = isPasvMode;
    }

    @Override
    public ByteArrayOutputStream getFileStream(String remotePath) throws Exception {
        logger.debug("begin get ftp remote files, remotePath: " + remotePath + ", devId: " + devId);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            boolean receiveResult = this.tranClient.retrieveFile(remotePath, bos);

            //获取响应码失败
            if (!receiveResult) {
                throw new RuntimeException("get ftp reply code fail");
            }
        } catch (Exception e) {
            IOUtils.close(bos);

            throw e;
        }

        return bos;
    }

    @Override
    public boolean putFileStream(InputStream srcInputStream, String dstPath) throws Exception {
        //临时文件后缀
        //String tmpFileName = dstPath + "." + DcmSystem.random(10000) + ".tmp";

        logger.debug("begin put to ftp, remotePath: " + dstPath + ", devId: " + devId);

        boolean result = this.tranClient.storeFile(dstPath, srcInputStream);
        logger.debug("begin put to ftp, result: "+result+ ", devId: " + devId);
//        if (result) {
//            result = this.tranClient.rename(tmpFileName, dstPath);
//        }

        return result;
    }

    @Override
    public String toString() {
        return "FtpTran [ip=" + ip + ", port=" + port + ", userName="
                + userName + ", password=" + password + ", devId=" + devId + "]";
    }
}
