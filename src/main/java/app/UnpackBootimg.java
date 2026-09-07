package app;

import elemental2.core.TypedArray;
import elemental2.core.Uint8Array;

import java.util.ArrayList;
import java.util.List;

public class UnpackBootimg {

    private static int BOOT_IMAGE_HEADER_V3_PAGESIZE = 4096;

    // Helper method matching unpack_bootimg behavior if needed
    public static int getNumberOfPages(int imageSize, int pageSize) {
        return (imageSize + pageSize - 1) / pageSize;
    }

    public static String formatOsVersion(int osVersion) {
        if (osVersion == 0) {
            return null;
        }
        int a = osVersion >> 14;
        int b = (osVersion >> 7) & ((1 << 7) - 1);
        int c = osVersion & ((1 << 7) - 1);
        return a + "." + b + "." + c;
    }

    public static String formatOsPatchLevel(int osPatchLevel) {
        if (osPatchLevel == 0) {
            return null;
        }
        int y = (osPatchLevel >> 4) + 2000;
        int m = osPatchLevel & 0x0F;

        StringBuilder sb = new StringBuilder(7);
        sb.append(y).append('-');
        if (m < 10) {
            sb.append('0');
        }
        return sb.append(m).toString();
    }

    public static OsVersionInfo decodeOsVersionPatchLevel(int osVersionPatchLevel) {
        int osVersion = osVersionPatchLevel >> 11;
        int osPatchLevel = osVersionPatchLevel & ((1 << 11) - 1);
        return new OsVersionInfo(
                formatOsVersion(osVersion),
                formatOsPatchLevel(osPatchLevel)
        );
    }

    public static UnpackResult unpackBoot(Uint8Array bootImg) {
        Info info = new Info();
        ByteSeeker boot = new ByteSeeker(bootImg);
        info.bootMagic = boot.readS(8);
        int[] kernelRamdiskSecondInfo = new int[9];
        for (int i = 0; i < 9; i++) {
            kernelRamdiskSecondInfo[i] = boot.readI();
        }
        // header version is always at [8] regardless of the value of header version.
        info.headerVersion = kernelRamdiskSecondInfo[8];

        int osVersionPatchLevel;
        if (info.headerVersion < 3) {
            info.kernelSize = kernelRamdiskSecondInfo[0];
            info.kernelLoadAddress = kernelRamdiskSecondInfo[1];
            info.ramdiskSize = kernelRamdiskSecondInfo[2];
            info.ramdiskLoadAddress = kernelRamdiskSecondInfo[3];
            info.secondSize = kernelRamdiskSecondInfo[4];
            info.secondLoadAddress = kernelRamdiskSecondInfo[5];
            info.tagsLoadAddress = kernelRamdiskSecondInfo[6];
            info.pageSize = kernelRamdiskSecondInfo[7];
            osVersionPatchLevel = boot.readI();
        } else {
            info.kernelSize = kernelRamdiskSecondInfo[0];
            info.ramdiskSize = kernelRamdiskSecondInfo[1];
            osVersionPatchLevel = kernelRamdiskSecondInfo[2];
            info.secondSize = 0;
            info.pageSize = BOOT_IMAGE_HEADER_V3_PAGESIZE;
        }
        OsVersionInfo osVerInfo = decodeOsVersionPatchLevel(osVersionPatchLevel);
        info.osVersion = osVerInfo.osVersion();
        info.osPatchLevel = osVerInfo.osPatchLevel();

        if (info.headerVersion < 3) {
            info.productName = boot.readS(16);
            info.cmdLine = boot.readS(512);
            boot.skip(32); // ignore SHA
            info.extraCmdLine = boot.readS(1024);
        } else {
            info.cmdLine = boot.readS(1536);
        }

        if (info.headerVersion == 1 || info.headerVersion == 2) {
            info.recoveryDtboSize = boot.readI();
            info.recoveryDtboOffset = boot.readQ();
            info.bootHeaderSize = boot.readI();
        } else {
            info.recoveryDtboSize = 0;
            info.recoveryDtboOffset = -999;
        }

        if (info.headerVersion == 2) {
            info.dtbSize = boot.readI();
            info.dtbLoadAddress = boot.readQ();
        } else {
            info.dtbSize = 0;
            info.dtbLoadAddress = 0;
        }

        if (info.headerVersion >= 4) {
            info.bootSignatureSize = boot.readI();
        } else {
            info.bootSignatureSize = 0;
        }

        int numHeaderPages = 1;

        int numKernelPages = getNumberOfPages(info.kernelSize, info.pageSize);
        int kernelOffset = info.pageSize * numHeaderPages;  // header occupies a page
        List<ImageInfo> imageInfoList = new ArrayList<>();
        imageInfoList.add(new ImageInfo(kernelOffset, info.kernelSize, "kernel"));

        int numberOfPages = getNumberOfPages(info.ramdiskSize, info.pageSize);
        int ramdiskOffset = info.pageSize * (numHeaderPages + numKernelPages); // header + kernel

        imageInfoList.add(new ImageInfo(ramdiskOffset, info.ramdiskSize, "ramdisk"));

        if (info.secondSize > 0) {
            int secondOffset = info.pageSize * (numHeaderPages + numKernelPages + numberOfPages);  // header + kernel + ramdisk
            imageInfoList.add(new ImageInfo(secondOffset, info.secondSize, "second"));
        }

        if (info.recoveryDtboSize > 0) {
            imageInfoList.add(new ImageInfo(info.recoveryDtboOffset, info.recoveryDtboSize, "recovery_dtbo"));
        }

        if (info.dtbSize > 0) {
            int numSecondPages = getNumberOfPages(info.secondSize, info.pageSize);
            int numRecoveryDtboPages = getNumberOfPages(info.recoveryDtboSize, info.pageSize);
            long dtbOffset = (long)info.pageSize * (numHeaderPages + numKernelPages + numberOfPages + numSecondPages + numRecoveryDtboPages);
            imageInfoList.add(new ImageInfo(dtbOffset, info.dtbSize, "dtb"));
        }

        if (info.bootSignatureSize > 0) {
            // boot signature only exists in boot.img version >= v4.
            // There are only kernel and ramdisk pages before the signature.
            int bootSignatureOffset = info.pageSize * (numHeaderPages + numKernelPages + numberOfPages);
            imageInfoList.add(new ImageInfo(bootSignatureOffset, info.bootSignatureSize, "boot_signature"));
        }

        List<BootComponent> components = new ArrayList<>();
        for (ImageInfo ii : imageInfoList) {
            TypedArray data = boot.getRaw((int) ii.offset(), ii.size());
            components.add(new BootComponent(ii.name(), ii.offset(), ii.size(), data));
        }

        return new UnpackResult(info, components);
    }
}
