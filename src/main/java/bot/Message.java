package bot;

public class Message {

    private final String text;
    private final int userId;

    public Message(String text, int userId) {
        this.text = text;
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public int getUserId() {
        return userId;
    }
}
