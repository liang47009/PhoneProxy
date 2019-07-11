package com.yunfeng.tools.phoneproxy.view.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yunfeng.tools.phoneproxy.R;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileAdapter extends ArrayAdapter<FtpFileItem> {

    protected int mResource;

    public FtpFileAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        TextView tvName = view.findViewById(R.id.name);
        TextView tvSize = view.findViewById(R.id.size);
        TextView tvDate = view.findViewById(R.id.date);
        TextView tvProp = view.findViewById(R.id.prop);
        final FtpFileItem item = getItem(position);
        tvName.setText(item.name);
        if (item.type == FTPFile.DIRECTORY_TYPE) {
            tvSize.setText("");
        } else {
            tvSize.setText(item.size);
        }
        tvDate.setText(item.date);
        tvProp.setText(item.prop);
        return view;
    }

}
