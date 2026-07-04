package com.growtharchive.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.growtharchive.config.properties.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class KakaoOAuthClientTest {

    @Test
    void authorizationUrlAlwaysUsesKakaoAuthorizeEndpoint() {
        AppProperties properties = new AppProperties();
        properties.getKakao().setClientId("rest-api-key");
        properties.getKakao().setRedirectUri("http://localhost:8080/api/v1/auth/kakao/callback");
        properties.getKakao().setAuthorizationUri("https://kauth.kakao.com/oauth/authorize");

        KakaoOAuthClient client = new KakaoOAuthClient(properties, new ObjectMapper(), RestClient.builder().build());

        assertThat(client.authorizationUrl("state"))
            .startsWith("https://kauth.kakao.com/oauth/authorize")
            .contains("response_type=code")
            .contains("client_id=rest-api-key")
            .contains("state=state");
    }

    @Test
    void fetchProfileExchangesCodeAndReadsKakaoUserInfo() {
        AppProperties properties = new AppProperties();
        properties.getKakao().setClientId("rest-api-key");
        properties.getKakao().setClientSecret("client-secret");
        properties.getKakao().setRedirectUri("http://localhost:8080/api/v1/auth/kakao/callback");
        properties.getKakao().setAuthorizationUri("https://kauth.kakao.com/oauth/authorize");
        properties.getKakao().setTokenUri("https://kauth.kakao.com/oauth/token");
        properties.getKakao().setUserInfoUri("https://kapi.kakao.com/v2/user/me");
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoOAuthClient client = new KakaoOAuthClient(properties, new ObjectMapper(), builder.build());

        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"access_token\":\"access-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer access-token"))
            .andRespond(withSuccess("""
                {
                  "id": 1234567890,
                  "kakao_account": {
                    "email": "member@example.com",
                    "profile": {
                      "nickname": "홍길동",
                      "profile_image_url": "https://profile.example/profile.png"
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        KakaoUserProfile profile = client.fetchProfile("authorization-code");

        assertThat(profile.providerUserId()).isEqualTo("1234567890");
        assertThat(profile.email()).isEqualTo("member@example.com");
        assertThat(profile.nickname()).isEqualTo("홍길동");
        assertThat(profile.profileImageUrl()).isEqualTo("https://profile.example/profile.png");
        server.verify();
    }

    @Test
    void fetchProfileTreatsMissingOptionalKakaoFieldsAsNull() {
        AppProperties properties = new AppProperties();
        properties.getKakao().setClientId("rest-api-key");
        properties.getKakao().setClientSecret("client-secret");
        properties.getKakao().setRedirectUri("http://localhost:8080/api/v1/auth/kakao/callback");
        properties.getKakao().setAuthorizationUri("https://kauth.kakao.com/oauth/authorize");
        properties.getKakao().setTokenUri("https://kauth.kakao.com/oauth/token");
        properties.getKakao().setUserInfoUri("https://kapi.kakao.com/v2/user/me");
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoOAuthClient client = new KakaoOAuthClient(properties, new ObjectMapper(), builder.build());

        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"access_token\":\"access-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer access-token"))
            .andRespond(withSuccess("{\"id\": 1234567890}", MediaType.APPLICATION_JSON));

        KakaoUserProfile profile = client.fetchProfile("authorization-code");

        assertThat(profile.providerUserId()).isEqualTo("1234567890");
        assertThat(profile.email()).isNull();
        assertThat(profile.nickname()).isNull();
        assertThat(profile.profileImageUrl()).isNull();
        server.verify();
    }
}
