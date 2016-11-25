package hk.hku.cs.videoalbum.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteServerConnect {

    public String getUrlResponse(String url) {
        HttpURLConnection httpURLConnection = null;
        final int HTML_BUFFER_SIZE = 2*1024*1024;
        char htmlBuffer[] = new char[HTML_BUFFER_SIZE];

        try {
            URL url_page = new URL(url);
            httpURLConnection = (HttpURLConnection) url_page.openConnection();
            httpURLConnection.setInstanceFollowRedirects(true);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String response = readerResponse(bufferedReader, htmlBuffer, HTML_BUFFER_SIZE);
            bufferedReader.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Fail to login";
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private String readerResponse(BufferedReader reader, char [] htmlBuffer, int bufSz) throws java.io.IOException
    {
        htmlBuffer[0] = '\0';
        int offset = 0;
        do {
            int cnt = reader.read(htmlBuffer, offset, bufSz - offset);
            if (cnt > 0) {
                offset += cnt;
            } else {
                break;
            }
        } while (true);
        return new String(htmlBuffer);
    }
}
