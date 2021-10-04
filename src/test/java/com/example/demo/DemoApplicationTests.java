package com.example.demo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DemoApplicationTests {
    @RegisterExtension
    WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().enableBrowserProxying(true).port(10001))
            .build();

    @Test
    void contextLoads() {
        wm1.stubFor(
                get(urlMatching(".*"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody("foo")
                        )
        );

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", Integer.toString(wm1.getRuntimeInfo().getHttpPort()));

        WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.create().proxyWithSystemProperties())).build();
        String result = client.get().uri("http://example.com").retrieve().bodyToMono(String.class).block();

        assertEquals("foo", result);
    }

}
