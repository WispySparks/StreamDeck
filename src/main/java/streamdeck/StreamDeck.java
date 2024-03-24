package streamdeck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class StreamDeck {

    public static final int VENDOR_ID = 4057;
    public static final int PRODUCT_ID = 109;
    public static final int NUM_BUTTONS = 15;
    private static byte[] blankIconData;
    private final HidDevice hidDevice;
    private final boolean[] buttonStates = new boolean[NUM_BUTTONS];

    static {
        try {
            InputStream stream = StreamDeck.class.getResourceAsStream("/blank.jpg");
            blankIconData = stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    public void updateButtonStates(int timeoutMS) {
        final byte[] prefix = {1, 0, 15, 0};
        byte[] data = new byte[prefix.length + NUM_BUTTONS];
        int bytesRead = hidDevice.read(data, timeoutMS);
        if (bytesRead != data.length) return;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return;
        }
        for (int i = 0; i < buttonStates.length; i++) {
            buttonStates[i] = byteToBool(data[i + prefix.length]);
        }
    }

    /**
     * Updates the class's button states, forever blocking call
     * @return new StreamDeck button states
     */
    public void updateButtonStates() {
        updateButtonStates(-1);
    }

    public void setBrightness(int brightness) {
        final byte BRIGHTNESS_REPORT_ID = 0x03;
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness is out of bounds! Must be between 0-100.");
        }
        hidDevice.sendFeatureReport(getBrightnessBuffer((byte) brightness), BRIGHTNESS_REPORT_ID);
    }

    public void setIcon(int button, File f) {
        final byte ICON_REPORT_ID = 0x02;
        byte[] data;
        try {
            InputStream stream = new FileInputStream(f);
            // data = stream.readNBytes(1016);
            data = stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace(); return;
        }
        byte[] prefix = {0x07, (byte) button, 0x00, (byte) 0xF8, 0x03, 0x00, 0x00};
        byte[] packet = new byte[prefix.length + data.length];
        System.arraycopy(prefix, 0, packet, 0, prefix.length);
        System.arraycopy(data, 0, packet, prefix.length, data.length);
        int i = hidDevice.write(packet, packet.length, ICON_REPORT_ID);
        System.out.println(hidDevice.getLastErrorMessage());
        System.out.println(i);
    }

    public void clearIcon(int button) {
        final byte ICON_REPORT_ID = 0x02;
        if (button < 0 || button > 14) {
            throw new IllegalArgumentException("Button index out of bounds! Must be between 0-14.");
        }
        // 1024 bytes max?
        //                                    0x00, 0xF8?
        byte[] prefix = {0x07, (byte) button, 0x01, 0x01, 0x03, 0x00, 0x00};
        byte[] packet = new byte[prefix.length + blankIconData.length];
        System.arraycopy(prefix, 0, packet, 0, prefix.length);
        System.arraycopy(blankIconData, 0, packet, prefix.length, blankIconData.length);
        hidDevice.write(packet, packet.length, ICON_REPORT_ID);
    }

    public void close() {
        hidDevice.close();
    }

    public void printButtonStates() {
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
