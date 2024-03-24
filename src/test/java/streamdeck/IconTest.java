package streamdeck;

import java.io.File;

public class IconTest {
    
    public static void main(String[] args) {
        StreamDeck streamDeck = StreamDeck.getStreamDeck();
        streamDeck.setBrightness(100);
        // for (int i = 0; i < StreamDeck.NUM_BUTTONS; i++) {
        //     streamDeck.clearIcon(i);
        // }
        streamDeck.setIcon(0, new File("C:\\Users\\wispy\\Downloads\\out.jpg"));
        streamDeck.close();
    }

}
