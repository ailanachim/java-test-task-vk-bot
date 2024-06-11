package bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.IOException;

public class VkClient {

    private final static String API_VERSION = "5.199";

    private final String accessToken;
    private final int groupId;

    private String key;
    private String ts;

    private final RestClient client;
    private RestClient updatesClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public VkClient(RestClient client, String accessToken, int groupId) {
        this.client = client;
        this.accessToken = accessToken;
        this.groupId = groupId;
    }

    /**
     * Sends message to VK user
     *
     * @param message message text. The maximum number of characters is 4096.
     * @param userId the ID of the user to whom the message is sent.
     * @param randomId A unique (in relation to the application ID and sender ID) identifier
     *                 designed to prevent repeated sending of the same message.
     *                 Saved with the message and available in the message history.
     *                 Possible values:
     *                      0 — uniqueness check is not needed.
     *                      Any other number within int32 - a check for uniqueness is needed.
     *                 The random_id sent in the request is used to check the uniqueness of messages
     *                 in a given conversation over the last hour (but not more than the last 100 messages).
     * @return message ID
     */
    public int sendMessage(String message, int userId, int randomId) throws IOException {
        String method = "messages.send";
        ResponseEntity<String> response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("method/" + method)
                        .queryParam("access_token", accessToken)
                        .queryParam("v", API_VERSION)
                        .queryParam("message", message)
                        .queryParam("user_id", userId)
                        .queryParam("random_id", randomId)
                        .build()
                )
                .retrieve()
                .toEntity(String.class);

        JsonNode json = mapper.readTree(response.getBody());
        if (json.get("error") != null) {
            throw new IOException(json.get("error").asText());
        }
        return json.get("response").asInt();
    }

    /**
     * @return json array of tracked events
     * More about format: <a href="https://dev.vk.com/ru/api/community-events/json-schema"></a>
     */
    public JsonNode getUpdates() throws IOException {
        if (updatesClient == null) {
            getLongPollServer();
        }

        ResponseEntity<String> response = updatesClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("act", "a_check")
                        .queryParam("key", key)
                        .queryParam("ts", ts)
                        .build()
                )
                .retrieve()
                .toEntity(String.class);

        JsonNode json = mapper.readTree(response.getBody());
        if (json.get("error") != null) {
            throw new IOException(json.get("error").asText());
        }
        if (json.get("failed") != null) {
            getLongPollServer();
            return getUpdates();
        }
        ts = json.get("ts").asText();
        return json.get("updates");
    }

    private void getLongPollServer() throws IOException {
        String method = "groups.getLongPollServer";
        ResponseEntity<String> response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("method/" + method)
                        .queryParam("access_token", accessToken)
                        .queryParam("v", API_VERSION)
                        .queryParam("group_id", groupId)
                        .build()
                )
                .retrieve()
                .toEntity(String.class);

        JsonNode json = mapper.readTree(response.getBody());
        if (json.get("error") != null) {
            throw new IOException(json.get("error").asText());
        }
        JsonNode responseData = json.get("response");
        String server = responseData.get("server").asText();
        key = responseData.get("key").asText();
        ts = responseData.get("ts").asText();

        updatesClient = RestClient.builder()
                .baseUrl(server)
                .build();
    }
}
