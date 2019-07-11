package com.yunfeng.tools.phoneproxy.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

class Constant {
    public static final int FTP_CONNECT_SUCCESS = 1;
    public static final int FTP_FILE_NOTEXISTS = 2;
    public static final int LOCAL_FILE_AIREADY_COMPLETE = 3;
    public static final int FTP_DOWN_CONTINUE = 4;
    public static final int FTP_DOWN_LOADING = 5;
    public static final int FTP_DOWN_SUCCESS = 6;
    public static final int FTP_DOWN_SIZEOUT = 7;
    public static final int FTP_DOWN_FAIL = 8;
    public static final int FTP_DISCONNECT_SUCCESS = 9;
    public static final int FTP_DELETEFILE_SUCCESS = 10;
    public static final int FTP_DELETEFILE_FAIL = 11;
}

interface FtpDeleteFileListener {
    void onFtpDelete(int code);
}

interface FtpProgressListener {
    void onFtpProgress(int code, long pregress, File file);
}

public class FTPClientFunctions {

    private static final String TAG = "FTPClientFunctions";

    private FTPClient ftpClient = null; // FTP客户端

    /**
     * 连接到FTP服务器
     *
     * @param host     ftp服务器域名
     * @param username 访问用户名
     * @param password 访问密码
     * @param port     端口
     * @return 是否连接成功
     */
    public boolean ftpConnect(String host, String username, String password, int port) {
        try {
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("UTF-8");
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
            conf.setServerLanguageCode("zh");
            conf.setDefaultDateFormatStr("d MMM yyyy");
            conf.setRecentDateFormatStr("d MMM HH:mm");
            conf.setServerTimeZoneId("Asia/Chinese");
//            ftpClient.configure(conf);
            Log.d(TAG, "connecting to the ftp server " + host + " ：" + port);
            ftpClient.connect(host, port);
            // 根据返回的状态码，判断链接是否建立成功
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                Log.d(TAG, "login to the ftp server");
                boolean status = ftpClient.login(username, password);
                /*
                 * 设置文件传输模式
                 * 避免一些可能会出现的问题，在这里必须要设定文件的传输格式。
                 * 在这里我们使用BINARY_FILE_TYPE来传输文本、图像和压缩文件。
                 */
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error: could not connect to host " + host);
        }
        return false;
    }

    /**
     * 断开ftp服务器连接
     *
     * @return 断开结果
     */
    public boolean ftpDisconnect() {
        // 断开ftp服务器连接
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
        }
        return false;
    }

    /**
     * ftp 文件上传
     *
     * @param srcFilePath  源文件目录
     * @param desFileName  文件名称
     * @param desDirectory 目标文件
     * @return 文件上传结果
     */
    public boolean ftpUpload(String srcFilePath, String desFileName, String desDirectory) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            status = ftpClient.storeFile(desFileName, srcFileStream);
            srcFileStream.close();
            return status;
        } catch (Exception e) {
            Log.e(TAG, "upload failed: ", e);
        }
        return status;
    }

    public void uploadFile(final InputStream srcFileStream, final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);    //图片支持二进制上传  如果采用ASCII_FILE_TYPE(默认)，虽然上传后有数据，但图片无法打开
                    boolean status = ftpClient.storeFile(name, srcFileStream);
                    Log.i(TAG, "ftpClient.storeFile: " + status);
                    srcFileStream.close();
                } catch (Exception e) {
                    Log.i(TAG, "run: " + e.toString());
                }
            }
        }).start();
    }

    public void uploadFile(String uri, String name) throws Exception {
        try {
            File file = new File(uri);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FileInputStream srcFileStream = new FileInputStream(file);
            boolean status = ftpClient.storeFile(name, srcFileStream);
            Log.i(TAG, "uploadFile: " + status);
            srcFileStream.close();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * FTP上传文件
     * filePathName 文件路径方式
     *
     * @param filePathName
     * @return
     */
    public boolean ftpUploadFile(String filePathName) {
        boolean result = false;
        try {
            File file = new File(filePathName);
            String dataDirectory = file.getParent();
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                uploadDirectoryFile(fileInputStream, dataDirectory, file.getName());
                result = true;  //上传成功
            } else {
                Log.i(TAG, "ftpUploadFile: 文件路径不存在");
            }
        } catch (Exception e) {
            Log.i(TAG, "ftpUploadFile: " + "Failure : " + e.getLocalizedMessage());
        }
        return result;
    }


    /**
     * 上传文件夹
     * 文件流 上传后指定根目录 文件名称
     *
     * @param srcFileStream 文件流
     * @param directoryName ftp存储的文件夹名称
     * @param name          ftp存储的文件名称
     */
    public void uploadDirectoryFile(InputStream srcFileStream, String directoryName, String name) throws Exception {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //判断当前FTP工作目录
            String pwd = ftpClient.printWorkingDirectory();
            //getDirectoryExist(directoryName);
            ftpClient.storeFile(name, srcFileStream);
            //changeToParentDirectory();
            srcFileStream.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public String getCurrentWorkDirectory() {
        String pwd = null;
        try {
            pwd = ftpClient.printWorkingDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pwd;
    }

    /**
     * 下载单个文件，可实现断点下载.
     *
     * @param serverPath Ftp目录及文件路径（文件夹+文件名）
     * @param localPath  本地目录文件夹目录（文件夹）
     * @param fileName   下载之后的文件名称（文件名）
     * @param listener   监听器
     * @throws IOException
     */
    public void downloadSingleFile(String serverPath, String localPath, String fileName, FtpProgressListener listener) throws Exception {
        listener.onFtpProgress(Constant.FTP_CONNECT_SUCCESS, 0, null);
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files != null && files.length == 0) {
            listener.onFtpProgress(Constant.FTP_FILE_NOTEXISTS, 0, null);
            return;
        }

        // 创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            boolean ismake = mkFile.mkdirs();
            Log.i(TAG, "是否存在" + ismake);
        }
        localPath = localPath + File.separator + fileName;
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            if (localSize >= serverSize) {
                listener.onFtpProgress(Constant.LOCAL_FILE_AIREADY_COMPLETE, 0, localFile);
                localFile.delete();
                localFile.createNewFile();
                localSize = 0;
            } else {
                listener.onFtpProgress(Constant.FTP_DOWN_CONTINUE, 0, null);
            }
        } else {
            localFile.createNewFile();
        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = localSize;
        // 开始准备下载文件
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        InputStream input = ftpClient.retrieveFileStream(serverPath);//在调用此方法后，一定要在流关闭后再调用completePendingCommand结束整个事务
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
            Log.i(TAG, "downloadSingleFile:正在下载" + step);
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 1 == 0) { // 每隔%1的进度返回一次
                    listener.onFtpProgress(Constant.FTP_DOWN_LOADING, process, null);
                }
            }

        }
        Log.i(TAG, "downloadSingleFile: 下载完毕");
        out.flush();
        out.close();
        input.close();

        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand() && localFile.length() == serverSize) {
            listener.onFtpProgress(Constant.FTP_DOWN_SUCCESS, process, localFile);
        } else if (localFile.length() != serverSize) {
            listener.onFtpProgress(Constant.FTP_DOWN_SIZEOUT, serverSize, localFile);
        } else {
            listener.onFtpProgress(Constant.FTP_DOWN_FAIL, 0, null);
        }

        // 下载完成之后关闭连接
//        this.disconnect();
        listener.onFtpProgress(Constant.FTP_DISCONNECT_SUCCESS, 0, null);

        return;
    }

    /**
     * 删除Ftp下的文件.
     *
     * @param serverPath Ftp目录及文件路径
     * @param listener   监听器
     * @throws IOException
     */
    public void deleteSingleFile(String serverPath, FtpDeleteFileListener listener) throws Exception {
        listener.onFtpDelete(Constant.FTP_CONNECT_SUCCESS);
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onFtpDelete(Constant.FTP_FILE_NOTEXISTS);
            return;
        }
        // 进行删除操作
        boolean flag = ftpClient.deleteFile(serverPath);
        if (flag) {
            listener.onFtpDelete(Constant.FTP_DELETEFILE_SUCCESS);
        } else {
            listener.onFtpDelete(Constant.FTP_DELETEFILE_FAIL);
        }
        listener.onFtpDelete(Constant.FTP_DISCONNECT_SUCCESS);
    }

    /**
     * ftp 更改目录
     *
     * @param path 更改的路径
     * @return 更改是否成功
     */
    public boolean ftpChangeDir(String path) {
        boolean status = false;
        try {
            status = ftpClient.changeWorkingDirectory(path);
        } catch (Exception e) {
            Log.e(TAG, "change directory failed: ", e);
        }
        return status;
    }

    public boolean ftpReturnParent() {
        boolean status = false;
        try {
            status = ftpClient.changeToParentDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    public List<FTPFile> ftpListCurrentFiles() {
        List<FTPFile> tmpResults = new LinkedList<FTPFile>();
        try {
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (null == file) {
                    continue;
                }
                tmpResults.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpResults;
    }

    public List<FTPFile> ftpGetCurrentDirFiles(String path, int pageSize) {
        List<FTPFile> tmpResults = new LinkedList<FTPFile>();
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                FTPFile[] files = engine.getNext(pageSize);
                for (FTPFile file : files) {
                    if (null == file) {
                        continue;
                    }
                    tmpResults.add(file);
                }
                // "page size" you want
                //do whatever you want with these files, display them, etc.
                //expensive FTPFile objects not created until needed.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpResults;
    }

    public boolean connect(String ip, int port, String userName, String pass) {
        boolean status = false;
        try {
            if (!ftpClient.isConnected()) {
                ftpClient.connect(ip, port);
                status = ftpClient.login(userName, pass);
            }
            Log.i(TAG, "connect: " + status);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    public void useCompressedTransfer(FTPClient ftpClient) {
        try {
            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //设置缓存
            ftpClient.setBufferSize(1024);
            //设置编码格式，防止中文乱码
            ftpClient.setControlEncoding("UTF-8");
            //设置连接超时时间
            ftpClient.setConnectTimeout(10 * 1000);
            //设置数据传输超时时间
//            ftpClient.setDataTimeout(10*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}