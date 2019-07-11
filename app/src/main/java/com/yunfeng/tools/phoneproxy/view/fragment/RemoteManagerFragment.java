package com.yunfeng.tools.phoneproxy.view.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.yunfeng.tools.phoneproxy.R;
import com.yunfeng.tools.phoneproxy.ftp.FTPClientFunctions;
import com.yunfeng.tools.phoneproxy.util.ThreadPool;
import com.yunfeng.tools.phoneproxy.view.custom.FtpFileAdapter;
import com.yunfeng.tools.phoneproxy.view.custom.FtpFileItem;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

interface OnFtpOprationListener {
    void onConnected();

    void onLoginSucces();

    void onChangedDir(List<FTPFile> list);
}

enum RemoteLocation {
    FTP, LOCAL
}

public class RemoteManagerFragment extends Fragment implements OnSelectedFilesListener, OnFtpOprationListener {

    private static final String TAG = "RemoteManagerFragment";
    private View layoutFtpLoginPanel;
    private View layoutServerDirPanel;
    private ListView listCurDir;
    private ListView listSelFile;
    private LinkedList<File> selectedFiles = new LinkedList<>();

    private FtpFileAdapter mCurDirAdapter;
    private FtpFileAdapter mSelFilesAdapter;

    public static RemoteManagerFragment newInstance() {
        return new RemoteManagerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_manager_fragment, container, false);
        if (view != null) {
            final EditText username_et = view.findViewById(R.id.ftp_et_username);
            final EditText password_et = view.findViewById(R.id.ftp_et_password);
            final EditText username_ip = view.findViewById(R.id.ftp_et_ip);
            final EditText password_port = view.findViewById(R.id.ftp_et_port);
            final Button btnFtpLogin = view.findViewById(R.id.ftp_btn_login);
            final Button btnFtpUpload = view.findViewById(R.id.ftp_btn_upload);
            final Button btnFtpDisConnect = view.findViewById(R.id.ftp_btn_disconnect);
            final Button btnFtpReturn = view.findViewById(R.id.ftp_btn_return);

            layoutFtpLoginPanel = view.findViewById(R.id.ftp_login_panel);
            layoutServerDirPanel = view.findViewById(R.id.server_dir_panel);

            listCurDir = view.findViewById(R.id.current_dir_list);
            mCurDirAdapter = new FtpFileAdapter(view.getContext(), R.layout.ftp_list_item);
            listCurDir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FtpFileItem itemData = (FtpFileItem) parent.getAdapter().getItem(position);
                    if (itemData.type == FTPFile.DIRECTORY_TYPE) {
                        RemoteManagerFragment.this.changeDirectory(itemData.name);
                    } else {

                    }
                }
            });
            listCurDir.setAdapter(mCurDirAdapter);

            listSelFile = view.findViewById(R.id.selected_files_list);
            listSelFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FtpFileItem itemData = (FtpFileItem) parent.getAdapter().getItem(position);
                    if (itemData.type == FTPFile.DIRECTORY_TYPE) {
                        File file = new File(itemData.path);
                        if (position == 0) {
                            File parentFile = file.getParentFile();
                            if (file.exists() && parentFile != null && parentFile.exists()) {
                                RemoteManagerFragment.this.listFilesWithDir(parentFile);
                            } else {
                                RemoteManagerFragment.this.listFilesWithDir(Environment.getExternalStorageDirectory());
                            }
                        } else {
                            RemoteManagerFragment.this.listFilesWithDir(file);
                        }
                    } else {

                    }
                }
            });
            mSelFilesAdapter = new FtpFileAdapter(view.getContext(), R.layout.ftp_list_item);
            listSelFile.setAdapter(mSelFilesAdapter);
            btnFtpReturn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    returnParent();
                }
            });
            btnFtpLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = username_et.getText().toString();
                    String password = password_et.getText().toString();
                    String ip = username_ip.getText().toString();
                    String port = password_port.getText().toString();
                    login(username, password, ip, port);
                }
            });
            btnFtpUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upload();
                }
            });
            btnFtpDisConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disconnect();
                }
            });
            Button btnChooseFile = view.findViewById(R.id.ftp_btn_choose);
            btnChooseFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    startActivityForResult(intent, 1);
                    FragmentManager manager = RemoteManagerFragment.this.getFragmentManager();
                    if (manager != null) {
                        FileSelectFragment newFragment = new FileSelectFragment();
                        newFragment.setListener(RemoteManagerFragment.this);
                        newFragment.show(manager, "FileSelectFragment");
                    }
                }
            });
            listFilesWithDir(Environment.getExternalStorageDirectory());
        }
        return view;
    }

    private void listFilesWithDir(File fileSrc) {
        if (fileSrc != null && fileSrc.isDirectory()) {
            mSelFilesAdapter.clear();
            FtpFileItem root = new FtpFileItem();
            root.name = "..";
            root.path = fileSrc.getAbsolutePath();
            root.type = FTPFile.DIRECTORY_TYPE;
            mSelFilesAdapter.add(root);
            File[] files = fileSrc.listFiles();
            if (files != null) {
                for (File file : files) {
                    FtpFileItem item = new FtpFileItem();
                    item.name = file.getName();
                    item.size = String.valueOf(file.length());
                    item.date = String.valueOf(file.lastModified());
                    item.path = file.getAbsolutePath();
                    item.prop = "";
                    if (file.isDirectory()) {
                        item.type = FTPFile.DIRECTORY_TYPE;
                    } else {
                        item.type = FTPFile.FILE_TYPE;
                    }
                    mSelFilesAdapter.add(item);
                }
            }
            mSelFilesAdapter.notifyDataSetChanged();
        }
    }

    private FTPClientFunctions ftpClient = new FTPClientFunctions();

    private void returnParent() {
        ThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ftpClient.ftpReturnParent();
                List<FTPFile> list = ftpClient.ftpListCurrentFiles();
                RemoteManagerFragment.this.onChangedDir(list);
            }
        });
    }

    private void disconnect() {
        ThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ftpClient.ftpDisconnect();
                RemoteManagerFragment.this.layoutFtpLoginPanel.setVisibility(View.VISIBLE);
                RemoteManagerFragment.this.layoutServerDirPanel.setVisibility(View.GONE);
            }
        });
    }

    private void upload() {
        for (final File file : selectedFiles) {
            ThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    String pwd = ftpClient.getCurrentWorkDirectory();
                    boolean ret = ftpClient.ftpUpload(file.getAbsolutePath(), file.getName(), pwd);
                    if (!ret) {
                        Log.d(TAG, "ftpClient.ftpUploadFile error: " + file.getAbsolutePath());
                    }
                }
            });
        }
    }

    private void login(final String username, final String password, final String ip, final String port) {
        ThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                // TODO 可以首先去判断一下网络
                boolean connectResult = ftpClient.ftpConnect(ip, username, password, Integer.valueOf(port));
                if (connectResult) {
                    RemoteManagerFragment.this.onLoginSucces();
                } else {
                    Log.w(TAG, "连接ftp服务器失败");
                }
            }
        });
    }

    @Override
    public void selectedFiles(Collection<File> files) {
        selectedFiles.clear();
        selectedFiles.addAll(files);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLoginSucces() {
        Activity activity = this.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RemoteManagerFragment.this.layoutFtpLoginPanel.setVisibility(View.GONE);
                    RemoteManagerFragment.this.layoutServerDirPanel.setVisibility(View.VISIBLE);
                }
            });
        }
        changeDirectory("/");
    }

    private void changeDirectory(final String direcotry) {
        ThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                boolean changeDirResult = ftpClient.ftpChangeDir(direcotry);
                if (changeDirResult) {
                    List<FTPFile> list = ftpClient.ftpListCurrentFiles();
                    RemoteManagerFragment.this.onChangedDir(list);
                } else {
                    Log.w(TAG, "切换ftp目录失败");
                }
            }
        });
    }

    @Override
    public void onChangedDir(final List<FTPFile> list) {
        Activity activity = this.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RemoteManagerFragment.this.mCurDirAdapter.clear();
                    for (FTPFile obj : list) {
                        FtpFileItem item = new FtpFileItem();
                        item.name = obj.getName();
                        item.size = String.valueOf(obj.getSize());
                        item.date = String.valueOf(obj.getTimestamp().getTimeInMillis());
                        item.prop = obj.getUser();
                        item.type = obj.getType();
                        RemoteManagerFragment.this.mCurDirAdapter.add(item);
                    }
                    RemoteManagerFragment.this.mCurDirAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = getPath(this.getContext(), uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            String upLoadFilePath = file.toString();
                            String upLoadFileName = file.getName();
                            Log.e(TAG, "path:" + upLoadFilePath + ", name: " + upLoadFileName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @TargetApi(19)
    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                以下是打印示例：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
//                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
