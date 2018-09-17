package raphaelwerner.ocr_avgeo;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText rg, nome, cpf, datanasc;
    private ImageView docPicture;
    ResultOCR resultOCR = new ResultOCR();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nome = findViewById(R.id.text_NOME);
        rg = findViewById(R.id.text_RG);
        cpf = findViewById(R.id.text_CPF);
        datanasc = findViewById(R.id.text_DN);
        docPicture = findViewById(R.id.imageDoc);


        try {
            Intent it = getIntent();
            String[] resultado = it.getStringArrayExtra("OCR");
            nome.setText(resultado[0]);
            rg.setText(resultado[1]);
            cpf.setText(resultado[2]);
            datanasc.setText(resultado[3]);
            Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/OCR/temp.jpg");
            docPicture.setImageBitmap(bitmap);
        }catch (Exception e){

        }





        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.button_camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InstaciarCamera();
            }
        });

        FloatingActionButton upload = (FloatingActionButton) findViewById(R.id.button_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
                inspect(data.getData());
        }
        else super.onActivityResult(requestCode, resultCode, data);

    }

    private void InstaciarCamera(){
        Intent camera = new Intent(this, CameraActivity.class);
        startActivity(camera);
    }

    private void inspect(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            bitmap = BitmapFactory.decodeStream(is, null, options);

            setTextOCR(resultOCR.inspectFromBitmap(bitmap,this,1), uri);

        } catch (FileNotFoundException e) {

        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private void setTextOCR(String[] ocr, Uri uri){
        nome.setText(ocr[0]);
        rg.setText(ocr[1]);
        cpf.setText(ocr[2]);
        datanasc.setText(ocr[3]);

        docPicture.setImageURI(uri);

    }
}
