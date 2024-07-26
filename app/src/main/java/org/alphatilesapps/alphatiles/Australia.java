package org.alphatilesapps.alphatiles;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
public class Australia extends GameActivity {

    int boardWidth = 7;
    int boardHeight = 7;
    TextView[][] buttons = new TextView[boardWidth][boardHeight];
    int[] GAME_BUTTONS = new int[boardWidth * boardHeight];



    HexagonalArray<TextView> hex = new HexagonalArray<>(buttons);

    LinkedList<TextView> wordBeingBuilt = new LinkedList<>();

    String[] tileStrings;
    String allCharacters = "";

    int bwSquareDim;
    int bwSquareMargin;

    private int prettyColor(int color) {
        Random rnd = new Random();
        switch (color) {
            case Color.GREEN:
                return Color.argb(255, 0, 204, 0);
                //return Color.argb(255, rnd.nextInt(32), rnd.nextInt(128 - 16) + 64 + 16, rnd.nextInt(64));
            default:
                return 0;
        }
    }

    HashMap<Integer, BWSquareInfo> idToSquinfo = new HashMap<>();
    private class BWSquareInfo {
        public int[] pos;
        public int[] originalScreenPos;
        boolean didSwap;
        TextView swappedWith;
        public int lowX, highX, lowY, highY;
    }

    class CorrectWordSet {
        HashSet<String> correctWords;

        public CorrectWordSet() {
            correctWords = new HashSet<>();

            for (Start.Word word : Start.wordList) {
                String wordToAdd = wordInLOPWithStandardizedSequenceOfCharacters(word);
                correctWords.add(wordToAdd);
                allCharacters += wordToAdd;
            }
        }

        void add(String word) {
            correctWords.add(word);
        }

        boolean contains(String word) {
            return correctWords.contains(word);
        }
    }

    CorrectWordSet correctWordSet = new CorrectWordSet();

    class BWSquareChain {
        LinkedList<TextView> textViewLinkedList = new LinkedList<>();

        String word = "";

        boolean contains(TextView v) {
            for (TextView w : textViewLinkedList) {
                if (w.getId() == v.getId())
                    return true;
            }
            return false;
        }

        boolean isAddable(TextView v) {
            if (textViewLinkedList.isEmpty())
                return true;
            if (contains(v))
                return false;
            TextView endOfWord = textViewLinkedList.getLast();
            int[] pos = idToSquinfo.get(endOfWord.getId()).pos;
            for (TextView w : hex.getAdjacents(pos[0], pos[1])) {
                if (w.getId() == v.getId())
                    return true;
            }

            return false;
        }

        void add(TextView v) {
            if (isAddable(v)) {
                textViewLinkedList.add(v);
                v.setBackgroundColor(Color.RED);
                word += v.getText();
            }
        }

        void clear() {
            while (!textViewLinkedList.isEmpty())
                textViewLinkedList.remove().setBackgroundColor(prettyColor(Color.GREEN));
            word = "";
        }

        String word () {
            return word;
        }

        boolean isWord() {
            return correctWordSet.contains(word());
        }
    }

    BWSquareChain chain = new BWSquareChain();

    class SubmitAndPreview {

        void onClick() {
            if (chain.isWord()) {
                System.out.println("That's a word!");
            } else {
                System.out.println("Not a word. Please just do better.");
            }
        }

        void update() {
            TextView wordView = findViewById(R.id.wordPreview);
            wordView.setText(chain.word());

            if (chain.isWord()) {
                wordView.setBackgroundColor(Color.GREEN);
            } else {
                wordView.setBackgroundColor(Color.LTGRAY);
            }

            if (chain.word().isEmpty()) {
                wordView.setVisibility(View.INVISIBLE);
            } else {
                wordView.setVisibility(View.VISIBLE);
            }
        }
    }

    SubmitAndPreview submitAndPreview = new SubmitAndPreview();

    class ReplacementPopup {

        private boolean popupup = false;

        private TextView square;

        public ReplacementPopup() {

        }

        void onReplacementClick(TextView v) {
            square.setText(v.getText());
            closePopup();
        }

        void openPopup(TextView v) {
            // store the view so that when a replacement is selected, the view can be changed
            square = v;

            square.animate().z(30);

            int[] pos = idToSquinfo.get(square.getId()).pos;
            for (int i = 0; i < hex.getWidth(); i++) {
                for (int j = 0; j < hex.getHeight(); j++) {
                    if (i == pos[0] && j == pos[1])
                        continue;
                    hex.get(i, j).animate().alpha(0.5F);
                }
            }

            LinearLayout popupLayout = findViewById(R.id.replacementPopup);
            popupLayout.setGravity(Gravity.CENTER);
            popupLayout.removeAllViews();
            popupLayout.setVisibility(View.VISIBLE);
            Start.Tile squareContentsTile = Start.tileHashMap.find((String) v.getText());
            ArrayList<String> distractors = squareContentsTile.distractors;
            for (String distractor : distractors) {
                TextView option = new TextView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        bwSquareDim + bwSquareMargin,
                        bwSquareDim + bwSquareMargin,
                        1
                );
                layoutParams.setMargins(5,5,5,5);
                option.setLayoutParams(layoutParams);

                option.setText(distractor);
                option.setGravity(Gravity.CENTER);
                option.setZ(30);
                option.setBackgroundColor(Color.argb(255, 0, 204, 0));

                option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onReplacementClick((TextView) v);
                    }
                });
                popupLayout.addView(option);
                popupup = true;
            }
        }

        void closePopup() {
            LinearLayout popupLayout = findViewById(R.id.replacementPopup);
            popupLayout.removeAllViews();
            popupLayout.setVisibility(View.GONE);

            if (square != null) {
                square.animate().z(0);
                int[] pos = idToSquinfo.get(square.getId()).pos;
                for (int i = 0; i < hex.getWidth(); i++) {
                    for (int j = 0; j < hex.getHeight(); j++) {
                        if (i == pos[0] && j == pos[1])
                            continue;
                        hex.get(i, j).animate().alpha(1.0F);
                    }
                }
            }

            popupup = false;
        }

        boolean isPopupup() { return popupup; }
    }

    ReplacementPopup poppy = new ReplacementPopup();

    /**
     * if square being dragged is {-1, -1}, then no square is being dragged
     * if it is not {-1, -1}, then square dragging is locked until the current
     *  square being dragged is done being dragged
     */
    int[] squareBeingDragged = new int[] {-1, -1};

    protected int[] getGameButtons() { return GAME_BUTTONS; }

    protected  int[] getWordImages() { return null; }

    protected int getAudioInstructionsResID() { return 0; }

    protected void centerGamesHomeImage() {}

    private void swap(int[] arr1, int[] arr2) {
        assert arr1.length == arr2.length;
        for (int i = 0; i < arr1.length; i++) {
            int temp = arr1[i];
            arr1[i] = arr2[i];
            arr2[i] = temp;
        }
    }

    int parity(int num) { return (num < 0) ? -1 : 1; }

    private View.OnTouchListener bwSquareOnClick = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            // get the square being dragged
            BWSquareInfo squinfo = idToSquinfo.get(v.getId());

            // get all squares swappable with the dragged square
            LinkedList<TextView> swappableAdjacents = hex.getSwappableAdjacents(squinfo.pos[0], squinfo.pos[1]);

            int[] offset = new int[2];
            findViewById(R.id.rl).getLocationOnScreen(offset);
            int vX = (int)v.getX() + (bwSquareDim / 2);
            int vY = (int)v.getY() + (bwSquareDim / 2);
            int touchX = (int)e.getRawX() - offset[0];
            int touchY = (int)e.getRawY() - offset[1];

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    System.out.println("Action Down");
                    squinfo.didSwap = false;
                    poppy.closePopup();
                    break;
                case MotionEvent.ACTION_MOVE:
                    System.out.println("Action Move");

                    if (touchY > v.getY() && touchX > v.getX() && touchY < v.getY() + bwSquareDim - bwSquareMargin && touchX < v.getX() + bwSquareDim - bwSquareMargin) {
                        break;
                    } else {
                        System.out.println("Action Outside");
                    }

                    for (TextView adj : swappableAdjacents) {
                        int adjMidX = (int) adj.getX() + (bwSquareDim / 2);
                        int adjMidY = (int) adj.getY() + (bwSquareDim / 2);

                        boolean doSwapX = parity(adjMidX - vX) == parity(touchX - vX);
                        boolean doSwapY = parity(adjMidY - vY) == parity(touchY - vY);

                        if (doSwapX && doSwapY && !squinfo.didSwap) {
                            System.out.println("Swap");
                            squinfo.didSwap = true;
                            BWSquareInfo swapSquinfo = idToSquinfo.get(adj.getId());
                            hex.swap(squinfo.pos[0], squinfo.pos[1], swapSquinfo.pos[0], swapSquinfo.pos[1]);
                            swap(squinfo.pos, swapSquinfo.pos);
                            swap(squinfo.originalScreenPos, swapSquinfo.originalScreenPos);

                            adj.animate()
                                    .x(swapSquinfo.originalScreenPos[0])
                                    .y(swapSquinfo.originalScreenPos[1])
                                    .setDuration(125);
                            v.animate()
                                    .x(squinfo.originalScreenPos[0])
                                    .y(squinfo.originalScreenPos[1])
                                    .setDuration(125);

                            chain.clear();

                            return false;
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    System.out.println("Action Up");

                    boolean withinX = touchX > v.getX() && touchX < v.getX() + bwSquareDim - bwSquareMargin;
                    boolean withinY = touchY > v.getY() && touchY < v.getY() + bwSquareDim - bwSquareMargin;
                    if (!squinfo.didSwap && !poppy.isPopupup() && withinX && withinY) {
                        chain.add((TextView) v);
                    }

                    System.out.println("Chain: " + chain.word());
                    submitAndPreview.update();

                    squinfo.didSwap = false;

                    break;
            }
            return false;
        }

    };
    private View.OnLongClickListener bwSquareOnLongClick = new View.OnLongClickListener() {
      public boolean onLongClick(View v) {
          System.out.println("Long Click Detected!");

          poppy.openPopup((TextView) v);
          return false;
      }
    };

    /**
     * Call this each time you want to fill a board square with a letter
     * @return The next letter to fill the board with
     */
    String fillBoardSquare() {
        return Start.getRandomTileByFrequency.get().text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int tileStringsFillIndex = 0;
        tileStrings = new String[Start.tileList.size()];
        for (Start.Tile tile : Start.tileList) {
                tileStrings[tileStringsFillIndex++] = tile.text;
        }

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.australia);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl);
        rl.measure(0, 0);
        int screenWidth = rl.getMeasuredWidth();
        bwSquareDim = screenWidth / boardWidth;
        bwSquareMargin = bwSquareDim / 10;
        screenWidth += bwSquareMargin;
        bwSquareDim = screenWidth / boardWidth;
        bwSquareMargin = bwSquareDim / 10;

        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                int xPos = (i * screenWidth) / boardWidth;
                int yPos = (j * screenWidth) / boardHeight;
                buttons[i][j] = new TextView(this);
                buttons[i][j].setText(fillBoardSquare());

                int color = prettyColor(Color.GREEN);
                buttons[i][j].setBackgroundColor(color);

                buttons[i][j].setGravity(Gravity.CENTER);
                buttons[i][j].setOnTouchListener(bwSquareOnClick);
                buttons[i][j].setOnLongClickListener(bwSquareOnLongClick);

                buttons[i][j].setId(View.generateViewId());

                // make that when we touch a bw square we can find its place in the array
                BWSquareInfo squinfo = new BWSquareInfo();
                squinfo.pos = new int[] {i, j};

                idToSquinfo.put(buttons[i][j].getId(), squinfo);

                RelativeLayout.LayoutParams params;
                params = new RelativeLayout.LayoutParams(bwSquareDim - bwSquareMargin, bwSquareDim - bwSquareMargin);
                params.leftMargin = xPos;
                params.topMargin = yPos + (hex.isUp(i) ? 0 : bwSquareDim / 2 );
                squinfo.originalScreenPos = new int[] {params.leftMargin, params.topMargin};
                rl.addView(buttons[i][j], params);
            }
        }

        // initialize GAME_BUTTONS array
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                GAME_BUTTONS[i*boardWidth + j] = buttons[i][j].getId();
            }
        }

        submitAndPreview.update();
        TextView wordPreview = findViewById(R.id.wordPreview);
        wordPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAndPreview.onClick();
            }
        });

        poppy.closePopup();

    }


}
