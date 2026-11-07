package info.mudbourn.mmscompat.bubble;

import net.minecraft.util.StringRepresentable;

/** Screen state of the computer block. */
public enum ComputerState implements StringRepresentable {
    DEFAULT("default"),
    BOOT("boot"),
    LOGIN("login");

    private final String name;

    ComputerState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
