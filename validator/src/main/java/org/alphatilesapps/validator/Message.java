package org.alphatilesapps.validator;

import java.util.Objects;
import java.util.Set;

public class Message {
    public enum Tag {
        FilePresence,
        Etc
    }
    public String content;
    public Tag tag;
    public Message(Tag tag, String content) {
        this.tag = tag;
        this.content = content;
    }
    public static Set<Tag> allTags() {
        return Set.of(Tag.FilePresence, Tag.Etc);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Message) {
            Message other = (Message)obj;
            return other.tag == tag && other.content.equals(content);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, tag);
    }
}
