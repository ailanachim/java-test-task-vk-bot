package bot;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Bot {

    private final VkClient client;
    @Value("${app.interval}")
    private int interval;

    public Bot(VkClient client) {
        this.client = client;
    }

    public void run() throws IOException, InterruptedException {
        while (true) {
            JsonNode updates = client.getUpdates();
            for (JsonNode update : updates) {
                if (update.get("type").asText().equals("message_new")) {
                    var jsonMessage = update.get("object").get("message");
                    int userId = jsonMessage.get("from_id").asInt();
                    String text = jsonMessage.get("text").asText();
                    String reply = "Вы сказали: " + text;
                    client.sendMessage(reply, userId, (int) (Math.random() * Integer.MAX_VALUE));
                }
            }
            Thread.sleep(interval * 1000L);
        }
    }
}
