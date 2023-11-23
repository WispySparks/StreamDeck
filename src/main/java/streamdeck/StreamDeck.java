package streamdeck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class StreamDeck {

    protected static final int VENDOR_ID = 4057;
    protected static final int PRODUCT_ID = 109;
    protected static final byte BRIGHTNESS_REPORT_ID = 0x03;
    protected static final byte[] INTERRUPT_ID = {1, 0, 15, 0};
    protected static final int NUM_BUTTONS = 15;
    private final HidDevice hidDevice;
    private boolean[] buttonStates;

    private StreamDeck(HidDevice hidDevice) {
        this.hidDevice = hidDevice;
        hidDevice.open();
    }

    public boolean[] getButtonStates() {
        return buttonStates;
    }

    /**
     * Updates the class's button states, blocking call until timeoutMS
     * @param timeoutMS 
     * @return new StreamDeck button states
     */
    public boolean[] updateButtonStates(int timeoutMS) {
        byte[] data = new byte[INTERRUPT_ID.length + NUM_BUTTONS];
        int bytesRead = hidDevice.read(data, timeoutMS);
        if (bytesRead != data.length) return null;
        for (int i = 0; i < INTERRUPT_ID.length; i++) {
            if (data[i] != INTERRUPT_ID[i]) return null;
        }
        boolean[] result = new boolean[NUM_BUTTONS];
        for (int i = 0; i < result.length; i++) {
            result[i] = byteToBool(data[i + INTERRUPT_ID.length]);
        }
        buttonStates = result;
        return result;
    }

    /**
     * Updates the class's button states, Forever blocking call
     * @return new StreamDeck button states
     */
    public boolean[] updateButtonStates() {
        return updateButtonStates(-1);
    }

    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness is out of bounds! Must be between 0-100.");
        }
        hidDevice.sendFeatureReport(getBrightnessBuffer((byte) brightness), BRIGHTNESS_REPORT_ID);
    }

    public void close() {
        hidDevice.close();
    }

    public void printButtons() {
        System.out.println(Arrays.toString(buttonStates));
    }

    private static byte[] getBrightnessBuffer(byte brightness) {
        byte[] buffer = new byte[]{
            0x08, brightness, 0x23, (byte) 0xB8, 0x01, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xA5, 0x49, (byte) 0xCD, 0x02, (byte) 0xFE, 0x7F, 0x00, 0x00,
        };
        return buffer;
    }

    private static boolean byteToBool(byte i) {
        return i == 1 ? true : false;
    }
    
    public static List<StreamDeck> getStreamDecks() {
        List<StreamDeck> streamDecks = new ArrayList<>();
        HidServices hidServices = HidManager.getHidServices();
        hidServices.start();
        for (HidDevice device : hidServices.getAttachedHidDevices()) {
            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) {
                streamDecks.add(new StreamDeck(device));
            }
        }
        return streamDecks;
    }

    public static StreamDeck getStreamDeck() {
        List<StreamDeck> decks = getStreamDecks();
        if (decks.size() > 0) {
            return decks.get(0);
        }
        return null;
    }

}
