/**
 * MIT License
 *
 * Copyright (c) 2010 - 2020 The OSHI Project Contributors: https://github.com/oshi/oshi/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package oshi.demo;

import java.io.File;
import java.net.URISyntaxException;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.tuples.Pair;

/**
 * Uses OSHI to attempt to identify which OSFileStore, HWDiskStore, and
 * HWPartition a file resides on. Intended as a demonstration, not intended to
 * be used in production code.
 */
public class DiskStoreForPath {
    /**
     * Main method
     * 
     * @param args
     *            Optional file path
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException {
        // Use the arg as a file path or get this class's path
        String filePath = args.length > 0 ? args[0]
                : new File(
                        DiskStoreForPath.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getPath();
        System.out.println("Searching stores for path: " + filePath);

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        HWDiskStore[] diskStores = hal.getDiskStores();
        Pair<Integer, Integer> dsPartIdx = getDiskStoreAndPartitionForPath(filePath, diskStores);
        int dsIndex = dsPartIdx.getA();
        int partIndex = dsPartIdx.getB();

        System.out.println();
        System.out.println("DiskStore index " + dsIndex + " and Partition index " + partIndex);
        if (dsIndex >= 0 && partIndex >= 0) {
            System.out.println(diskStores[dsIndex]);
            System.out.println(" |-- " + diskStores[dsIndex].getPartitions()[partIndex]);
        } else {
            System.out.println("Couldn't find that path on a partition.");
        }

        OperatingSystem os = si.getOperatingSystem();
        OSFileStore[] fileStores = os.getFileSystem().getFileStores();
        int fsIndex = getFileStoreForPath(filePath, fileStores);

        System.out.println();
        System.out.println("FileStore index " + fsIndex);
        if (fsIndex >= 0) {
            System.out.println(fileStores[fsIndex]);
        } else {
            System.out.println("Couldn't find that path on a filestore.");
        }
    }

    private static Pair<Integer, Integer> getDiskStoreAndPartitionForPath(String path, HWDiskStore[] diskStores) {
        for (int ds = 0; ds < diskStores.length; ds++) {
            HWDiskStore store = diskStores[ds];
            HWPartition[] parts = store.getPartitions();
            for (int part = 0; part < parts.length; part++) {
                String mount = parts[part].getMountPoint();
                if (!mount.isEmpty() && path.substring(0, mount.length()).equalsIgnoreCase(mount)) {
                    return new Pair<>(ds, part);
                }
            }
        }
        return new Pair<>(-1, -1);
    }

    private static int getFileStoreForPath(String path, OSFileStore[] fileStores) {
        for (int fs = 0; fs < fileStores.length; fs++) {
            String mount = fileStores[fs].getMount();
            if (!mount.isEmpty() && path.substring(0, mount.length()).equalsIgnoreCase(mount)) {
                return fs;
            }
        }
        return -1;
    }
}