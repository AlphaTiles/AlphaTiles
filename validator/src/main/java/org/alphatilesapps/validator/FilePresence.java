package org.alphatilesapps.validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/// A class to manage checks for whether files exist
public class FilePresence {
    ArrayList<File> files = new ArrayList<>();
    HashSet<String> folders = new HashSet<>();
    HashMap<String, TagData> tagData = new HashMap<>();
    HashMap<String, Message.Tag> folderTags = new HashMap<>();
    public ArrayList<Message> fatalErrors = new ArrayList<>();
    public ArrayList<Message> warnings = new ArrayList<>();
    public ArrayList<Message> recommendations = new ArrayList<>();
    /// Add a required file with subfolder and tag
    public void add(String tag, String subfolder, String file, String mimeType, String reason, boolean optional, long maxSize) {
        for(File other : files) {
            if(!file.isEmpty() && other.name.equals(file) && other.folderName.equals(subfolder))
                return;
        }
        folders.add(subfolder);
        files.add(new File(tag, subfolder, file, mimeType, reason, optional, maxSize));
        tagData.putIfAbsent(tag, new TagData());
    }
    public void addAll(String tag, String subfolder, ArrayList<String> files, String mimeType, String reason, boolean optional, long maxSize) {
        for(String file : files) {
            add(tag, subfolder, file, mimeType, reason, optional, maxSize);
        }
    }
    /// Use the given Message.Tag for errors about the given file tag
    public void folderMessageTag(String folder, Message.Tag messageTag) {
        folderTags.putIfAbsent(folder, messageTag);
    }
    private Message.Tag fileMessageTag(File file) {
        return folderTags.getOrDefault(file.folderName, Message.Tag.FilePresence);
    }
    public void check(Validator.GoogleDriveFolder langPack, boolean showExcess) {
        ArrayList<ExcessFile> excessFiles = new ArrayList<>();
        for(String folderName : folders) {
            try {
                Validator.GoogleDriveFolder folder = langPack.getFolderFromName(folderName);
                HashMap<String, Integer> counts = new HashMap<>();
                ArrayList<Validator.GoogleDriveItem> contents = folder.getFolderContents();
                for(int i = 0; i < contents.size(); i++) {
                    String key = contents.get(i).getName().split("\\.")[0];
                    counts.merge(key, 1, Integer::sum);
                }
                for (int i = 0; i < folder.getFolderContents().size();) {
                    Validator.GoogleDriveItem item = folder.getFolderContents().get(i);
                    boolean excess = true;
                    String stripped = item.getName().split("\\.")[0];
                    for(File file : files) {
                        if (!file.folderName.equals(folderName) || !file.incorrect) continue;
                        boolean nameMatches = !file.hasName || file.name.equals(stripped);
                        boolean typeMatches = false;
                        for (String type : file.mimeTypes) {
                            if (item.getMimeType().startsWith(type)) {
                                typeMatches = true;
                                break;
                            }
                        }
                        if (nameMatches && typeMatches) {
                            excess = false;
                            file.incorrect = false;
                            if(item.getSize() > file.maxSize) {
                                String human = file.maxSize + " B";
                                if(file.maxSize > 1000_000) {
                                    human = file.maxSize / 1000_000 + " MB";
                                } else if(file.maxSize > 1000) {
                                    human = file.maxSize / 1000 + " KB";
                                }
                                warnings.add(new Message(Message.Tag.Etc, "Item " + item.getName() + " in folder " + folderName + " is too large, should be smaller than " + human));
                            } else if(item.getSize() == 0) {
                                fatalErrors.add(new Message(Message.Tag.Etc, "Item " + item.getName() + " in folder " + folderName + " is zero sized"));
                            }
                            break;
                        }
                    }
                    if (excess) {
                        Message.Tag tag = folderTags.getOrDefault(folderName, Message.Tag.FilePresence);
                        if(showExcess) {
                            if(counts.get(stripped) <= 1) {
                                warnings.add(new Message(tag, "Item " + item.getName() + " in folder " + folderName + " is not used and will not be downloaded"));
                            } else {
                                warnings.add(new Message(tag, "Item " + item.getName() + " in folder " + folderName + " is duplicated"));
                            }
                        }
                        excessFiles.add(new ExcessFile(folderName, stripped));
                        folder.getFolderContents().remove(i);
                    } else {
                        i++;
                    }
                }
            } catch (Exception ignored) {}
        }
        for(File file : files) {
            TagData data = tagData.get(file.tag);
            if(!file.incorrect) {
                data.count++;
            } else if(!file.optional) {
                data.failed = true;
                float minError = 0.4f;
                ExcessFile closestMatch = null;
                for(ExcessFile excess : excessFiles) {
                    float error = wordDistance(excess.name, file.name)/(float)file.name.length();
                    if(error < minError) {
                        minError = error;
                        closestMatch = excess;
                    }
                }
                Message.Tag tag = fileMessageTag(file);
                if(closestMatch != null)
                    recommendations.add(new Message(tag, "Unused item " + closestMatch.name + " and missing item " + file.name + " are similar, did you make a typo?"));
                if(file.hasName) {
                    String extensionNote = "";
                    if(file.name.contains(".")) {
                        String actual = file.name.split("\\.")[0];
                        extensionNote = ". Don't use file extensions in the google sheet, try changing " + file.name + " to " + actual + " there";
                    }
                    if(!file.reason.isEmpty()) {
                        fatalErrors.add(new Message(tag, "Item " + file.name + ", which is required because "
                                + file.reason + ", is missing from folder " + file.folderName + " or has the wrong mime type" + extensionNote));
                    } else {
                        fatalErrors.add(new Message(tag, "Required item " + file.name + " is missing from folder " + file.folderName + " or has the wrong mime type" + extensionNote));
                    }
                } else {
                    fatalErrors.add(new Message(tag, "Required item of type " + file.mimeTypes + " is missing from folder " + file.folderName));
                }
            }
        }
    }
    public boolean okay(String tag) {
        TagData data = tagData.get(tag);
        return data == null || !data.failed;
    }
    public boolean empty(String tag) {
        TagData data = tagData.get(tag);
        return data == null || data.count == 0;
    }

    static class ExcessFile {
        String folder;
        String name;
        public ExcessFile(String folder, String name) {
            this.folder = folder;
            this.name = name;
        }
    }
    static class File {
        boolean optional;
        boolean incorrect = true;
        boolean hasName = true;
        String tag;
        Set<String> mimeTypes;
        String folderName;
        String name;
        String reason;
        long maxSize;
        File(String tag, String folderName, String name, String mimeTypes, String reason, boolean optional, long maxSize) {
            this.tag = tag;
            this.name = name;
            if(name.isBlank()) {
                hasName = false;
            }
            this.folderName = folderName;
            this.optional = optional;
            String[] split = mimeTypes.split(",");
            this.mimeTypes = new HashSet<>();
            for(String type : split) {
                this.mimeTypes.add(type.strip());
            }
            this.reason = reason;
            this.maxSize = maxSize;
        }
    }
    static class TagData {
        int count = 0;
        boolean failed = false;
        Message.Tag messageTag;
    }
    // Word distance algorithm from https://www.baeldung.com/java-levenshtein-distance
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }
    static int wordDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }
}
