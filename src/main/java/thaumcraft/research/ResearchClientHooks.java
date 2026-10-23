package thaumcraft.research;

public final class ResearchClientHooks {
    private static Runnable bookOpener = () -> {
    };

    private ResearchClientHooks() {
    }

    public static void setBookOpener(Runnable opener) {
        bookOpener = opener;
    }

    public static void openBook() {
        bookOpener.run();
    }
}
