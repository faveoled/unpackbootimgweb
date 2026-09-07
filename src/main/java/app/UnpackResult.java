package app;

import java.util.List;

public class UnpackResult {
    private final Info info;
    private final List<BootComponent> components;

    public UnpackResult(Info info, List<BootComponent> components) {
        this.info = info;
        this.components = components;
    }

    public Info getInfo() {
        return info;
    }

    public List<BootComponent> getComponents() {
        return components;
    }
}
