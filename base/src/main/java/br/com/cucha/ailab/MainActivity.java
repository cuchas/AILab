package br.com.cucha.ailab;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<AIResponse> {

    ArrayList<String> mChatList = new ArrayList<>();
    private ChatAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mQuery;
    private AIService mAiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String TOKEN = "seu token";
        AIConfiguration config = new AIConfiguration(
                TOKEN,
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        mAiService = AIService.getService(this, config);

        final EditText edit = findViewById(R.id.edit_text_main);

        mAdapter = new ChatAdapter();

        mRecyclerView = findViewById(R.id.recycler_main);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);

        findViewById(R.id.button_send_main).setOnClickListener(v -> {

            mQuery = edit.getText().toString();

            edit.setText(null);

            mChatList.add(mQuery);

            getLoaderManager().restartLoader(0, null, this).forceLoad();

            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public Loader<AIResponse> onCreateLoader(int id, Bundle args) {
        return new AILoader(this, mAiService, mQuery);
    }

    @Override
    public void onLoadFinished(Loader<AIResponse> loader, AIResponse data) {

        if(data == null) return;

        mChatList.add(data.getResult().getFulfillment().getSpeech());

        mAdapter.notifyDataSetChanged();

        mRecyclerView.scrollToPosition(mChatList.size() -1);
    }

    @Override
    public void onLoaderReset(Loader<AIResponse> loader) {

    }

    private static class AILoader extends AsyncTaskLoader<AIResponse> {

        private final AIService mAiService;
        private final AIRequest mRequest;

        public AILoader(Context context, AIService aiService, String query) {
            super(context);
            mAiService = aiService;
            mRequest = new AIRequest(query);
        }

        @Override
        public AIResponse loadInBackground() {
            AIResponse response = null;
            try {
                response = mAiService.textRequest(mRequest);
            } catch (AIServiceException e) {
                e.printStackTrace();
            }

            return response;
        }
    }

    private static class ChatViewHolder extends RecyclerView.ViewHolder {

        private final TextView text;

        public ChatViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView;
        }

        public void bind(String s) {
            text.setText(s);
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

        @Override
        public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.view_text_main, null);

            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChatViewHolder holder, int position) {
            holder.bind(mChatList.get(position));
        }

        @Override
        public int getItemCount() {
            return mChatList.size();
        }
    }
}
