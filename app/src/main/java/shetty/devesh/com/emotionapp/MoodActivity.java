package shetty.devesh.com.emotionapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

import shetty.devesh.com.emotionapp.data.BeatBox;
import shetty.devesh.com.emotionapp.helper.ImageHelper;
import shetty.devesh.com.emotionapp.model.Song;
import shetty.devesh.com.emotionapp.util.StorageUtil;

public class MoodActivity extends AppCompatActivity {

  public static final String PHOTO_URI = "PHOTO_URI";

  public static final String Broadcast_PLAY_NEW_AUDIO = "shetty.devesh.com.emotionapp.PLAY_NEW_AUDIO";


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

  private RecyclerView mRecyclerViewSongs;

  private BeatBox mBeatBox;

  private  boolean isAutoPlayEnabled = true;

  private FloatingActionButton fab;

  private MediaPlayerService player;
  boolean serviceBound = false;

  private List<Song> mAudioList;


  //Binding this Client to the AudioPlayer Service
  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
      player = binder.getService();
      serviceBound = true;

      Toast.makeText(MoodActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceBound = false;
    }
  };

  private void playAudio(int audioIndex) {
    //Check is service is active
    if (!serviceBound) {
      //Store Serializable audioList to SharedPreferences
      StorageUtil storage = new StorageUtil(getApplicationContext());
      storage.storeAudio(mAudioList);
      storage.storeAudioIndex(audioIndex);

      Intent playerIntent = new Intent(this, MediaPlayerService.class);
      startService(playerIntent);
      bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    } else {
      //Store the new audioIndex to SharedPreferences
      StorageUtil storage = new StorageUtil(getApplicationContext());
      storage.storeAudioIndex(audioIndex);

      //Service is active
      //Send a broadcast to the service -> PLAY_NEW_AUDIO
      Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
      sendBroadcast(broadcastIntent);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putBoolean("ServiceState", serviceBound);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    serviceBound = savedInstanceState.getBoolean("ServiceState");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (serviceBound) {
      unbindService(serviceConnection);
      //service is active
      player.stopSelf();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mood);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mBeatBox = new BeatBox(this);

    mAudioList = mBeatBox.getSounds();

    fab = (FloatingActionButton) findViewById(R.id.fab_music_control);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(mRecyclerViewSongs.getChildCount() > 0){
          if(mBeatBox.isMediaPlaying()){
            fab.setImageResource(android.R.drawable.ic_media_play);
            mBeatBox.pause();
          }
          else
          {
            fab.setImageResource(android.R.drawable.ic_media_pause);
            mBeatBox.play();
          }
        }
      }
    });
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mProgressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);
    //    mProgressBar.setVisibility(View.VISIBLE);

    mRecyclerViewSongs = (RecyclerView) findViewById(R.id.rcv_songs);

    mRecyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerViewSongs.setAdapter(new SoundAdapter(mBeatBox.getSounds()));

    if (client == null) {
      client = new EmotionServiceRestClient(getString(R.string.subscription_key));
    }

    mTextViewResult = (TextView) findViewById(R.id.tv_mood_result);

    Intent intent = getIntent();

    mImageUri = intent.getParcelableExtra(PHOTO_URI);

    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
      mImageUri, getContentResolver());
    if (mBitmap != null) {
      // Show the image on screen.
      ImageView imageView = (ImageView) findViewById(R.id.iv_image);
      imageView.setImageBitmap(mBitmap);

      // Add detection log.
      Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
        + "x" + mBitmap.getHeight());

      doRecognize();
    }
  }
  public void doRecognize() {

    // Do emotion detection using auto-detected faces.
    try {
      new MoodActivity.doRequest(false).execute();
    } catch (Exception e) {
      mTextViewResult.append("Error encountered. Exception is: " + e.toString());
    }

    String faceSubscriptionKey = getString(R.string.faceSubscription_key);
    if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
      mTextViewResult.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
    } else {
      // Do emotion detection using face rectangles provided by Face API.
      try {
        new MoodActivity.doRequest(true).execute();
      } catch (Exception e) {
        mTextViewResult.append("Error encountered. Exception is: " + e.toString());
      }
    }
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
          ImageView imageView = (ImageView) findViewById(R.id.iv_image);
          imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
        }

      }
      //mProgressBar.setVisibility(View.GONE);
    }
  }

  private class SoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public Button mSoundButton;
    private Song mSong;

    public SoundHolder(View itemView) {
      super(itemView);
      mSoundButton = (Button) itemView.findViewById(R.id.list_item_sound_button);
      mSoundButton.setOnClickListener(this);
    }

    public void bindSound(Song sound) {
      mSong = sound;
      mSoundButton.setText(mSong.getName());
    }

    public void playSound(){
      mBeatBox.play(mSong);
    }

    @Override
    public void onClick(View view) {
      fab.setImageResource(android.R.drawable.ic_media_pause);
      //mBeatBox.play(mSong);
      playAudio(mSong.getIndex());
    }
  }

  private class SoundAdapter extends RecyclerView.Adapter<SoundHolder>{

    private List<Song> mSongs;
    public SoundAdapter(List<Song> sounds) {
      mSongs = sounds;
    }

    @Override
    public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = getLayoutInflater();
      View view = inflater.inflate(R.layout.list_item_sound, parent, false);
      return new SoundHolder(view);
    }
    @Override
    public void onBindViewHolder(SoundHolder holder, int position) {
      Song sound = mSongs.get(position);
      holder.bindSound(sound);
      if(position == 0 && isAutoPlayEnabled){
        isAutoPlayEnabled = false;
        fab.setImageResource(android.R.drawable.ic_media_pause);
        //holder.playSound();
        playAudio(position);
      }
    }

    @Override
    public int getItemCount() {
      return mSongs.size();
    }
  }

}
