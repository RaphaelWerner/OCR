package raphaelwerner.ocr_avgeo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity implements OnFocusListener{

    private Camera mCamera;
    private ShowCamera mCameraPreview;
    private int currentCameraId;
    ResultOCR resultOCR = new ResultOCR();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        if (getIntent().hasExtra("camera_id")) {
            currentCameraId = getIntent().getIntExtra("camera_id", Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        mCamera = getCameraInstance(currentCameraId);
        mCameraPreview = new ShowCamera(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
    }


    private Camera getCameraInstance(int currentCameraId) {
        Camera camera = null;
        try {
            camera = Camera.open(currentCameraId);
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
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

    @Override
    public void onFocused() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, mPicture);
                mCameraPreview.setNeedToTakePic(false);
                //captureButton.setEnabled(true);
                Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/OCR/temp.jpg");

            }
        }, 1500);
    }


}
