package com.mining.mining.rar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mining.mining.rar.Archive;
import com.mining.mining.rar.exception.RarException;
import com.mining.mining.rar.rarfile.FileHeader;

public class RarTest {

    public static void main(String[] args) {
        unZipRAR("D:\\a\\");
        System.out.println("有" + count + "个文件");
    }


    private static int count = 0;

    public static void unZipRAR(String path) {
        File file = new File(path);
        if (!file.exists()) {
            // 如果文件存在
            System.err.println("文件夹不存在");
        } else {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                // 如果文件夹是空的
                System.err.println("文件夹是空的");
            } else {
                try {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            // 如果是目录，递归
                            unZipRAR(f.getAbsolutePath());
                        } else {
                            String fname = f.getName();
                            int pos = fname.lastIndexOf(".");
                            String prefix = fname.substring(pos + 1);
                            if ("rar".equals(prefix)) {
                                String destPath = "D:\\b\\";
                                System.out.println("Unzip the file：" + f.getAbsolutePath());
                                RarDecompressionUtil.unrar(f.getAbsolutePath(), destPath, "1024");
                                count++;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
