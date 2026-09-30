package dev.chirag45.breeze_core.outbox;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.utils.HTTPClient;
import com.clerk.backend_api.utils.RetryConfig;
import com.clerk.backend_api.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Configuration
public class ClerkSdkConfig {

    @Bean
    Clerk clerkSdk(@Value("${breeze.clerk.secret-key:}") String secretKey) {
        HTTPClient boundedTransport = new HTTPClient() {
            @Override
            public HttpResponse<InputStream> send(HttpRequest request)
                    throws IOException, InterruptedException, URISyntaxException {
                return HTTPClient.super.send(Utils.copy(request)
                        .timeout(Duration.ofSeconds(15))
                        .build());
            }
        };

        return Clerk.builder()
                .bearerAuth(secretKey)
                .retryConfig(RetryConfig.builder().noRetries().build())
                .client(boundedTransport)
                .build();
    }
}
