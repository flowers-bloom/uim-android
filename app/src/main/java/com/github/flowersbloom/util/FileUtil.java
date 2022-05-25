package com.github.flowersbloom.util;

import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void writeBytes(byte[] bytes) {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codec.h264", true);
            writer.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
