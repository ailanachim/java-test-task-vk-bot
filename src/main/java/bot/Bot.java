package bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Bot {

    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final VkClient client;
    @Value("${app.interval}")
    private int interval;

    public Bot(VkClient client) {
        this.client = client;
    }

    public void run() throws IOException, InterruptedException {
        while (true) {
            var messages = client.getMessages();
            for (var message : messages) {
                String reply = "Вы сказали: " + message.getText();
                try {
                    client.sendMessage(reply, message.getUserId(), (int) (Math.random() * Integer.MAX_VALUE));
                } catch (IOException e) {
                    logger.warn(e.getMessage());
                }
            }
            Thread.sleep(interval * 1000L);
        }
    }
}
