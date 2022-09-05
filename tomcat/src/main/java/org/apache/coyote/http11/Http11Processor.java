package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import nextstep.jwp.application.LoginService;
import nextstep.jwp.exception.UncheckedServletException;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
             final var outputStream = connection.getOutputStream()) {

            final StartLine startLine = new StartLine(bufferedReader.readLine());
            final String responseBody = getResponseBody(startLine.getRequestUri());
            final String contentType = getContentType(startLine.getRequestUri());
            final Map<String, String> queryParams = startLine.getQueryParams();

            if (queryParams.containsKey("account")) {
                LoginService.checkAccount(queryParams);
            }


            final var response = String.join("\r\n",
                    "HTTP/1.1 200 OK ",
                    "Content-Type: " + contentType + ";charset=utf-8 ",
                    "Content-Length: " + responseBody.getBytes().length + " ",
                    "",
                    responseBody);

            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getResponseBody(String requestUri) throws IOException {
        if (requestUri.equals("/")) {
            requestUri = "/index.html";
        }
        if (!requestUri.contains(".")) {
            requestUri += ".html";
        }
        URL resource = getClass().getClassLoader().getResource("static" + requestUri);
        Path filePath = new File(Objects.requireNonNull(resource).getFile()).toPath();
        return new String(Files.readAllBytes(filePath));
    }

    private String getContentType(String requestUri) {
        if (requestUri.endsWith("css")) {
            return "text/css";
        }
        if (requestUri.endsWith("js")) {
            return "text/javascript";
        }
        return "text/html";
    }
}
