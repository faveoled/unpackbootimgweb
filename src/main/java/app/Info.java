package app;

import java.util.ArrayList;
import java.util.List;

public class Info {
    public int kernelSize;
    public int kernelLoadAddress;
    public int ramdiskSize;
    public int ramdiskLoadAddress;
    public int secondSize;
    public int secondLoadAddress;
    public int tagsLoadAddress;
    public int pageSize;
    public String osVersion;
    public String osPatchLevel;
    public int headerVersion;
    public String bootMagic;
    public String productName;
    public String cmdLine;
    public String extraCmdLine;
    public int recoveryDtboSize;
    public long recoveryDtboOffset;
    public int bootHeaderSize;
    public int dtbSize;
    public long dtbLoadAddress;
    public int bootSignatureSize;

    public static final String OUT_BASE = "boot/";

    public String formatMkBootImg() {
        List<String> params = new ArrayList<>();
        params.add("--header_version");
        params.add(String.valueOf(this.headerVersion));

        if (this.osVersion != null) {
            params.add("--os_version");
            params.add(this.osVersion);
        }
        if (this.osPatchLevel != null) {
            params.add("--os_patch_level");
            params.add(this.osPatchLevel);
        }

        params.add("--kernel");
        params.add(OUT_BASE + "kernel");

        params.add("--ramdisk");
        params.add(OUT_BASE + "ramdisk");

        if (headerVersion <= 2) {
            if (secondSize > 0) {
                params.add("--second");
                params.add("second");
            }
            if (recoveryDtboSize > 0) {
                params.add("--recovery_dtbo");
                params.add(" ");
                params.add(OUT_BASE + "recovery_dtbo");
            }
            if (dtbSize > 0) {
                params.add("--dtb");
                params.add(OUT_BASE + "dtb");
            }

            params.add("--pagesize");
            params.add(toPaddedHex8(this.pageSize));

            // Kernel load address is base + kernel_offset in mkbootimg.py.
            // However we don't know the value of 'base' when unpacking a boot
            // image in this script, so we set 'base' to zero and 'kernel_offset'
            // to the kernel load address, 'ramdisk_offset' to the ramdisk load
            // address, ... etc.
            params.add("--base");
            params.add(toPaddedHex8( 0));

            params.add("--kernel_offset");
            params.add(toPaddedHex8(this.kernelLoadAddress));

            params.add("--ramdisk_offset");
            params.add(toPaddedHex8(this.ramdiskLoadAddress));

            params.add("--second_offset");
            params.add(toPaddedHex8(this.secondLoadAddress));

            params.add("--tags_offset");
            params.add(toPaddedHex8(this.tagsLoadAddress));

            if (headerVersion == 2) {
                params.add("--dtb_offset");
                params.add(toPaddedHex16(this.dtbLoadAddress));
            }

            params.add("--board");
            params.add("'" + this.productName + "'");

            params.add("--cmdline");
            params.add("'" + cmdLine + extraCmdLine + "'");
        } else {
            params.add("--cmdline");
            params.add("'" + cmdLine + "'");
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.size(); i += 2) {
            if (i != 0) {
                result.append(" ");
            }
            result.append(params.get(i));
            result.append(" ");
            result.append(params.get(i + 1));
        }
        return result.toString();
    }

    private static String toPaddedHex8(int value) {
        String hex = Integer.toHexString(value);
        return "0x" + "00000000".substring(hex.length()) + hex;
    }

    private static String toPaddedHex16(long value) {
        String hex = Long.toHexString(value);
        return "0x" + "0000000000000000".substring(hex.length()) + hex;
    }

}
