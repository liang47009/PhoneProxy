package com.yunfeng.tools.phoneproxy.view.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yunfeng.tools.phoneproxy.R;

import org.apache.commons.net.ftp.FTPFile;

import java.util.List;

public class FtpFileAdapter extends ArrayAdapter<FtpFileItem> {

    protected int mResource;

    public FtpFileAdapter(Context context, int resource, List<FtpFileItem> objects) {
        super(context, resource, 0, objects);
        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        ImageView ivType = view.findViewById(R.id.file_type_image);
        TextView tvName = view.findViewById(R.id.name);
        TextView tvSize = view.findViewById(R.id.size);
        TextView tvDate = view.findViewById(R.id.date);
        TextView tvProp = view.findViewById(R.id.prop);
        final FtpFileItem item = getItem(position);
        if (item != null) {
            tvName.setText(item.name);
            if (item.type == FTPFile.DIRECTORY_TYPE) {
                tvSize.setText("");
                if (position == 0) {// 上一级按钮
                    ivType.setImageResource(R.drawable.back_button);
                } else {
                    ivType.setImageResource(R.drawable.format_folder);
                }
            } else {
                tvSize.setText(item.size);
                ivType.setImageResource(R.drawable.format_unkown);
            }
            tvDate.setText(item.date);
            tvProp.setText(item.prop);
        }
        return view;
    }

}
