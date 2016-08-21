package jp.techacademy.tomonari.watanabe.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private int page = 1;//ページ管理用

    TextView note;
    TextView pages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

            }
        } else {
            getContentsInfo();
        }

        //idを取得している
        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(this);

        Button show = (Button) findViewById(R.id.show);
        show.setOnClickListener(this);

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);

        note =(TextView) findViewById(R.id.show);

    }

    @Override
    public void onClick(View v) {
        Button next = (Button) findViewById(R.id.next);
        Button prev = (Button) findViewById(R.id.prev);

        if (v.getId() == R.id.prev){
                --page;
                getContentsInfo();

            } else if (v.getId() == R.id.next){
                ++page;
                getContentsInfo();

            } else if (v.getId() == R.id.show){

                if(mainTimer == null){
                    timerTask = new MyTimerTask();
                    mainTimer = new Timer(true);
                    note.setText("停止");
                    mainTimer.schedule(timerTask, 2000,2000);
                    next.setEnabled(false);
                    prev.setEnabled(false);

                }

                else if(mainTimer != null){
                    mainTimer.cancel();
                    mainTimer=null;
                    note.setText("再生");
                    next.setEnabled(true);
                    prev.setEnabled(true);

                    //一時停止
                }

            }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getContentsInfo();
                } else {
                    Log.d("test","許可されてない");
                }
                break;
            default:
                break;

        }
    }



    private void getContentsInfo() {
        //画像情報の取得

        TextView pages = (TextView) findViewById(R.id.pages);

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//外部ストレージの画像を全て指定、cursorクラスになる
                null,
                null,
                null,
                null
        );



        if (cursor.move(page)) {

            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);



        } else if (page <= 0 && cursor.moveToLast()){

            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);

            page = cursor.getCount();


        } else if (page > cursor.getCount() && cursor.moveToFirst()){

            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);

            page = 1;

        }

        pages.setText(String.valueOf(page) + "/" + String.valueOf(cursor.getCount()));
        Log.d("test",String.valueOf(page));


        cursor.close();

    }

    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            //handlerで別thredで処理する
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ++page;
                    getContentsInfo();
                }
            });

        }


    }

    MyTimerTask timerTask = null;
    Timer mainTimer = null;
    Handler mHandler = new Handler();      //UI Threadへのpost用ハンドラ


}
