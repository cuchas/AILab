package br.com.cucha.ailab;

import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity {

    ArrayList<String> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String TOKEN = "seu token";
        AIConfiguration config = new AIConfiguration(
                TOKEN,
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        final AIService aiService = AIService.getService(this, config);

        final EditText edit = findViewById(R.id.edit_text_main);

        final ChatAdapter adapter = new ChatAdapter();

        final RecyclerView recyclerView = findViewById(R.id.recycler_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.button_send_main).setOnClickListener(v -> {

            final String query = edit.getText().toString();

            edit.setText(null);

            chatList.add(query);

            adapter.notifyDataSetChanged();

            AIRequest req = new AIRequest(query);

            new AsyncTask<AIRequest, Void, AIResponse>() {
                @Override
                protected AIResponse doInBackground(AIRequest... aiRequests) {
                    AIResponse response = null;
                    try {
                        response = aiService.textRequest(req);
                    } catch (AIServiceException e) {
                        e.printStackTrace();
                    }

                    return response;
                }

                @Override
                protected void onPostExecute(AIResponse aiResponse) {
                    super.onPostExecute(aiResponse);

                    chatList.add(aiResponse.getResult().getFulfillment().getSpeech());

                    adapter.notifyDataSetChanged();

                    recyclerView.scrollToPosition(chatList.size() -1);
                }
            }.execute(req);

            edit.setText(null);
        });
    }

    private class ChatViewHolder extends RecyclerView.ViewHolder {

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

            ChatViewHolder viewHolder = new ChatViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ChatViewHolder holder, int position) {
            holder.bind(chatList.get(position));
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }
    }
}
