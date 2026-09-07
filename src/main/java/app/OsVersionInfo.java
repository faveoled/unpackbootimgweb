package app;

import java.util.Objects;

public class OsVersionInfo {
    private final String osVersion;
    private final String osPatchLevel;

    public OsVersionInfo(String osVersion, String osPatchLevel) {
        this.osVersion = osVersion;
        this.osPatchLevel = osPatchLevel;
    }

    public String osVersion() {
        return osVersion;
    }

    public String osPatchLevel() {
        return osPatchLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        OsVersionInfo that = (OsVersionInfo) obj;
        return Objects.equals(this.osVersion, that.osVersion) &&
                Objects.equals(this.osPatchLevel, that.osPatchLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(osVersion, osPatchLevel);
    }

    @Override
    public String toString() {
        return "OsVersionInfo[" +
                "osVersion=" + osVersion + ", " +
                "osPatchLevel=" + osPatchLevel + ']';
    }
}
