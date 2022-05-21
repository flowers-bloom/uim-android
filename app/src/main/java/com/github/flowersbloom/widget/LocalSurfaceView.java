package com.github.flowersbloom.widget;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.github.flowersbloom.H265Encoder;

import java.io.IOException;

public class LocalSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;
    private Camera.Size cameraSize;
    private H265Encoder h265Encoder;

    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void startCapture() {
        h265Encoder = new H265Encoder(cameraSize.width, cameraSize.height);
        h265Encoder.start();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        //视频数据流通过编码器编码后再进行网络传输
        if (h265Encoder != null) {
            //h265Encoder.encodeFrame(bytes);
        }
        this.camera.addCallbackBuffer(bytes);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    byte[] buffer;

    private void startPreview() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        cameraSize = camera.getParameters().getPreviewSize();
        try {
            camera.setPreviewDisplay(getHolder());
            camera.setDisplayOrientation(90);
            buffer = new byte[cameraSize.width * cameraSize.height * 3 / 2];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
