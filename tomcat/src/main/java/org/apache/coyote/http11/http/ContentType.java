package org.apache.coyote.http11.http;

import java.util.Arrays;

public enum ContentType {
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    JAVASCRIPT("js", "text/javascript"),
    FAVICON("ico", "image/vnd.microsoft.icon")
    ;

    private final String extension;
    private final String mimeType;

    ContentType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static ContentType from(String url) {
        if (!url.contains(".")) {
            return HTML;
        }
        return Arrays.stream(ContentType.values())
                .filter(it -> url.endsWith(it.getExtension()))
                .findFirst()
                .orElseThrow();
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }
}
