package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dictionary extends AppCompatActivity {

    private ListView wordListView;
    private List<Start.Word> dictionaryWords;
    private int currentPage = 0;
    private static final int WORDS_PER_PAGE = 10;
    protected boolean mediaPlayerIsPlaying = false;
    protected Handler handler = new Handler();
    protected Start.Word refWord;

    protected void setAllGameButtonsUnclickable() {
        // Implementation from GameActivity
    }

    protected void setOptionsRowUnclickable() {
        // Implementation from GameActivity
    }

    protected void playActiveWordClip(boolean playFromFinalSound) {
        // Implementation from GameActivity
    }

    protected void setAllGameButtonsClickable() {
        // Implementation from GameActivity
    }

    protected void setOptionsRowClickable() {
        // Implementation from GameActivity
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        wordListView = findViewById(R.id.wordListView);
        dictionaryWords = new ArrayList<>(wordList);
        Collections.sort(dictionaryWords, (w1, w2) -> w1.wordInLWC.compareTo(w2.wordInLWC));

        updateWordList();
    }

    private void updateWordList() {
        int startIndex = currentPage * WORDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORDS_PER_PAGE, dictionaryWords.size());
        List<Start.Word> pageWords = dictionaryWords.subList(startIndex, endIndex);

        WordAdapter adapter = new WordAdapter(this, pageWords);
        wordListView.setAdapter(adapter);
    }

    private class WordAdapter extends ArrayAdapter<Start.Word> {
        private Context context;
        private List<Start.Word> words;

        public WordAdapter(Context context, List<Start.Word> words) {
            super(context, R.layout.word_item, words);
            this.context = context;
            this.words = words;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.word_item, parent, false);
            }

            ImageView wordImage = convertView.findViewById(R.id.wordImage);
            TextView wordText = convertView.findViewById(R.id.wordText);
            ImageButton audioButton = convertView.findViewById(R.id.audioButton);

            final Start.Word word = words.get(position);
            wordText.setText(word.wordInLWC);

            // Set image resource
            int resID = getResources().getIdentifier(word.wordInLWC + "2", "drawable", getPackageName());
            wordImage.setImageResource(resID);

            audioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(word);
                }
            });

            return convertView;
        }

        private void playAudio(Start.Word word) {
            if (mediaPlayerIsPlaying) {
                return;
            }

            mediaPlayerIsPlaying = true;
            setAllGameButtonsUnclickable();
            setOptionsRowUnclickable();

            refWord = word;
            playActiveWordClip(false);

            // Set a timer to re-enable buttons after audio finishes
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mediaPlayerIsPlaying = false;
                    setAllGameButtonsClickable();
                    setOptionsRowClickable();
                }
            }, 2000); // Adjust this delay as needed
        }
    }
}