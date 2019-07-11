package com.yunfeng.tools.phoneproxy.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.yunfeng.tools.phoneproxy.R;
import com.yunfeng.tools.phoneproxy.view.widget.FileDialog;
import com.yunfeng.tools.phoneproxy.view.widget.FileDialogView;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FileSelectFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_file_select, container);
        final FileDialogView pickerView = view.findViewById(R.id.picker);
        pickerView.setFileMode(FileDialog.FILE_MODE_OPEN_MULTI);
        pickerView.setInitialPath(Environment.getExternalStorageDirectory().getAbsolutePath());
        pickerView.openFolder();
        Button cancelButton = (Button) pickerView.findViewById(R.id.button_dialog_file_cancel);
        Button okButton = (Button) pickerView.findViewById(R.id.button_dialog_file_ok);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileSelectFragment.this.getActivity() != null) {
                    FileSelectFragment.this.getActivity().setResult(RESULT_CANCELED);
                }
                FileSelectFragment.this.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<File> files = pickerView.getSelectedFiles();
                if (files != null && files.size() > 0) {
                    File file = files.get(0);
                    Intent intent = new Intent();
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    if (FileSelectFragment.this.getActivity() != null) {
                        FileSelectFragment.this.getActivity().setResult(RESULT_OK, intent);
                    }
                }
                FileSelectFragment.this.dismiss();
            }
        });
        return view;
    }
}