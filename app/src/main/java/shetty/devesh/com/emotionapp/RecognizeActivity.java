package shetty.devesh.com.emotionapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import shetty.devesh.com.emotionapp.helper.ImageHelper;
import shetty.devesh.com.emotionapp.helper.SelectImageActivity;

public class RecognizeActivity extends AppCompatActivity {

  public static final String PHOTO_URI = "PHOTO_URI";

  // Flag to indicate which task is to be performed.
  private static final int REQUEST_SELECT_IMAGE = 0;


  // The URI of the image selected to detect.
  private Uri mImageUri;

  // The image selected to detect.
  private Bitmap mBitmap;

  // The edit to show status and result.
  private TextView mTextViewResult;

  private EmotionServiceClient client;

  private ProgressBar mProgressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognize);

    mProgressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);
    mProgressBar.setVisibility(View.VISIBLE);

    if (client == null) {
      client = new EmotionServiceRestClient(getString(R.string.subscription_key));
    }

    mTextViewResult = (TextView) findViewById(R.id.tv_result);

    Intent intent = getIntent();

    mImageUri = intent.getParcelableExtra(PHOTO_URI);

    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
      mImageUri, getContentResolver());
    if (mBitmap != null) {
      // Show the image on screen.
      ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
      imageView.setImageBitmap(mBitmap);

      // Add detection log.
      Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
        + "x" + mBitmap.getHeight());

      doRecognize();
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_recognize, menu);
    return true;
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

  public void doRecognize() {

    // Do emotion detection using auto-detected faces.
    try {
      new doRequest(false).execute();
    } catch (Exception e) {
      mTextViewResult.append("Error encountered. Exception is: " + e.toString());
    }

    String faceSubscriptionKey = getString(R.string.faceSubscription_key);
    if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
      mTextViewResult.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
    } else {
      // Do emotion detection using face rectangles provided by Face API.
      try {
        new doRequest(true).execute();
      } catch (Exception e) {
        mTextViewResult.append("Error encountered. Exception is: " + e.toString());
      }
    }
  }

  // Called when the "Select Image" button is clicked.
  public void selectImage(View view) {
    mTextViewResult.setText("");

    Intent intent;
    intent = new Intent(RecognizeActivity.this, SelectImageActivity.class);
    startActivityForResult(intent, REQUEST_SELECT_IMAGE);
  }


  private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
    Log.d("emotion", "Start emotion detection with auto-face detection");

    Gson gson = new Gson();

    // Put the image into an input stream for detection.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

    long startTime = System.currentTimeMillis();

    List<RecognizeResult> result = null;
    //
    // Detect emotion by auto-detecting faces in the image.
    //
    result = this.client.recognizeImage(inputStream);

    String json = gson.toJson(result);
    Log.d("result", json);

    Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));

    return result;
  }

  private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
    Log.d("emotion", "Do emotion detection with known face rectangles");
    Gson gson = new Gson();

    // Put the image into an input stream for detection.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

    long timeMark = System.currentTimeMillis();
    Log.d("emotion", "Start face detection using Face API");
    FaceRectangle[] faceRectangles = null;
    String faceSubscriptionKey = getString(R.string.faceSubscription_key);
    FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
    Face faces[] = faceClient.detect(inputStream, false, false, null);
    Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

    if (faces != null) {
      faceRectangles = new FaceRectangle[faces.length];

      for (int i = 0; i < faceRectangles.length; i++) {
        // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
        com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
        faceRectangles[i] = new FaceRectangle(rect.left, rect.top, rect.width, rect.height);
      }
    }

    List<RecognizeResult> result = null;
    if (faceRectangles != null) {
      inputStream.reset();

      timeMark = System.currentTimeMillis();
      Log.d("emotion", "Start emotion detection using Emotion API");

      result = this.client.recognizeImage(inputStream, faceRectangles);

      String json = gson.toJson(result);
      Log.d("result", json);

      Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
    }
    return result;
  }

  private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
    // Store error message
    private Exception e = null;
    private boolean useFaceRectangles = false;

    public doRequest(boolean useFaceRectangles) {
      this.useFaceRectangles = useFaceRectangles;
    }

    @Override
    protected List<RecognizeResult> doInBackground(String... args) {
      if (this.useFaceRectangles == false) {
        try {
          return processWithAutoFaceDetection();
        } catch (Exception e) {
          this.e = e;    // Store error
        }
      } else {
        try {
          return processWithFaceRectangles();
        } catch (Exception e) {
          this.e = e;    // Store error
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(List<RecognizeResult> result) {
      super.onPostExecute(result);
      // Display based on error existence

      if (this.useFaceRectangles == false) {
        //mTextViewResult.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
      } else {
        // mTextViewResult.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
      }
      if (e != null) {
        mTextViewResult.setText("Error: " + e.getMessage());
        this.e = null;
      } else {
        if (result.size() == 0) {
          mTextViewResult.append("No emotion detected :(");
        } else {
          Integer count = 0;
          // Covert bitmap to a mutable bitmap by copying it
          Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
          Canvas faceCanvas = new Canvas(bitmapCopy);
          faceCanvas.drawBitmap(mBitmap, 0, 0, null);
          Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
          paint.setStyle(Paint.Style.STROKE);
          paint.setStrokeWidth(5);
          paint.setColor(Color.RED);

          for (RecognizeResult r : result) {

            String text = "Angry";
            double max = r.scores.anger;

            if (r.scores.contempt > max) {
              text = "Contempt";
              max = r.scores.contempt;
            }

            if (r.scores.disgust > max) {
              text = "Disgust";
              max = r.scores.disgust;
            }

            if (r.scores.fear > max) {
              text = "Fear";
              max = r.scores.fear;
            }

            if (r.scores.happiness > max) {
              text = "Happiness";
              max = r.scores.happiness;
            }

            if (r.scores.neutral > max) {
              text = "Neutral";
              max = r.scores.neutral;
            }

            if (r.scores.sadness > max) {
              text = "Sadness";
              max = r.scores.sadness;

            }

            if (r.scores.surprise > max) {
              text = "Surprise";
              max = r.scores.surprise;

            }

            mTextViewResult.setText(text);

            faceCanvas.drawRect(r.faceRectangle.left,
              r.faceRectangle.top,
              r.faceRectangle.left + r.faceRectangle.width,
              r.faceRectangle.top + r.faceRectangle.height,
              paint);
            count++;
          }
          ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
          imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
        }

      }
      mProgressBar.setVisibility(View.GONE);
    }
  }
}
