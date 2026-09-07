package app;

import java.util.Objects;

public final class ImageInfo {


    enum FieldType {
        INT, LONG
    }

    private final FieldType fieldType;
    private final long offset;
    private final int size;
    private final String name;

    public ImageInfo(int offset, int size, String name) {
        fieldType = FieldType.INT;
        this.offset = offset;
        this.size = size;
        this.name = name;
    }

    public ImageInfo(long offset, int size, String name) {
        fieldType = FieldType.LONG;
        this.offset = offset;
        this.size = size;
        this.name = name;
    }

    public long offset() {
        return offset;
    }

    public int size() {
        return size;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ImageInfo that = (ImageInfo) obj;
        return this.offset == that.offset &&
                this.size == that.size &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, size, name);
    }

    @Override
    public String toString() {
        return "ImageInfo[" +
                "offset=" + offset + ", " +
                "size=" + size + ", " +
                "name=" + name + ']';
    }
}
