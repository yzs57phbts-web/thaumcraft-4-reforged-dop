package thaumcraft.item;

import thaumcraft.platform.StackKey;

public final class NodeJarKeys {
    public static final StackKey<NodeJarData> DATA = StackKey.register("node_jar", NodeJarData.CODEC, NodeJarData.STREAM_CODEC);

    private NodeJarKeys() {
    }

    public static void init() {
    }
}
