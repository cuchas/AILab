package br.com.cucha.ailab;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import ai.api.AIListener;
import ai.api.android.AIService;

/**
 * Created by eduardo on 10/11/17.
 */

public class AIHelper implements LifecycleObserver {

    private final Context mContext;
    private final AIService mAiService;
    private final Lifecycle mLifeCycle;


    public AIHelper(Context context,
                    AIService aiService,
                    Lifecycle lifecycle,
                    AIListener listener) {

        mContext = context;
        mAiService = aiService;
        mAiService.setListener(listener);
        mLifeCycle = lifecycle;
        mLifeCycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void start() {

        int permission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);

        if(permission == PackageManager.PERMISSION_DENIED)
            return;

        mAiService.startListening();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void stop() {
        mAiService.stopListening();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroy() {
        mLifeCycle.removeObserver(this);
    }
}
