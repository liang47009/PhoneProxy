package com.yunfeng.tools.phoneproxy.ftp;

import java.io.File;

public class FtpProgressListener {
    public void onFtpProgress(int code, long pregress, File file) {
    }
}

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