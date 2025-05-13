package com.reider745.innercoretoolchain.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtil {
    public static byte[] readHttp(String url) throws IOException {
        final URLConnection connection = new URL(url).openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setReadTimeout(3000);
        connection.connect();

        final InputStream stream = connection.getInputStream();
        final byte[] bytes = stream.readAllBytes();
        stream.close();
        return bytes;
    }

    public static String readStringHttp(String url) throws IOException {
        return new String(readHttp(url));
    }
}
