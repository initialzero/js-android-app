/*
 * The MIT License
 *
 * Copyright (c) 2010 Xtreme Labs and Pivotal Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.apache.http.fake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8196];
        int len;
        try {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            in.close();
        }
    }

    /**
     * This method consumes an inputstream, returning its content then closing
     * it.
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream(inputStream.available());
            copy(inputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } finally {
            inputStream.close();
        }
    }

    public static <T> T[] reverse(T[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int destI = array.length - i - 1;
            T o = array[destI];
            array[destI] = array[i];
            array[i] = o;
        }
        return array;
    }

    public static File file(String... pathParts) {
        return file(new File("."), pathParts);
    }

    public static File file(File f, String... pathParts) {
        for (String pathPart : pathParts) {
            f = new File(f, pathPart);
        }
        return f;
    }

    public static URL url(String path) throws MalformedURLException {
        //Starts with double backslash, is likely a UNC path
        if(path.startsWith("\\\\")) {
            path = path.replace("\\", "/");
        }
        return new URL("file:/" + (path.startsWith("/") ? "/" + path : path));
    }

    public static List<Integer> intArrayToList(int[] ints) {
        List<Integer> youSuckJava = new ArrayList<Integer>();
        for (int attr1 : ints) {
            youSuckJava.add(attr1);
        }
        return youSuckJava;
    }

    public static int parseInt(String valueFor) {
        if (valueFor.startsWith("0x")) {
            return Integer.parseInt(valueFor.substring(2), 16);
        } else {
            return Integer.parseInt(valueFor, 10);
        }
    }
}