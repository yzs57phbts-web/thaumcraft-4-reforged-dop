package thaumcraft.research;

import thaumcraft.platform.StackKey;

public final class ResearchNoteKeys {
    public static final StackKey<ResearchNote> NOTE = StackKey.register("research_note", ResearchNote.CODEC, ResearchNote.STREAM_CODEC);

    private ResearchNoteKeys() {
    }

    public static void init() {
    }
}
