package com.frank.live.stream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import com.frank.live.camera2.Camera2Helper;
import com.frank.live.camera2.Camera2Listener;
import com.frank.live.listener.OnFrameDataCallback;
import com.frank.live.param.VideoParam;

/**
 * Pushing video stream: using Camera2
 * Created by frank on 2020/02/12.
 */
public class VideoStreamNew extends VideoStreamBase
        implements TextureView.SurfaceTextureListener, Camera2Listener {

    private static final String TAG = VideoStreamNew.class.getSimpleName();

    private boolean isLiving;
    private final Context mContext;
    private Camera2Helper camera2Helper;
    private final VideoParam mVideoParam;
    private final TextureView mTextureView;
    private final OnFrameDataCallback mCallback;

    public VideoStreamNew(OnFrameDataCallback callback,
                          View view,
                          VideoParam videoParam,
                          Context context) {
        this.mCallback = callback;
        // just support TextureView now
        this.mTextureView = (TextureView) view;
        this.mVideoParam = videoParam;
        this.mContext = context;
        mTextureView.setSurfaceTextureListener(this);
    }

    /**
     * start previewing
     */
    private void startPreview() {
        int rotateDegree = 0;
        if (mContext instanceof Activity) {
            rotateDegree = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        }
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
                .specificCameraId(Camera2Helper.CAMERA_ID_BACK)
                .context(mContext.getApplicationContext())
                .previewOn(mTextureView)
                .previewViewSize(new Point(mVideoParam.getWidth(), mVideoParam.getHeight()))
                .rotation(rotateDegree)
                .build();
        camera2Helper.start();
    }

    @Override
    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void switchCamera() {
        if (camera2Helper != null) {
            camera2Helper.switchCamera();
        }
    }

    @Override
    public void startLive() {
        isLiving = true;
    }

    @Override
    public void stopLive() {
        isLiving = false;
    }

    @Override
    public void release() {
        if (camera2Helper != null) {
            camera2Helper.stop();
            camera2Helper.release();
            camera2Helper = null;
        }
    }

    /**
     * stop previewing
     */
    private void stopPreview() {
        if (camera2Helper != null) {
            camera2Helper.stop();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable...");
        startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed...");
        stopPreview();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * Camera2 preview frame data
     *
     * @param y plane of y
     * @param u plane of u
     * @param v plane of v
     */
    @Override
    public void onPreviewFrame(byte[] y, byte[] u, byte[] v) {
        if (isLiving && mCallback != null) {
            mCallback.onVideoFrame(null, y, u, v);
        }
    }

    @Override
    public void onCameraOpened(Size previewSize, int displayOrientation) {
        Log.i(TAG, "onCameraOpened previewSize=" + previewSize.toString());
        if (mCallback != null && mVideoParam != null) {
            mCallback.onVideoCodecInfo(previewSize.getWidth(), previewSize.getHeight(),
                    mVideoParam.getFrameRate(), mVideoParam.getBitRate());
        }
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed");
    }

    @Override
    public void onCameraError(Exception e) {
        Log.e(TAG, "onCameraError=" + e.toString());
    }

}
