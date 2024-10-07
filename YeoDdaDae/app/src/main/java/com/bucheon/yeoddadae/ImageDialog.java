package com.bucheon.yeoddadae;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

public class ImageDialog extends Dialog {
    Context context;
    String imageUrl;

    ImageView imageImgView;
    ImageButton imageBackBtn;

    public ImageDialog(@NonNull Context context, String imageUrl) {
        super(context);
        this.context = context;
        this.imageUrl = imageUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        int width = context.getResources().getDisplayMetrics().widthPixels;
        params.width = width;
        params.height = width;
        getWindow().setAttributes(params);

        imageImgView = findViewById(R.id.imageImgView);
        imageBackBtn = findViewById(R.id.imageBackBtn);

        Glide.with(context).load(imageUrl).into(imageImgView);

        imageBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
