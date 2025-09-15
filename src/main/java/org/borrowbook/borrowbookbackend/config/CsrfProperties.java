package org.borrowbook.borrowbookbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "csrf")
public class CsrfProperties {
    private Cookie cookie = new Cookie();
    private String parameterName = "_csrf";
    private String headerName = "X-XSRF-TOKEN";

    @Data
    public static class Cookie {
        private String name = "XSRF-TOKEN";
        private int maxAge = 3600;
        private boolean httpOnly = false;
        private String path;
        private String domain;
        private Boolean secure;
    }
}