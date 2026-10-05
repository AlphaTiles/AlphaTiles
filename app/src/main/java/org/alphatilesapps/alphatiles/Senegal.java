package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Senegal extends GameActivity {
    private static final int GRID_SIZE = 7;
    private static final int TOTAL_TILES = 49;
    private static final String[] WORD_COLORS = {"#E31B23", "#FDEF42", "#00853F"}; // Red, Yellow, Green
    private List<Start.Word> chosenWords = new ArrayList<>();

    // State variables for dynamic validation
    private List<PathNode> solutionPath = new ArrayList<>();
    private List<PathNode> currentTargetSequence = new ArrayList<>();
    private String[][] gridLayout = new String[GRID_SIZE][GRID_SIZE];
    private List<Integer> userPathIndices = new ArrayList<>();

    private int currentIndex = 0;

    // Single random instance to prevent memory thrashing during recursion
    private Random dfsRandom = new Random();

    // Parsed configuration
    private int gameLevel;
    private int wordCount;

    protected static final int[] GAME_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07,
            R.id.tile08, R.id.tile09, R.id.tile10, R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14,
            R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20, R.id.tile21,
            R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28,
            R.id.tile29, R.id.tile30, R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35,
            R.id.tile36, R.id.tile37, R.id.tile38, R.id.tile39, R.id.tile40, R.id.tile41, R.id.tile42,
            R.id.tile43, R.id.tile44, R.id.tile45, R.id.tile46, R.id.tile47, R.id.tile48, R.id.tile49
    };

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage1, R.id.wordImage2, R.id.wordImage3,
            R.id.wordImage4, R.id.wordImage5, R.id.wordImage6
    };

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int resID;
        try {
            resID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            resID = 0;
        }
        return resID;
    }

    @Override
    protected void hideInstructionAudioImage() {
        ImageView instructionsButton = findViewById(R.id.instructions);
        if (instructionsButton != null) instructionsButton.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.senegal);
        context = this;

        ActivityLayouts.applyEdgeToEdge(this, R.id.senegalCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            fixConstraintsRTL(R.id.senegalCL);
        }

        visibleGameButtons = TOTAL_TILES;
        incorrectAnswersSelected = new ArrayList<>(visibleGameButtons);
        for (int i = 0; i < visibleGameButtons; i++) {
            incorrectAnswersSelected.add("");
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        // Parse 2-digit challenge level (e.g., 14 -> Level 1, 4 Words)
        if (challengeLevel >= 10) {
            gameLevel = challengeLevel / 10;
            wordCount = challengeLevel % 10;
        } else {
            gameLevel = challengeLevel; // Fallback
            wordCount = 3;
        }
        // Bound word count to available UI slots
        wordCount = Math.max(3, Math.min(6, wordCount));

        updatePointsAndTrackers(0);
        initializeGame();
    }

    private void initializeGame() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        currentIndex = 0;
        chosenWords.clear();
        solutionPath.clear();
        currentTargetSequence.clear();
        userPathIndices.clear();
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();

        selectWordsAndGeneratePath();

        // Bind Words to Images dynamically up to 6
        for (int i = 0; i < WORD_IMAGES.length; i++) {
            ImageView img = findViewById(WORD_IMAGES[i]);
            img.setBackgroundResource(R.drawable.tile_white_background);
            img.getBackground().clearColorFilter();
            img.setAlpha(1.0f);

            if (i < chosenWords.size()) {
                Start.Word w = chosenWords.get(i);
                int resID = getResources().getIdentifier(w.wordInLWC, "drawable", getPackageName());
                if (resID != 0) img.setImageResource(resID);
                img.setVisibility(View.VISIBLE);
            } else {
                // Use GONE instead of INVISIBLE so the remaining images expand to fill the layout weight
                img.setVisibility(View.GONE);
            }
        }

        fillGridWithLetters();
        setAllGameButtonsClickable();
        setOptionsRowClickable();
    }

    // HELPER: Get allowed moves based on Level
    private int[][] getAllowedMoves() {
        if (gameLevel == 1) {
            // Level 1: Orthogonal only
            return new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        } else {
            // Level 2+: All 8 directions
            return new int[][]{{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        }
    }

    // HELPER: Get minimum distance based on Level's movement rules
    private int getMinDistance(int x, int y) {
        int targetX = GRID_SIZE - 1;
        int targetY = GRID_SIZE - 1;
        if (gameLevel == 1) {
            return Math.abs(targetX - x) + Math.abs(targetY - y); // Manhattan distance
        } else {
            return Math.max(Math.abs(targetX - x), Math.abs(targetY - y)); // Chebyshev distance
        }
    }

    private void selectWordsAndGeneratePath() {
        Random rand = new Random();
        List<Start.Word> wordPool = new ArrayList<>(cumulativeStageBasedWordList);
        if (wordPool.isEmpty()) wordPool = new ArrayList<>(Start.wordList);
        if (wordPool.isEmpty()) return;

        int attempts = 0;
        while (attempts < 1000 && solutionPath.isEmpty()) {
            chosenWords.clear();
            List<Start.Word> tempPool = new ArrayList<>(wordPool);
            Collections.shuffle(tempPool);

            int totalLetters = 0;
            for (Start.Word w : tempPool) {
                if (chosenWords.size() == wordCount) break;
                int len = Start.tileList.parseWordIntoTiles(w.wordInLOP, w).size();
                if (totalLetters + len <= TOTAL_TILES) {
                    chosenWords.add(w);
                    totalLetters += len;
                }
            }

            if (chosenWords.size() == wordCount) {
                boolean isValidLength;
                if (gameLevel == 1) {
                    // Level 1 Requires Manhattan distance of at least 13 tiles, and MUST be an odd number
                    isValidLength = (totalLetters >= 13 && totalLetters <= TOTAL_TILES && totalLetters % 2 != 0);
                } else {
                    // Level 2 Requires at least enough tiles to span the grid directly
                    isValidLength = (totalLetters >= GRID_SIZE && totalLetters <= TOTAL_TILES);
                }

                if (isValidLength) {
                    solutionPath = attemptPathGeneration();
                }
            }
            attempts++;
        }

        // Failsafe - Ensure we don't attempt an impossible grid fill
        if (solutionPath.isEmpty() && !chosenWords.isEmpty()) {
            int totalLetters = 0;
            for (Start.Word w : chosenWords) {
                totalLetters += Start.tileList.parseWordIntoTiles(w.wordInLOP, w).size();
            }
            boolean isValidLength;
            if (gameLevel == 1) {
                isValidLength = (totalLetters >= 13 && totalLetters <= TOTAL_TILES && totalLetters % 2 != 0);
            } else {
                isValidLength = (totalLetters >= GRID_SIZE && totalLetters <= TOTAL_TILES);
            }

            if (isValidLength) {
                solutionPath = attemptPathGeneration();
            } else {
                chosenWords.clear(); // Clear to prevent a partial/broken game state
            }
        }
    }

    private List<PathNode> attemptPathGeneration() {
        List<PathNode> targetSequence = new ArrayList<>();
        for (int w = 0; w < chosenWords.size(); w++) {
            Start.Word word = chosenWords.get(w);
            List<Start.Tile> parsedTiles = Start.tileList.parseWordIntoTiles(word.wordInLOP, word);
            for (int i = 0; i < parsedTiles.size(); i++) {
                targetSequence.add(new PathNode(-1, -1, parsedTiles.get(i), word, w, (i == parsedTiles.size() - 1)));
            }
        }

        List<PathNode> path = new ArrayList<>();
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        int[] stepCounter = {0}; // Track recursion depth to prevent ANRs

        if (dfs(0, 0, 0, targetSequence, path, visited, stepCounter)) {
            currentTargetSequence = targetSequence;
            return path;
        }
        return new ArrayList<>();
    }

    private boolean dfs(int x, int y, int step, List<PathNode> targetSeq, List<PathNode> path, boolean[][] visited, int[] stepCounter) {
        stepCounter[0]++;
        if (stepCounter[0] > 50000) return false; // Hard limit to prevent main thread lockup

        if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE || visited[x][y]) return false;

        int remainingSteps = targetSeq.size() - 1 - step;
        int distToGoal = getMinDistance(x, y);

        if (distToGoal > remainingSteps) return false;

        PathNode nodeTemplate = targetSeq.get(step);
        PathNode actualNode = new PathNode(x, y, nodeTemplate.tile, nodeTemplate.word, nodeTemplate.wordIndex, nodeTemplate.isWordEnd);

        path.add(actualNode);
        visited[x][y] = true;

        if (step == targetSeq.size() - 1) {
            if (x == GRID_SIZE - 1 && y == GRID_SIZE - 1) return true;
        } else {
            int[][] moves = getAllowedMoves();
            int[] indices = new int[moves.length];
            for (int i = 0; i < moves.length; i++) indices[i] = i;

            // In-place Fisher-Yates shuffle to avoid memory thrashing inside the loop
            for (int i = indices.length - 1; i > 0; i--) {
                int index = dfsRandom.nextInt(i + 1);
                int temp = indices[index];
                indices[index] = indices[i];
                indices[i] = temp;
            }

            for (int i = 0; i < indices.length; i++) {
                int[] move = moves[indices[i]];
                if (dfs(x + move[0], y + move[1], step + 1, targetSeq, path, visited, stepCounter)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        visited[x][y] = false;
        return false;
    }

    private void fillGridWithLetters() {
        for(int i = 0; i < GRID_SIZE; i++) {
            for(int j = 0; j < GRID_SIZE; j++) {
                gridLayout[i][j] = null;
            }
        }

        for (PathNode node : solutionPath) {
            gridLayout[node.x][node.y] = node.tile.text;
        }

        List<Start.Tile> allTiles = new ArrayList<>(Start.tileList);
        Random rand = new Random();
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (gridLayout[x][y] == null) {
                    gridLayout[x][y] = allTiles.get(rand.nextInt(allTiles.size())).text;
                }

                int buttonIndex = y * GRID_SIZE + x;
                TextView tileView = findViewById(GAME_BUTTONS[buttonIndex]);
                tileView.setText(gridLayout[x][y]);
                tileView.setBackgroundResource(R.drawable.tile_white_background);
                tileView.getBackground().clearColorFilter();
                tileView.setTextColor(Color.parseColor("#333333"));

                if (x == 0 && y == 0) {
                    tileView.getBackground().setColorFilter(Color.parseColor("#E0E0E0"), PorterDuff.Mode.SRC_ATOP);
                } else if (x == GRID_SIZE - 1 && y == GRID_SIZE - 1) {
                    tileView.getBackground().setColorFilter(Color.parseColor("#FFCDD2"), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public void onTileClick(View view) {
        if (mediaPlayerIsPlaying || view.getTag() == null) return;

        int buttonIndex = Integer.parseInt(view.getTag().toString());
        int x = buttonIndex % GRID_SIZE;
        int y = buttonIndex / GRID_SIZE;

        if (currentIndex < currentTargetSequence.size()) {
            PathNode expectedNode = currentTargetSequence.get(currentIndex);
            String clickedText = gridLayout[x][y];

            boolean isCorrect = false;

            if (clickedText != null && clickedText.equals(expectedNode.tile.text)) {
                boolean isValidAdjacency = false;

                if (currentIndex == 0) {
                    isValidAdjacency = (x == 0 && y == 0);
                } else {
                    int lastIndex = userPathIndices.get(userPathIndices.size() - 1);
                    int lastX = lastIndex % GRID_SIZE;
                    int lastY = lastIndex / GRID_SIZE;
                    int dx = Math.abs(x - lastX);
                    int dy = Math.abs(y - lastY);

                    if (gameLevel == 1) {
                        // Level 1: Orthogonal adjacency
                        if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
                            if (!userPathIndices.contains(buttonIndex)) isValidAdjacency = true;
                        }
                    } else {
                        // Level 2+: All 8 directions
                        if (dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0)) {
                            if (!userPathIndices.contains(buttonIndex)) isValidAdjacency = true;
                        }
                    }
                }

                if (isValidAdjacency) {
                    boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
                    for (int idx : userPathIndices) {
                        visited[idx % GRID_SIZE][idx / GRID_SIZE] = true;
                    }

                    if (canCompletePath(x, y, currentIndex, visited)) {
                        isCorrect = true;
                    }
                }
            }

            if (isCorrect) {
                userPathIndices.add(buttonIndex);

                TextView tileView = findViewById(GAME_BUTTONS[buttonIndex]);
                // This naturally loops over the 3 Senegal colors regardless of word count
                int colorIndex = expectedNode.wordIndex % WORD_COLORS.length;
                int color = Color.parseColor(WORD_COLORS[colorIndex]);

                tileView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                if (colorIndex == 1) {
                    tileView.setTextColor(Color.parseColor("#333333"));
                } else {
                    tileView.setTextColor(Color.WHITE);
                }

                tileView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start();

                currentIndex++;

                if (expectedNode.isWordEnd) {
                    refWord = expectedNode.word;

                    ImageView img = findViewById(WORD_IMAGES[expectedNode.wordIndex]);
                    if(img != null) {
                        img.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        img.setAlpha(0.6f);
                        img.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start();
                    }

                    boolean isFinalWord = (currentIndex == currentTargetSequence.size());
                    playCorrectSoundThenActiveWordClip(false);

                    if (isFinalWord) {
                        repeatLocked = false;
                        setAdvanceArrowToBlue();
                        updatePointsAndTrackers(1);
                    }
                }
            } else {
                incorrectOnLevel++;
                playIncorrectSound();
                View wrongTile = findViewById(GAME_BUTTONS[buttonIndex]);
                wrongTile.getBackground().setColorFilter(Color.parseColor("#ff4444"), PorterDuff.Mode.SRC_ATOP);
                wrongTile.animate().translationXBy(15).setDuration(50).withEndAction(() ->
                        wrongTile.animate().translationXBy(-30).setDuration(50).withEndAction(() ->
                                wrongTile.animate().translationXBy(15).setDuration(50).withEndAction(() -> {
                                    wrongTile.getBackground().clearColorFilter();
                                }).start()
                        ).start()
                ).start();
            }
        }
    }

    private boolean canCompletePath(int x, int y, int step, boolean[][] visited) {
        if (step == currentTargetSequence.size() - 1) {
            return x == GRID_SIZE - 1 && y == GRID_SIZE - 1;
        }

        int remainingSteps = currentTargetSequence.size() - 1 - step;
        int distToGoal = getMinDistance(x, y);
        if (distToGoal > remainingSteps) return false;

        visited[x][y] = true;
        int[][] moves = getAllowedMoves();

        for (int[] move : moves) {
            int nx = x + move[0];
            int ny = y + move[1];

            if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && !visited[nx][ny]) {
                if (gridLayout[nx][ny] != null && gridLayout[nx][ny].equals(currentTargetSequence.get(step + 1).tile.text)) {
                    if (canCompletePath(nx, ny, step + 1, visited)) {
                        visited[x][y] = false;
                        return true;
                    }
                }
            }
        }

        visited[x][y] = false;
        return false;
    }

    public void onImageClick(View view) {
        if (mediaPlayerIsPlaying || view.getTag() == null) return;
        int index = Integer.parseInt(view.getTag().toString());
        if (index < chosenWords.size()) {
            refWord = chosenWords.get(index);
            playActiveWordClip(false);
        }
    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            for(int i : WORD_IMAGES) {
                findViewById(i).animate().scaleX(1.0f).scaleY(1.0f).setDuration(0).start();
            }
            for(int i : GAME_BUTTONS) {
                findViewById(i).animate().scaleX(1.0f).scaleY(1.0f).setDuration(0).start();
            }
            initializeGame();
        }
    }

    private static class PathNode {
        int x, y;
        Start.Tile tile;
        Start.Word word;
        int wordIndex;
        boolean isWordEnd;

        PathNode(int x, int y, Start.Tile tile, Start.Word word, int wordIndex, boolean isWordEnd) {
            this.x = x;
            this.y = y;
            this.tile = tile;
            this.word = word;
            this.wordIndex = wordIndex;
            this.isWordEnd = isWordEnd;
        }
    }
}