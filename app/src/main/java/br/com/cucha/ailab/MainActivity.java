package br.com.cucha.ailab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.json.JSONStringer;

import ai.api.AIListener;
import ai.api.GsonFactory;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

public class MainActivity extends AppCompatActivity implements AIListener, TextToSpeech.OnInitListener {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static String TAG = MainActivity.class.getName();
    private Gson mGson;
    private TextToSpeech mTts;
    private boolean mSpeech;
    private static String TOKEN = "seu token";
    private AIService mAiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGson = new Gson();
        mTts = new TextToSpeech(this, this);

        AIConfiguration config = new AIConfiguration(
                TOKEN,
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        mAiService = AIService.getService(this, config);

        findViewById(R.id.button_ai_main).setOnClickListener(this::onButtonAIClick);

        new AIHelper(this, mAiService, getLifecycle(), this);
    }

    public void onButtonAIClick(View view) {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(permission == PackageManager.PERMISSION_DENIED) {
            String[] permissions = new String[] { Manifest.permission.RECORD_AUDIO };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO);
        }

        mAiService.stopListening();
        mAiService.startListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResult(AIResponse result) {
        log(result);

        if(mSpeech) {
            String speech = result.getResult().getFulfillment().getSpeech();
            mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onError(AIError error) {
        log(error);
    }

    @Override
    public void onAudioLevel(float level) {
    }

    @Override
    public void onListeningStarted() {
    }

    @Override
    public void onListeningCanceled() {
    }

    @Override
    public void onListeningFinished() {
    }

    void log(Object object) {
        Log.d(TAG, mGson.toJson(object));
    }

    @Override
    public void onInit(int i) {
        mSpeech = i == TextToSpeech.SUCCESS;
    }

    @Override
    protected void onDestroy() {
        mTts.shutdown();
        super.onDestroy();
    }
}
