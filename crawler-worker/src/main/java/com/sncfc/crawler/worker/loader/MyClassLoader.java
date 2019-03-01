package com.sncfc.crawler.worker.loader;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by 10180210 on 2018/6/20.
 */
public class MyClassLoader extends ClassLoader {
    private String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            throw new ClassNotFoundException(name);
        }
    }

    private byte[] loadByte(String name) throws Exception {
        int index = name.lastIndexOf(".");
        String className = name.substring(index + 1);

        FileInputStream fis = new FileInputStream(classPath + File.separator + className + ".class");

        int length = fis.available();
        byte[] data = new byte[length];
        fis.read(data);
        fis.close();

        return data;
    }
}
