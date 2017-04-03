package shetty.devesh.com.emotionapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
  // Flag to indicate the request of the next task to be performed
  private static final int REQUEST_TAKE_PHOTO = 0;
  private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;
  private static final String TAG = "MainActivity";
  private Context mContext = MainActivity.this;

  // The URI of photo taken with camera
  private Uri mUriPhotoTaken;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    if (getString(R.string.subscription_key).startsWith("Please")) {
      new AlertDialog.Builder(this)
        .setTitle(getString(R.string.add_subscription_key_tip_title))
        .setMessage(getString(R.string.add_subscription_key_tip))
        .setCancelable(false)
        .show();
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  public void activityRecognize(View v) {
    Intent intent = new Intent(this, RecognizeActivity.class);
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // Save the activity state when it's going to stop.
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable("ImageUri", mUriPhotoTaken);
  }

  // Recover the saved state when the activity is recreated.
  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
  }

  // Deal with the result of selection of the photos and faces.
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_TAKE_PHOTO:
      case REQUEST_SELECT_IMAGE_IN_ALBUM:
        if (resultCode == RESULT_OK) {
          Uri imageUri;
          if (data == null || data.getData() == null) {
            imageUri = mUriPhotoTaken;
          } else {
            imageUri = data.getData();
          }

          Intent intent = new Intent(mContext, MoodActivity.class);
          intent.putExtra(MoodActivity.PHOTO_URI, imageUri);
          startActivity(intent);

        }
        break;
      default:
        break;
    }
  }


  // When the camera icon is clicked.
  public void takePhoto(View view) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (intent.resolveActivity(getPackageManager()) != null) {
      // Save the photo taken to a temporary file.
      File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      try {
        File file = File.createTempFile("IMG_", ".jpg", storageDir);
        mUriPhotoTaken = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
    }
  }

  // When the gallery icon is clicked.
  public void selectImageInAlbum(View view) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM);
    }
  }
}
