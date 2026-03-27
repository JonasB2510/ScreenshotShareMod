package de.jonasb2510.screenshotsharemod2.client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScreenshotUpload {
    public static String uploadToCatbox(Path filePath) throws IOException {
        File file = filePath.toFile();
        String fileName = file.getName();

        URL url = new URL("https://catbox.moe/user/api.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        String boundary = "----catbox-boundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
            // REQUIRED: reqtype parameter
            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n");
            dos.writeBytes("fileupload\r\n");

            // File field
            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + fileName + "\"\r\n");
            dos.writeBytes("Content-Type: image/png\r\n\r\n");

            // File data
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            }

            dos.writeBytes("\r\n--" + boundary + "--\r\n");
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            return new String(conn.getInputStream().readAllBytes()).trim();
        }
        throw new IOException("HTTP " + code);
    }
}
