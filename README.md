# TestCase to demonstrate SocialGist connection drops

### System Requirements
Java 17

### Configure username and password
Open src/main/resources/application.properties

Update: reddit.stream.user and reddit.stream.password configurations

### Running Instructions
```console
mvn clean test
```

### Check Console Logs to see the reproduced behavior 
As you can see the connection gets dropped after 1 minute (15:02:10) Connected - (15:03:10) - Error :

```
Running com.publicrelay.example.reddit_testcase.SocialGistConnection_TestCase
2022-09-28 14:59:32,941 [INFO] Attempting connecting to the url: https://publicrelay.socialgist.com/stream/redditpremium_dev/subscription/main/part/1/data.json?keepalivestream=true
2022-09-28 15:02:10,334 [INFO] RedditReaderService connected!
2022-09-28 15:03:10,315 [ERROR] Exception invoking Reddit URL: https://publicrelay.socialgist.com/stream/redditpremium_dev/subscription/main/part/1/data.json?keepalivestream=true
java.io.EOFException: Unexpected end of ZLIB input stream
    at java.util.zip.InflaterInputStream.fill(InflaterInputStream.java:244) ~[?:?]
    at java.util.zip.InflaterInputStream.read(InflaterInputStream.java:158) ~[?:?]
    at java.util.zip.GZIPInputStream.read(GZIPInputStream.java:117) ~[?:?]
    at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:270) ~[?:?]
    at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:313) ~[?:?]
```
