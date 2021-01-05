package com.mining.mining.rar;

import com.mining.mining.rar.exception.RarException;
import com.mining.mining.rar.rarfile.FileHeader;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;


public class RarDecompressionUtil {

    private static final String SEPARATOR = File.separator;


    /**
     * 解压指定RAR文件到当前文件夹
     */
    public static void unrar(String srcRar, String password) throws IOException {
        unrar(srcRar, null, password);
    }

    /**
     * 解压指定的RAR压缩文件到指定的目录中
     */
    public static void unrar(String srcRar, String destPath, String password) throws IOException {
        File srcFile = new File(srcRar);
        if (!srcFile.exists()) {
            return;
        }
        if (null == destPath || destPath.length() == 0) {
            unrar(srcFile, srcFile.getParent(), password);
            return;
        }
        unrar(srcFile, destPath, password);
    }

    /**
     * 解压指定RAR文件到当前文件夹
     */
    public static void unrar(File srcRarFile, String password) throws IOException {
        if (null == srcRarFile || !srcRarFile.exists()) {
            throw new IOException("文件不存在.");
        }
        unrar(srcRarFile, srcRarFile.getParent(), password);
    }

    /**
     * 解压指定RAR文件到指定的路径
     */
    public static void unrar(File srcRarFile, String destPath, String password) throws IOException {
        if (null == srcRarFile || !srcRarFile.exists()) {
            throw new IOException("压缩文件不存在.");
        }
        if (!destPath.endsWith(SEPARATOR)) {
            destPath += SEPARATOR;
        }
        Archive archive;
        OutputStream unOut = null;
        try {
            archive = new Archive(srcRarFile, new UnrarCallback() {
                @Override
                public boolean isNextVolumeReady(File nextVolume) {
                    return false;
                }

                @Override
                public void volumeProgressChanged(long current, long total) {
                }
            }, password, false);
            FileHeader fileHeader = archive.nextFileHeader();
            while (null != fileHeader) {
                if (!fileHeader.isDirectory()) {
                    // 1 根据不同的操作系统拿到相应的 destDirName 和 destFileName
                    String destFileName;
                    String destDirName;
                    if (SEPARATOR.equals("/")) {
                        // 非windows系统
                        destFileName = getDestFileNameString(destPath, fileHeader);
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("/"));
                    } else {
                        // windows系统
                        destFileName = getDestFileNameString(destPath, fileHeader);
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("\\"));
                    }
                    // 2创建文件夹
                    File dir = new File(destDirName);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    // 抽取压缩文件
                    unOut = new FileOutputStream(new File(destFileName));
                    archive.extractFile(fileHeader, unOut);
                    unOut.flush();
                    unOut.close();
                }
                fileHeader = archive.nextFileHeader();
            }
            archive.close();
        } catch (RarException e) {
            e.printStackTrace();
        } finally {
            if (unOut != null) {
                unOut.close();
            }
        }
    }

    /**
     *
     * @param srcRarFile
     * @param destPath
     * @param keyFile
     * @param queue
     * @param stream
     */
    public static void unrar_v2(File srcRarFile, String destPath, String keyFile, LinkedBlockingQueue<String> queue, ByteArrayOutputStream stream) {
        Archive archive = null;
        OutputStream unOut = null;
        try {
            String password = queue.take();
            System.out.println(Thread.currentThread().getName() + ":" + password);
            archive = new Archive(srcRarFile, password, true);

            FileHeader fileHeader = archive.nextFileHeader();
            while (null != fileHeader) {
                if (!fileHeader.isDirectory()) {
                    stream.reset();
                    archive.extractFile(fileHeader, stream);

                    FileWriter writer = new FileWriter(keyFile, true);
                    writer.write("\n");
                    writer.write(password);
                    writer.flush();
                    writer.close();

                    FileOutputStream fileOutputStream = new FileOutputStream(destPath + getFileName(fileHeader));
                    fileOutputStream.write(stream.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                fileHeader = archive.nextFileHeader();
            }
        } catch (Exception e) {
        } finally {
            if (null != archive) {
                try {
                    archive.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (unOut != null) {
                try {
                    unOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getDestFileNameString(String destPath, FileHeader fileHeader) {
        String destFileName;
        String fileNameW = fileHeader.getFileNameW();
        if (null == fileNameW || "".equals(fileNameW)) {
            destFileName = (destPath + fileHeader.getFileNameString()).replaceAll("/", "\\\\");
        } else {
            destFileName = (destPath + fileHeader.getFileNameW()).replaceAll("/", "\\\\");
        }
        return destFileName;
    }

    private static String getFileName(FileHeader fileHeader) {
        String fileNameW = fileHeader.getFileNameW();
        if (null == fileNameW || "".equals(fileNameW)) {
            return Thread.currentThread().getName() + ":" + fileHeader.getFileNameString().replaceAll("/", "\\\\");
        } else {
            return Thread.currentThread().getName() + ":" + fileHeader.getFileNameW().replaceAll("/", "\\\\");
        }
    }
}