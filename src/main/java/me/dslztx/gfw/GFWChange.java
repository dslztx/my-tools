package me.dslztx.gfw;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.dslztx.assist.util.FileAssist;
import me.dslztx.assist.util.IOAssist;

public class GFWChange {

    private static final Logger logger = LoggerFactory.getLogger(GFWChange.class);

    private static String dir;

    public static void main(String[] args) throws InterruptedException, IOException {
        dir = args[0];

        List<String> servers = loadServers();

        logger.info("servers list: " + servers);

        int cur = 0;

        while (true) {
            if (!testConnect()) {

                int used = cur;
                while (true) {
                    cur = (++cur) % servers.size();
                    if (cur == used) {
                        logger.error("all servers are down");
                        System.exit(-1);
                    }

                    if (changeTo(servers.get(cur))) {
                        break;
                    }
                }
            }

            Thread.sleep(3 * 60 * 1000);
        }
    }

    private static List<String> loadServers() throws IOException {
        BufferedReader reader =
            IOAssist.bufferedReader(new File(dir + File.separator + "shadowsocksr" + File.separator + "servers.list"));

        List<String> server = new ArrayList<>();
        String s;
        while ((s = reader.readLine()) != null) {
            server.add(s);
        }
        return server;
    }

    private static boolean changeTo(String server) {
        FileAssist.copyClassPathFileToDst("changeAndRestart.sh", "/tmp/changeAndRestart.sh", false);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "/tmp/changeAndRestart.sh", dir, server);
            Process process = processBuilder.start();

            // 同步化
            process.waitFor();

            // 等待服务成功开启
            Thread.sleep(10000);

            if (test()) {
                logger.info("now server is: " + server + ", connect succeed");
                return true;
            } else {
                logger.info("now server is: " + server + ", connect fail");
                return false;
            }
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    public static boolean testConnect() {
        int tryCount = 3;
        while ((tryCount--) > 0) {
            if (test()) {
                return true;
            }
        }
        return false;
    }

    public static boolean test() {
        final SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new MyConnectionSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

        int timeout = 3;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
            .setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm)
            .setDefaultRequestConfig(config).build();
        try {
            InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", 8989);

            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            HttpHost target = new HttpHost("www.google.com", 443, "https");
            HttpGet request = new HttpGet("/");

            logger.info("Executing request " + request + " to " + target + " via SOCKS proxy " + socksaddr);

            CloseableHttpResponse response = httpclient.execute(target, request, context);

            logger.info("----------------------------------------");

            logger.info("" + response.getStatusLine());

            EntityUtils.consume(response.getEntity());

            response.close();

            return true;
        } catch (Throwable e) {
            logger.error("", e);
            return false;
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

    static class MyConnectionSocketFactory extends SSLConnectionSocketFactory {

        public MyConnectionSocketFactory() throws NoSuchAlgorithmException {
            super(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress)context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

    }
}
