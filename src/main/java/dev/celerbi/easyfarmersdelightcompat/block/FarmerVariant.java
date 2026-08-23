package dev.celerbi.easyfarmersdelightcompat.block;

public enum FarmerVariant {
    PADDY(false, true),
    RICH(true, false),
    RICH_PADDY(true, true);

    private final boolean rich;
    private final boolean aquatic;

    FarmerVariant(boolean rich, boolean aquatic) {
        this.rich = rich;
        this.aquatic = aquatic;
    }

    public boolean isRich() {
        return rich;
    }

    public boolean isAquatic() {
        return aquatic;
    }
}
