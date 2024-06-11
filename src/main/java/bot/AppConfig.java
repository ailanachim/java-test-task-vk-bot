package bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${app.base_url}")
    private String baseUrl;
    @Value("${app.group_id}")
    private int groupId;
    @Value("${app.access_token}")
    private String accessToken;

    @Bean
    public VkClient vkClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        return new VkClient(
                restClient,
                accessToken,
                groupId
        );
    }
}
