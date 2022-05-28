package com.github.flowersbloom.widget;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.github.flowersbloom.H264Encoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;
    private Camera.Size cameraSize;
    private H264Encoder h264Encoder;
    private CompletableFuture<Integer> initCameraFuture;

    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        initCameraFuture = new CompletableFuture<>();
    }

    public void startCapture(Activity activity, InetSocketAddress dstAddress) {
        new Thread(() -> {
            try {
                initCameraFuture.get(10, TimeUnit.SECONDS);
            }catch (Exception e) {
                log.error("startCapture e:{}", e.getMessage());
            }
            log.info("init camera success");
            activity.runOnUiThread(() -> {
                h264Encoder = new H264Encoder(cameraSize.width, cameraSize.height, dstAddress);
                h264Encoder.startLive();
            });
        }).start();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        //视频数据流通过编码器编码后再进行网络传输
        if (h264Encoder != null) {
            h264Encoder.encodeFrame(bytes);
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
            initCameraFuture.complete(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
