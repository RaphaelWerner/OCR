package raphaelwerner.ocr_avgeo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private OnFocusListener onFocusListener;
    ResultOCR resultOCR = new ResultOCR();
    private boolean needToTakePic = false;
    Context context;

    private Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0) {
                mCamera.cancelAutoFocus();
            }
        }
    };

    // Constructor that obtains context and camera
    @SuppressWarnings("deprecation")
    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.onFocusListener = (OnFocusListener) context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            // left blank for now
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        this.mSurfaceHolder.removeCallback(this);
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
            e.printStackTrace();
        }
    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            mCamera.setParameters(param);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isNeedToTakePic()) {
            onFocusListener.onFocused();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int) (x - 100),
                    (int) (y - 100),
                    (int) (x + 100),
                    (int) (y + 100));


            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000);

            doTouchFocus(targetFocusRect);
            capture();

        }

        return false;
    }

    public void capture() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, mPicture);
                setNeedToTakePic(false);

            }
        }, 1500);
    }
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/OCR/temp.jpg");
                analyzePicture(bitmap);

                camera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private static File getOutputMediaFile() {

        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {

        } else {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "OCR");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File outputFile = new File(folder, "temp.jpg");
            return outputFile;
        }
        return null;
    }

    private void analyzePicture(Bitmap bitmap){
        int aux = 0;
        String[] a = resultOCR.inspectFromBitmap(bitmap, context, 1);
        for(int i = 0; i < a.length; i++){
            if(a[i] == "" || a[i] == null){
                aux++;
            }
        }
        if(aux < 3){
            Intent main = new Intent(context, MainActivity.class);
            main.putExtra("OCR", a);
            context.startActivity(main);
        }

    }


    public boolean isNeedToTakePic() {
        return needToTakePic;
    }

    public void setNeedToTakePic(boolean needToTakePic) {
        this.needToTakePic = needToTakePic;
    }



}

