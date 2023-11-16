package streamdeck;

import java.util.ArrayList;
import java.util.List;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class Main {

    static final byte BRIGHTNESS_REPORT_ID = 0x03;

    public static void main(String[] args) {
        List<HidDevice> streamDecks = new ArrayList<>();
        HidServices hidServices = HidManager.getHidServices();
        hidServices.start();
        for (HidDevice device : hidServices.getAttachedHidDevices()) {
            if (device.getVendorId() == 4057 && device.getProductId() == 109) {
                streamDecks.add(device);
            }
        }
        HidDevice streamDeck = streamDecks.get(0);
        streamDeck.open();
        int r = streamDeck.sendFeatureReport(getBrightnessCommand((byte) 100), BRIGHTNESS_REPORT_ID);
        System.out.println(r);
        streamDeck.close();
    }

    public static byte[] getBrightnessCommand(byte brightness) {
        byte[] buffer = new byte[]{
            0x08, brightness, 0x23, (byte) 0xB8, 0x01, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xA5, 0x49, (byte) 0xCD, 0x02, (byte) 0xFE, 0x7F, 0x00, 0x00,
        };
        return buffer;
    }

}
