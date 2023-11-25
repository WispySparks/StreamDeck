package streamdeck;

public class IconTest {
    
    public static void main(String[] args) {
        StreamDeck streamDeck = StreamDeck.getStreamDeck();
        streamDeck.setBrightness(100);
        for (int i = 0; i < 15; i++) {
            streamDeck.clearIcon(i);
        }
        streamDeck.close();
    }

}
