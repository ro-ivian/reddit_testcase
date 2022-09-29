/* Copyright (C) PublicRelay, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * The work belongs to the author's employer under work made for hire principles.
 */

package com.publicrelay.example.reddit_testcase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * @author Roman Bickersky
 * @since Sep 28, 2022
 */
public class SocialGistConnection {

    /**
     * our assigned Reddit username to fetch data.
     */
    @Value(value = "${reddit.stream.user}")
    private String redditStreamUser;

    /**
     * What password to use when authenticating to Reddit.
     */
    @Value(value = "${reddit.stream.password}")
    private String redditStreamPassword;

    /**
     * Reddit URI to connect to.
     */
    @Value(value = "${reddit.stream.url}")
    private String redditStreamUrl;

    private final Boolean gzip = true;

    /**
     * logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    // This parameter is used to keep the connected client to this stream alive by sending the carriage return character every 15 seconds.
    // This parameter is most useful when the volume on this stream is very low.
    // The default value is false means no carriage return characters will be sent.
    private static final String KEEP_ALIVE_STREAM_PROPERTY = "keepalivestream";
    private static final int SIXTY_SECONDS = 60000; // 60 seconds
    private static final int CONNECT_TIMEOUT_10_SECONDS = 10000; // 10 seconds
    private static final int FIVE_MINUTES = 5 * SIXTY_SECONDS;
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String BASIC_PROPERTY_VALUE = "Basic ";
    private static final String ACCEPT_ENCODING_PROPERTY = "Accept-Encoding";
    private static final String GZIP_PROPERTY_VALUE = "gzip";

    public void read() {
        final String url = redditStreamUrl;

        log.info("Attempting connecting to the url: " + url);

        if (!url.contains(KEEP_ALIVE_STREAM_PROPERTY)) {
            System.err.println("keepalivestream parameter is not set!!!");
        }

        HttpURLConnection conn = null;
        try {
            conn = getConnectionConnect(url);

            final int statusCode = conn.getResponseCode();

            if (statusCode >= 200 && statusCode <= 299) {

                // GOOD

                processStream(conn);
            } else {
                log.error("Disconnecting due to the statusCode {}", statusCode);
                log.error("StatusMessage: {}", conn.getResponseMessage());
            }

        } catch (final IOException e) {
            log.error("Exception invoking Reddit URL: " + url, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    private void processStream(final HttpURLConnection conn) throws IOException {

        log.info("RedditReaderService connected!");

        try (InputStream inputStream = conn.getInputStream()) {

            try (BufferedReader reader = createBufferedReader(inputStream)) {

                String line = reader.readLine();
                while (line != null) {

                    if (line != null && line.length() > 20) {
                        log.info("Reading -> [{}...]", line.substring(0, 20));
                    } else {
                        log.info("Reading -> [{}]", line);
                    }

                    line = reader.readLine();

                }

            }

        }

    }


    /**
     * @param inputStream
     * @return
     * @throws IOException
     */
    private BufferedReader createBufferedReader(InputStream inputStream) throws IOException {

        if (gzip) {
            return createGZBufferedReader(inputStream);
        } else {
            return createSimpleBufferedReader(inputStream);
        }
    }

    /**
     * PLEASE DO NOT CHANGE!
     * separate method in order to test.
     * @param inputStream
     * @return
     * @throws IOException
     */
    public BufferedReader createGZBufferedReader(final InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)));
    }

    public BufferedReader createSimpleBufferedReader(final InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * Set the username and password for Reddit connection.
     * @throws IOException
     */
    private HttpURLConnection getConnectionConnect(final String urlString) throws IOException {

        log.debug("User: " + redditStreamUser);
        final URL url = new URL(urlString);
        final String authToken = encodeBase64(redditStreamUser + ":" + redditStreamPassword);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // We set keepalivestream=true, so SocialGist is supposed to send us carriage every 60 seconds even if there is no data
        // those it is safe to set ReadTimeout. (If we missed 5 carriages means that something is wrong let's try to reconnect)
        connection.setReadTimeout(FIVE_MINUTES);
        connection.setConnectTimeout(CONNECT_TIMEOUT_10_SECONDS);
        connection.setRequestProperty(AUTHORIZATION_PROPERTY, BASIC_PROPERTY_VALUE + authToken);

        if (gzip) {
            log.info("Using GZIP Encoding to optimize the payload");
            connection.setRequestProperty(ACCEPT_ENCODING_PROPERTY, GZIP_PROPERTY_VALUE);
        }

        return connection;
    }

    /**
     * Returns the input string encode as 64 bit.
     *
     * @param input
     * @return
     */
    public static String encodeBase64(final String input) {
        final String result = Base64.encodeBase64String(input.getBytes());
        final String newResult = StringUtils.trimAllWhitespace(result);
        return newResult;
    }

}
