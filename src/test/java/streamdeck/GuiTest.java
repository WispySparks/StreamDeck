package streamdeck;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class GuiTest {

    public static void main(String[] args) {
        List<JPanel> buttons = new ArrayList<>();
        JFrame window = new JFrame("StreamDeck");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(640, 360);
        window.setLocationRelativeTo(null);
        window.setLayout(null);
        window.getContentPane().setBackground(Color.BLACK);
        for (int i = 0; i < 15; i++) { // Buttons
            JPanel rectangle = new JPanel();
            int width = 75;
            int height = 75;
            int x = i % 5 * (width + 25) + 90;
            int y = i / 5 * (height + 25) + 20;
            rectangle.setBounds(x, y, width, height);
            rectangle.setBackground(Color.DARK_GRAY);
            buttons.add(rectangle);
            window.add(rectangle);
        }
        JSlider slider = new JSlider();
        slider.setBounds(0, 10, 75, 25);
        slider.setBackground(Color.BLACK);
        window.add(slider);
        window.setVisible(true);
        StreamDeck streamDeck = StreamDeck.getStreamDeck();
        slider.addChangeListener((e) -> { // Brightness
            streamDeck.setBrightness(((JSlider) e.getSource()).getValue());
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> streamDeck.close()));
        while (true) {
            streamDeck.updateButtonStates();
            boolean[] result = streamDeck.getButtonStates();
            for (int i = 0; i < result.length; i++) {
                if (result[i]) {
                    buttons.get(i).setBackground(Color.LIGHT_GRAY);
                }
                else {
                    buttons.get(i).setBackground(Color.DARK_GRAY);
                }
            }
        }
    }

}
