package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.tileList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Australia (Bookworm) Game
 *
 * How to play:
 *
 * The player is presented with a hexagonal grid of squares. There is a letter in each square. The
 * player taps the squares to build a chain of squares. If the letters in the chain of squares forms
 * a word, the user can submit that word with the green check-mark. The squares in the submitted
 * word will then turn yellow. Once the entire board is yellow, the player wins the round. The
 * player's score is determined by the number of unique words they build.
 *
 * The player has two ways of changing the board. They can swipe the squares to move them around
 * diagonally. If they press and hold a square, a small pop-up menu containing a list of letters
 * that are similar to the letter found inside the pressed square. They player can select any of
 * these letters to replace the letter in the square.
 */
public class Australia extends GameActivity {

    /**
     * If FAST_TESTING is set to true, the entire board will fill up with the word
     * agu', allowing round completion to be tested more quickly
     */
    private boolean FAST_TESTING = false;

    /**
     * The width, in number of squares, of the game board. (Making this number small can be useful
     * for quick testing)
     */
    private int boardWidth = 7;

    /**
     * The height, in number of squares, of the game board. (Making this number small can be useful
     * for quick testing)
     */
    private int boardHeight = 7;
    private TextView[][] buttons = new TextView[boardWidth][boardHeight];
    private int[] GAME_BUTTONS;
    private ArrayList<Integer> gameButtons = new ArrayList<>();

    /**
     * This hexagonal array is used to keep track of which squares on the screen are
     * adjacent to one another
     */
    private HexagonalArray<TextView> board = new HexagonalArray<>(buttons);

    /**
     * The width (in pixels) of each square is bwSquareDim - bwSquareMargin
     */
    private int bwSquareDim;
    private int bwSquareMargin;
    private Random randy = new Random();

    /**
     * This hashmap is used to store information about each square on the screen. Squinfo is a
     * shortening of "Square Info". This game often requires retrieving information about the state
     * of a square based on the id of TextView of the square.
     */
    private HashMap<Integer, BWSquareInfo> idToSquinfo = new HashMap<>();

    /**
     * The Bookworm Square Info class is used to keep track of information about every square on
     * the board.
     */
    private class BWSquareInfo {
        public int[] pos;
        public int[] originalScreenPos;
        boolean didSwap;
        public int backgroundColor;
        public int textColor = Color.WHITE;
        public int z = 0;
        public boolean squareHasBeenUsed = false;
    }

    /**
     * This class handles all of the ways a square can change its appearance
     */
    private class Visual {
        public static final int SELECT = 0;
        public static final int DESELECT = 1;
        public static final int FOCUS = 2;
        public static final int UNFOCUS = 3;
        public static final int SET_USED = 4;
        public static final int RESET = 5;

        public void action(TextView v, int action) {
            if (v == null)
                return;
            BWSquareInfo squinfo = idToSquinfo.get(v.getId());
            switch (action) {
                case SELECT:
                    v.setBackgroundColor(Color.WHITE);
                    v.setTextColor(Color.BLACK);
                    v.setZ(30);
                    //v.setBackgroundColor(Color.YELLOW);
                    //v.setTextColor(Color.BLACK);
                    break;
                case DESELECT:
                    v.setBackgroundColor(squinfo.backgroundColor);
                    v.setTextColor(squinfo.textColor);
                    v.setZ(squinfo.z);
                    break;
                case FOCUS: {
                    v.animate().z(30);
                    int[] pos = squinfo.pos;
                    for (int i = 0; i < board.getWidth(); i++) {
                        for (int j = 0; j < board.getHeight(); j++) {
                            if (i == pos[0] && j == pos[1])
                                continue;
                            board.get(i, j).animate().alpha(0.5F);
                        }
                    }
                    break;
                }
                case UNFOCUS: {
                    v.animate().z(squinfo.z);
                    int[] pos = squinfo.pos;
                    for (int i = 0; i < board.getWidth(); i++) {
                        for (int j = 0; j < board.getHeight(); j++) {
                            if (i == pos[0] && j == pos[1])
                                continue;
                            board.get(i, j).animate().alpha(1.0F);
                        }
                    }
                    break;
                }
                case SET_USED:
                    squinfo.squareHasBeenUsed = true;
                    //squinfo.backgroundColor = Color.WHITE;
                    squinfo.backgroundColor = Color.YELLOW;
                    squinfo.textColor = Color.BLACK;
                    //squinfo.z = 30;
                    v.setBackgroundColor(squinfo.backgroundColor);
                    v.setTextColor(squinfo.textColor);
                    v.setZ(squinfo.z);
                    break;
                case RESET:
                    squinfo.squareHasBeenUsed = false;
                    squinfo.backgroundColor = Color.parseColor(colorList.get(randy.nextInt(4)));
                    squinfo.textColor = Color.WHITE;
                    squinfo.z = 0;
                    v.setBackgroundColor(squinfo.backgroundColor);
                    v.setTextColor(squinfo.textColor);
                    v.setZ(squinfo.z);
                    break;
                default:
                    System.out.println("Not a thing, nerd.");
            }
        }
    }

    private Visual visual = new Visual();

    /**
     * This class is used to fill each square with text. Until the board is completely filled,
     * it will repeatedly choose a random word and scatter the tiles of that word across the board
     */
    private class BoardFiller {
        int neededTiles = boardWidth * boardHeight;
        ArrayList<Start.Tile> tilePool = new ArrayList<>();
        ArrayList<TextView> squares = new ArrayList<>();
        public void fillBoard() {
            // first clear the tile pool
            if (!tilePool.isEmpty()) {
                tilePool.clear();
                squares.clear();
            }

            // then fill the tile pool with at least as many tiles as needed
            while (tilePool.size() < neededTiles) {
                Start.Word word;
                if (FAST_TESTING) {
                    word = Start.wordList.get(2);
                } else {
                    word = Start.wordList.get(randy.nextInt(Start.wordList.size()));
                }
                tilePool.addAll(tileList.parseWordIntoTiles(word.wordInLOP, word));
            }

            // randomize the order of the squares
            for (int i = 0; i < boardWidth; i++)
                for (int j = 0; j < boardHeight; j++)
                    squares.add(buttons[i][j]);
            Collections.shuffle(squares);

            // make absolutely certain that there are more tiles in the tile pool than
            // there are squares on the board.
            assert squares.size() <= tilePool.size();

            // fill the board
            Iterator<Start.Tile> tileIterator = tilePool.iterator();
            Iterator<TextView> squareIterator = squares.iterator();
            while (squareIterator.hasNext()) {
                TextView square = squareIterator.next();
                square.setText(tileIterator.next().text);
                visual.action(square, Visual.RESET);
            }

        }
    }

    private BoardFiller boardFiller = new BoardFiller();

    /**
     * checks whether or not all of the squares on the board have been used to form a word
     */
    private boolean allSquaresUsed() {
        for (BWSquareInfo squinfo : idToSquinfo.values())
            if (!squinfo.squareHasBeenUsed)
                return false;
        return true;
    }

    /**
     * The CorrectWordSet is used to check whether or not a given string is a valid word
     */
    private class CorrectWordSet {
        HashSet<String> correctWords;

        public CorrectWordSet() {
            correctWords = new HashSet<>();

            for (Start.Word word : Start.wordList) {
                String wordToAdd = wordInLOPWithStandardizedSequenceOfCharacters(word);
                correctWords.add(wordToAdd);
            }
        }

        boolean contains(String word) {
            return correctWords.contains(word);
        }
    }

    private CorrectWordSet correctWordSet = new CorrectWordSet();

    /**
     * The WordsFoundSet keeps a record of every word that the player has found so far. The user
     * scores points for every unique word they find.
     */
    private class WordsFoundSet {
        HashSet<String> wordsFound = new HashSet<>();
        public WordsFoundSet() {}

        void add(String word) {
            wordsFound.add(word);
        }

        void clear() {
            wordsFound.clear();
        }

        int score() {
            return wordsFound.size();
        }
    }
    private WordsFoundSet wordsFoundSet = new WordsFoundSet();

    /**
     * The BWSquareChain ("Bookworm Square Chain") keeps track of the chain of letters that the
     * player creates. It keeps track of the word being formed by the player's square chain and
     * makes sure they can only add squares that have not been added yet and that are adjacent to
     * the most recently added square. It also handles giving some visual instructions.
     */
    private class BWSquareChain {
        LinkedList<TextView> textViewLinkedList = new LinkedList<>();

        String word = "";

        boolean contains(TextView v) {
            for (TextView w : textViewLinkedList) {
                if (w.getId() == v.getId())
                    return true;
            }
            return false;
        }

        /**
         * Determines whether or not a square is addable to the chain based on 1. whether or not
         * the square is adjacent to the previously added square, and 2. whether or not the square
         * has already been added to the chain.
         * @param v the square whose addability will be determined.
         * @return whether or not the chosen square can be added to the square chain
         */
        boolean isAddable(TextView v) {
            if (textViewLinkedList.isEmpty())
                return true;
            if (contains(v))
                return false;
            TextView endOfWord = textViewLinkedList.getLast();
            int[] pos = idToSquinfo.get(endOfWord.getId()).pos;
            for (TextView w : board.getAdjacents(pos[0], pos[1])) {
                if (w.getId() == v.getId())
                    return true;
            }

            return false;
        }

        /**
         * Update the visuals of a square when it is selected
         * @param v the square that is selected
         */
        void select(TextView v) {
            visual.action(v, Visual.SELECT);
        }

        /**
         * Update the visuals of a square when it is deselected
         * @param v the square that is deselected
         */
        void deselect(TextView v) {
            visual.action(v, Visual.DESELECT);
        }

        /**
         * Add a square to the chain.
         * @param v the square to be added to the chain
         */
        void add(TextView v) {
            if (isAddable(v)) {
                textViewLinkedList.add(v);
                select(v);
                word += v.getText();
            }
        }

        /**
         * remove the square that has most recently been added to the square chain and update the
         * visuals
         * @return the square that was removed from the chain
         */
        TextView backspace() {
            if (!textViewLinkedList.isEmpty()) {
                TextView removed = textViewLinkedList.removeLast();
                deselect(removed);
                word = word.substring(0, word.length() - removed.getText().length());
                return removed;
            } else {
                return null;
            }
        }

        void clear() {
            while (backspace() != null);
            word = "";
        }

        String word () {
            return word;
        }

        String refreshWord() {
            word = "";
            for (TextView v : textViewLinkedList)
                word += v.getText();
            return word;
        }

        boolean isWord() {
            //return Start.lopWordHashMap.get(word) != null;
            return correctWordSet.contains(word());
        }
        List<TextView> list() { return textViewLinkedList; }

        /**
         * This method is like clear, except that when the chain is cleared, the squares disappear
         * and all squares above the ones in the chain fall to fill in the gaps. Then the gaps that
         * are left by that are filled with new squares.
         *
         * Right now this method isn't being used because I thought it would make sense to set the
         * squares' background to yellow upon being submitted, giving the player a visual sense of
         * how far into the round they are.
         */
        void collapseColumns() {
            // make every square in the chain invisible
            for (TextView v : textViewLinkedList)
                v.setAlpha(0.0F);

            // make every square above the squares of the chain fall
            for (int k = 0; k < boardHeight - 1; k++) {
                boolean swapOccured = false;
                for (int i = 0; i < boardWidth; i++) {
                    for (int j = 0; j < boardHeight - 1; j++) {
                        if (buttons[i][j + 1].getAlpha() == 0.0F) {
                            swap(buttons[i][j], buttons[i][j + 1]);
                            swapOccured = true;
                        }
                    }
                }
                if (!swapOccured) break;
            }

            // make the invisible squares reappear with new tiles in them
            for (TextView v : textViewLinkedList) {
                v.setText(fillBoardSquare());
                v.animate().alpha(1.0F).setDuration(500).setStartDelay(500);
            }

            // clear the chain
            clear();
            submitAndPreview.update();
        }

        /**
         * Once a chain is submitted and accepted as a valid word, the squares must remember that
         * they have been used. Once all squares have been used, the round is won.
         */
        void setSquaresToUsed() {
            for (TextView v : textViewLinkedList) {
                visual.action(v, Visual.SET_USED);
            }
        }
    }

    private BWSquareChain chain = new BWSquareChain();

    /**
     * The SubmitAndPreview class handles displaying the word that the player is building as they
     * are building it. It also handles what happens when the check-mark (submit) button is pressed,
     * playing the "nuh-uh" sound if the chain they have build isn't a word and handling the points
     * if the chain is a word.
     */
    private class SubmitAndPreview {

        void onClick() {
            if (chain.isWord()) {
                wordsFoundSet.add(chain.word());
                chain.setSquaresToUsed();
                chain.clear();
                submitAndPreview.update();
                if (allSquaresUsed()) {
                    System.out.println("You win! Yahoo!");

                    //update points and trackers
                    updatePointsAndTrackers(wordsFoundSet.score());

                    // prepare repeatGame arrow to be clicked
                    setAdvanceArrowToBlue();
                    repeatLocked = false;
                    setOptionsRowClickable();

                    // clear the list of words the user has found
                    wordsFoundSet.clear();

                    // stop the player from repeatedly clicking the submit
                    // button and cheesing the game
                    findViewById(R.id.submit).setClickable(false);
                }
            } else {
                playIncorrectSound();
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
        }
    }

    private SubmitAndPreview submitAndPreview = new SubmitAndPreview();

    /**
     * This code handles the pop-up that displays after the user presses and holds a square. Letters
     * similar to the one found in the pressed square will be displayed in the pop-up. If the player
     * selects any of the letters in the pop-up, the pressed square's letter will be replaced with
     * the selected letter.
     */
    private class ReplacementPopup {

        private boolean popupup = false;

        private TextView square;

        public ReplacementPopup() {

        }

        void onReplacementClick(TextView v) {
            square.setText(v.getText());
            chain.refreshWord();
            submitAndPreview.update();
            closePopup();
        }

        void openPopup(TextView v) {
            // store the view so that when a replacement is selected, the view can be changed
            square = v;

            visual.action(v, Visual.FOCUS);
            /*square.animate().z(30);

            int[] pos = idToSquinfo.get(square.getId()).pos;
            for (int i = 0; i < hex.getWidth(); i++) {
                for (int j = 0; j < hex.getHeight(); j++) {
                    if (i == pos[0] && j == pos[1])
                        continue;
                    hex.get(i, j).animate().alpha(0.5F);
                }
            }*/

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

            visual.action(square, Visual.UNFOCUS);

            /*if (square != null) {
                square.animate().z(0);
                int[] pos = idToSquinfo.get(square.getId()).pos;
                for (int i = 0; i < hex.getWidth(); i++) {
                    for (int j = 0; j < hex.getHeight(); j++) {
                        if (i == pos[0] && j == pos[1])
                            continue;
                        hex.get(i, j).animate().alpha(1.0F);
                    }
                }
            }*/

            popupup = false;
        }

        boolean isPopupup() { return popupup; }
    }

    private ReplacementPopup poppy = new ReplacementPopup();

    protected int[] getGameButtons() {
        Integer[] game_buttons = new Integer[gameButtons.size()];
        gameButtons.toArray(game_buttons);
        int[] GAME_BUTTONS = new int[game_buttons.length];
        for (int i = 0; i < game_buttons.length; i++)
            GAME_BUTTONS[i] = game_buttons[i];
        return GAME_BUTTONS;
    }

    protected  int[] getWordImages() { return null; }

    protected int getAudioInstructionsResID() { return 0; }

    protected void centerGamesHomeImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        ConstraintLayout constraintLayout = findViewById(R.id.constraintlayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, R.id.constraintlayout);
        constraintSet.applyTo(constraintLayout);
    }

    private void swap(int[] arr1, int[] arr2) {
        assert arr1.length == arr2.length;
        for (int i = 0; i < arr1.length; i++) {
            int temp = arr1[i];
            arr1[i] = arr2[i];
            arr2[i] = temp;
        }
    }

    int parity(int num) { return (num < 0) ? -1 : 1; }

    /**
     * Swap the squares both visually and in the internal representation of the game board.
     * @param v1 the first square to be swapped
     * @param v2 the second square to be swapped
     */
    private void swap(TextView v1, TextView v2) {
        BWSquareInfo squinfo = idToSquinfo.get(v1.getId());
        BWSquareInfo swapSquinfo = idToSquinfo.get(v2.getId());
        board.swap(squinfo.pos[0], squinfo.pos[1], swapSquinfo.pos[0], swapSquinfo.pos[1]);
        swap(squinfo.pos, swapSquinfo.pos);
        swap(squinfo.originalScreenPos, swapSquinfo.originalScreenPos);

        v2.animate()
                .x(swapSquinfo.originalScreenPos[0])
                .y(swapSquinfo.originalScreenPos[1])
                .setDuration(125);
        v1.animate()
                .x(squinfo.originalScreenPos[0])
                .y(squinfo.originalScreenPos[1])
                .setDuration(125);
    }

    private View.OnTouchListener bwSquareOnClick = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            v.performClick();

            // get the square being dragged
            BWSquareInfo squinfo = idToSquinfo.get(v.getId());

            // get all squares swappable with the dragged square
            LinkedList<TextView> swappableAdjacents = board.getSwappableAdjacents(squinfo.pos[0], squinfo.pos[1]);

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
                            swap((TextView)v, adj);

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
    private String fillBoardSquare() {
        return Start.getRandomTileByFrequency.get().text;
    }

    private void fillBoardWithTiles() {
        Random randy = new Random();
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                buttons[i][j].setText(fillBoardSquare());
                visual.action(buttons[i][j], Visual.RESET);
                /*buttons[i][j].setTextColor(Color.WHITE);

                String wordColorStr = colorList.get(randy.nextInt(5));
                int wordColorNo = Color.parseColor(wordColorStr);
                idToSquinfo.get(buttons[i][j].getId()).backgroundColor = wordColorNo;
                buttons[i][j].setBackgroundColor(wordColorNo);*/

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.australia);


        // The board is set programmatically, based on the boardWidth, boardHeight, and width of the
        // screen. All that you need to do to change the number of squares on the board is change
        // boardWidth and boardHeight.
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
                params.topMargin = yPos + (board.isUp(i) ? 0 : bwSquareDim / 2 ); // stagger the heights of the columns
                squinfo.originalScreenPos = new int[] {params.leftMargin, params.topMargin};
                rl.addView(buttons[i][j], params);

                gameButtons.add(buttons[i][j].getId());
            }
        }

        GAME_BUTTONS = new int[boardWidth * boardHeight];
        // initialize GAME_BUTTONS array
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                GAME_BUTTONS[i*boardWidth + j] = buttons[i][j].getId();
            }
        }

        submitAndPreview.update();

        ImageView submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAndPreview.onClick();
            }
        });
        gameButtons.add(R.id.submit);

        ImageView deleteText = findViewById(R.id.deleteText);
        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chain.backspace();
                submitAndPreview.update();
            }
        });
        gameButtons.add(R.id.deleteText);


        ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);
        gamesHomeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { goBackToEarth(v); }
        });

        ImageView repeatImage = findViewById(R.id.repeatImage);
        repeatImage.bringToFront();

        poppy.closePopup();

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }


        updatePointsAndTrackers(0);
        setOptionsRowClickable();
        playAgain();
    }

    public void repeatGame(View view) {
        System.out.println("Arrow has been pressed");
        if (!repeatLocked) {
            System.out.println("Game has been played again");
            playAgain();
        }

    }

    void playAgain() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        boardFiller.fillBoard();
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }
}
