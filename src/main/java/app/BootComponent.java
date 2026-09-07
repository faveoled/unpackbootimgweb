package app;

import elemental2.core.TypedArray;

public class BootComponent {
    private final String name;
    private final long offset;
    private final int size;
    private final TypedArray data;

    public BootComponent(String name, long offset, int size, TypedArray data) {
        this.name = name;
        this.offset = offset;
        this.size = size;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public long getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public TypedArray getData() {
        return data;
    }
}
