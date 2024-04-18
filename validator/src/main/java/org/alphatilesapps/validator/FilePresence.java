package org.alphatilesapps.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/// A class to manage checks for whether files exist
public class FilePresence {
    ArrayList<File> files = new ArrayList<>();
    HashSet<String> folders = new HashSet<>();
    HashMap<String, TagData> tagData = new HashMap<>();
    public ArrayList<String> fatalErrors = new ArrayList<>();
    public ArrayList<String> warnings = new ArrayList<>();
    /// Add a required file with subfolder and tag
    public void add(String tag, String subfolder, String file, String mimeType, String reason, boolean optional) {
        folders.add(subfolder);
        files.add(new File(tag, subfolder, file, mimeType, reason, optional));
        tagData.putIfAbsent(tag, new TagData());
    }
    public void check(Validator.GoogleDriveFolder langPack) {
        for(String folderName : folders) {
            try {
                Validator.GoogleDriveFolder folder = langPack.getFolderFromName(folderName);
                for (int i = 0; i < folder.getFolderContents().size();) {
                    Validator.GoogleDriveItem item = folder.getFolderContents().get(i);
                    boolean excess = true;
                    for(File file : files) {
                        if(!file.folderName.equals(folderName) || !file.incorrect) continue;
                        String stripped = item.getName().split("\\.")[0];
                        boolean nameMatches = !file.hasName || file.name.equals(stripped);
                        if(nameMatches && file.mimeTypes.contains(item.getMimeType())) {
                            excess = false;
                            file.incorrect = false;
                            break;
                        }
                    }
                    if (excess) {
                        warnings.add("Item " + item.getName() + " in folder " + folderName + " is not used and will not be downloaded");
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
                if(file.hasName) {
                    if(!file.reason.isEmpty()) {
                        fatalErrors.add("Item " + file.name + ", which is required because "
                                + file.reason + ", is missing from folder " + file.folderName + " or has the wrong mime type");
                    } else {
                        fatalErrors.add("Required item " + file.name + " is missing from folder " + file.folderName + " or has the wrong mime type");
                    }
                } else {
                    fatalErrors.add("Required item of type " + file.mimeTypes + " is missing from folder " + file.folderName);
                }
            }
        }
    }
    public boolean success(String tag) {
        TagData data = tagData.get(tag);
        return !data.failed && data.count > 0;
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
        File(String tag, String folderName, String name, String mimeTypes, String reason, boolean optional) {
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
        }
    }
    static class TagData {
        int count = 0;
        boolean failed = false;
    }
}
