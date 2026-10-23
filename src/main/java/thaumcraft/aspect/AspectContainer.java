package thaumcraft.aspect;

public interface AspectContainer {
    AspectList getAspects();

    void setAspects(AspectList aspects);

    boolean doesContainerAccept(Aspect aspect);

    int addToContainer(Aspect aspect, int amount);

    boolean takeFromContainer(Aspect aspect, int amount);

    boolean doesContainerContainAmount(Aspect aspect, int amount);

    int containerContains(Aspect aspect);
}
