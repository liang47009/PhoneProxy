package com.yunfeng.tools.phoneproxy.view.widget;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.view.View.OnClickListener;

import com.yunfeng.tools.phoneproxy.R;
import com.yunfeng.tools.phoneproxy.util.FileUtil;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 文件对象，继承自File
 *
 * @author NashLegend
 */
class FileItem extends File {

    private static final long serialVersionUID = 2675728441786325207L;

    /**
     * 文件在文件列表中显示的icon
     */
    private int icon = R.drawable.ic_launcher_background;

    /**
     * 文件是否在列表中被选中
     */
    private boolean selected = false;

    /**
     * 文件类型，默认为FILE_TYPE_NORMAL，即普通文件。
     */
    private int fileType = FileUtil.FILE_TYPE_NORMAL;

    /**
     * 文件后缀
     */
    private String suffix = "";

    public FileItem(File file) {
        this(file.getAbsolutePath());
    }

    public FileItem(String path) {
        super(path);
        setFileTypeBySuffix();
    }

    public FileItem(URI uri) {
        super(uri);
        setFileTypeBySuffix();
    }

    public FileItem(File dir, String name) {
        super(dir, name);
        setFileTypeBySuffix();
    }

    public FileItem(String dirPath, String name) {
        super(dirPath, name);
        setFileTypeBySuffix();
    }

    /**
     * 根据后缀取得文件类型
     */
    private void setFileTypeBySuffix() {
        int type = FileUtil.getFileType(this);
        setFileType(type);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getFileType() {
        return fileType;
    }

    /**
     * 设置fileTyle,同时修改icon
     *
     * @param fileType
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
        switch (fileType) {
            case FileUtil.FILE_TYPE_APK:
                setIcon(R.drawable.format_apk);
                break;
            case FileUtil.FILE_TYPE_FOLDER:
                setIcon(R.drawable.format_folder);
                break;
            case FileUtil.FILE_TYPE_IMAGE:
                setIcon(R.drawable.format_picture);
                break;
            case FileUtil.FILE_TYPE_NORMAL:
                setIcon(R.drawable.format_unkown);
                break;
            case FileUtil.FILE_TYPE_AUDIO:
                setIcon(R.drawable.format_music);
                break;
            case FileUtil.FILE_TYPE_TXT:
                setIcon(R.drawable.format_text);
                break;
            case FileUtil.FILE_TYPE_VIDEO:
                setIcon(R.drawable.format_media);
                break;
            case FileUtil.FILE_TYPE_ZIP:
                setIcon(R.drawable.format_zip);
                break;
            case FileUtil.FILE_TYPE_HTML:
                setIcon(R.drawable.format_html);
                break;
            case FileUtil.FILE_TYPE_PDF:
                setIcon(R.drawable.format_pdf);
                break;
            case FileUtil.FILE_TYPE_WORD:
                setIcon(R.drawable.format_word);
                break;
            case FileUtil.FILE_TYPE_EXCEL:
                setIcon(R.drawable.format_excel);
                break;
            case FileUtil.FILE_TYPE_PPT:
                setIcon(R.drawable.format_ppt);
                break;
            case FileUtil.FILE_TYPE_TORRENT:
                setIcon(R.drawable.format_torrent);
                break;
            case FileUtil.FILE_TYPE_EBOOK:
                setIcon(R.drawable.format_ebook);
                break;
            case FileUtil.FILE_TYPE_CHM:
                setIcon(R.drawable.format_chm);
                break;
            default:
                setIcon(R.drawable.format_unkown);
                break;
        }
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}

class FileListAdapter extends BaseAdapter {
    private ArrayList<FileItem> list = new ArrayList<FileItem>();
    private Context mContext;
    private File currentDirectory;
    private FileDialogView dialogView;

    public FileListAdapter(Context Context) {
        mContext = Context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = new FileItemView(mContext);
            holder.fileItemView = (FileItemView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.fileItemView.setFileItem(list.get(position), this,
                dialogView.getFileMode());
        return holder.fileItemView;
    }

    class ViewHolder {
        FileItemView fileItemView;
    }

    public ArrayList<FileItem> getList() {
        return list;
    }

    public void setList(ArrayList<FileItem> list) {
        this.list = list;
    }

    /**
     * 打开文件夹，更新文件列表
     *
     * @param file
     */
    public void openFolder(File file) {
        if (file != null && file.exists() && file.isDirectory()) {
            if (!file.equals(currentDirectory)) {
                // 与当前目录不同
                currentDirectory = file;
                list.clear();
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File tmpFile = files[i];
                        if (tmpFile.isFile()
                                && (dialogView.getFileMode() == FileDialog.FILE_MODE_OPEN_FOLDER_MULTI || dialogView
                                .getFileMode() == FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)) {
                            continue;
                        }
                        list.add(new FileItem(files[i]));
                    }
                }
                files = null;
                sortList();
                notifyDataSetChanged();
            }
        }
        dialogView.getPathText().setText(file.getAbsolutePath());
    }

    /**
     * 选择当前目录下所有文件
     */
    public void selectAll() {
        int mode = dialogView.getFileMode();
        if (mode > FileDialog.FILE_MODE_OPEN_FILE_MULTI) {
            // 单选模式应该看不到全选按钮才对
            return;
        }
        for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext(); ) {
            FileItem fileItem = (FileItem) iterator.next();

            if (mode == FileDialog.FILE_MODE_OPEN_FILE_MULTI
                    && fileItem.isDirectory()) {
                // fileItem是目录，但是只能选择文件，则返回
                continue;
            }
            if (mode == FileDialog.FILE_MODE_OPEN_FOLDER_MULTI
                    && !fileItem.isDirectory()) {
                // fileItem是文件，但是只能选择目录，则返回
                continue;
            }

            fileItem.setSelected(true);
        }
        notifyDataSetChanged();
    }

    /**
     * 取消所有文件的选中状态
     */
    public void unselectAll() {
        for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext(); ) {
            FileItem fileItem = (FileItem) iterator.next();
            fileItem.setSelected(false);
        }
        notifyDataSetChanged();
    }

    /**
     * 只在选中时调用，取消选中不调用，且只由FileItemView调用
     *
     * @param fileItem
     */
    public void selectOne(FileItem fileItem) {
        int mode = dialogView.getFileMode();
        if (mode > FileDialog.FILE_MODE_OPEN_FILE_MULTI) {
            // 如果是单选
            if (mode == FileDialog.FILE_MODE_OPEN_FILE_SINGLE
                    && fileItem.isDirectory()) {
                // fileItem是目录，但是只能选择文件，则返回
                return;
            }
            if (mode == FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE
                    && !fileItem.isDirectory()) {
                // fileItem是文件，但是只能选择目录，则返回
                return;
            }
            for (Iterator<FileItem> iterator = list.iterator(); iterator
                    .hasNext(); ) {
                FileItem tmpItem = (FileItem) iterator.next();
                if (tmpItem.equals(fileItem)) {
                    tmpItem.setSelected(true);
                } else {
                    tmpItem.setSelected(false);
                }
            }
        } else {
            // 如果是多选
            if (mode == FileDialog.FILE_MODE_OPEN_FILE_MULTI
                    && fileItem.isDirectory()) {
                // fileItem是目录，但是只能选择文件，则返回
                return;
            }
            if (mode == FileDialog.FILE_MODE_OPEN_FOLDER_MULTI
                    && !fileItem.isDirectory()) {
                // fileItem是文件，但是只能选择目录，则返回
                return;
            }
            fileItem.setSelected(true);
        }

        notifyDataSetChanged();
    }

    public void sortList() {
        FileItemComparator comparator = new FileItemComparator();
        Collections.sort(list, comparator);
    }

    /**
     * 取消一个的选择，其他逻辑都在FileItemView里面
     */
    public void unselectOne() {
        dialogView.unselectCheckBox();
    }

    /**
     * @return 选中的文件列表
     */
    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> selectedFiles = new ArrayList<File>();
        for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext(); ) {
            FileItem file = iterator.next();// 强制转换为File
            if (file.isSelected()) {
                selectedFiles.add(file);
            }
        }
        return selectedFiles;
    }

    public class FileItemComparator implements Comparator<FileItem> {

        @Override
        public int compare(FileItem lhs, FileItem rhs) {
            if (lhs.isDirectory() != rhs.isDirectory()) {
                // 如果一个是文件，一个是文件夹，优先按照类型排序
                if (lhs.isDirectory()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                // 如果同是文件夹或者文件，则按名称排序
                return lhs.getName().toLowerCase()
                        .compareTo(rhs.getName().toLowerCase());
            }
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public FileDialogView getDialogView() {
        return dialogView;
    }

    public void setDialogView(FileDialogView dialogView) {
        this.dialogView = dialogView;
    }

}

/**
 * FileDialog的view
 *
 * @author NashLegend
 */
public class FileDialogView extends FrameLayout implements OnClickListener, OnCheckedChangeListener {

    private FileListAdapter adapter;
    private ListView listView;
    private EditText pathText;
    private ImageButton backButton;
    private CheckBox selectAllButton;

    private int fileMode = FileDialog.FILE_MODE_OPEN_MULTI;
    private String initialPath = "/";

    // Call by outer method
    private Button cancelButton;
    private Button okButton;

    public FileDialogView(Context context) {
        super(context);
        initView(context);
    }

    public FileDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FileDialogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dialog_file, this);

        listView = (ListView) findViewById(R.id.listview_dialog_file);
        pathText = (EditText) findViewById(R.id.edittext_dialog_file_path);
        backButton = (ImageButton) findViewById(R.id.imagebutton_dialog_file_back);
        selectAllButton = (CheckBox) findViewById(R.id.checkbox_dialog_file_all);
        cancelButton = (Button) findViewById(R.id.button_dialog_file_cancel);
        okButton = (Button) findViewById(R.id.button_dialog_file_ok);

        backButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        selectAllButton.setOnCheckedChangeListener(this);
        pathText.setKeyListener(null);

        adapter = new FileListAdapter(context);
        adapter.setDialogView(this);
        listView.setAdapter(adapter);
    }

    /**
     * 打开目录
     *
     * @param file 要打开的文件夹
     */
    public void openFolder(File file) {
        if (!file.exists() || !file.isDirectory()) {
            // 若不存在此目录，则打开根文件夹
            file = Environment.getExternalStorageDirectory();
        }
        adapter.openFolder(file);
    }

    /**
     * 打开目录
     *
     * @param path 要打开的文件夹路径
     */
    public void openFolder(String path) {
        openFolder(new File(path));
    }

    /**
     * 打开初始目录
     */
    public void openFolder() {
        openFolder(initialPath);
    }

    /**
     * 返回上级目录
     */
    private void back2ParentLevel() {
        File file = adapter.getCurrentDirectory();
        if (file != null && file.getParentFile() != null) {
            openFolder(file.getParentFile());
        }
    }

    /**
     * 选中当前目录所有文件
     */
    private void selectAll() {
        adapter.selectAll();
    }

    /**
     * 取消选中当前目录所有文件
     */
    private void unselectAll() {
        adapter.unselectAll();
    }

    public void unselectCheckBox() {
        selectAllButton.setOnCheckedChangeListener(null);
        selectAllButton.setChecked(false);
        selectAllButton.setOnCheckedChangeListener(this);
    }

    /**
     * @return 返回选中的文件列表
     */
    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> list = new ArrayList<File>();
        if (adapter.getSelectedFiles().size() > 0) {
            list = adapter.getSelectedFiles();
        } else {
            if (fileMode == FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE) {
                list.add(adapter.getCurrentDirectory());
            }
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imagebutton_dialog_file_back) {
            back2ParentLevel();
        }
    }

    public EditText getPathText() {
        return pathText;
    }

    public int getFileMode() {
        return fileMode;
    }

    public void setFileMode(int fileMode) {
        this.fileMode = fileMode;
        if (fileMode > FileDialog.FILE_MODE_OPEN_FILE_MULTI) {
            // 单选模式应该看不到全选按钮才对
            selectAllButton.setVisibility(View.GONE);
        } else {
            selectAllButton.setVisibility(View.VISIBLE);
        }
    }

    public String getInitialPath() {
        return initialPath;
    }

    public void setInitialPath(String initialPath) {
        this.initialPath = initialPath;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (selectAllButton.isChecked()) {
            selectAll();
        } else {
            unselectAll();
        }
    }

    public CheckBox getSelectAllButton() {
        return selectAllButton;
    }
}