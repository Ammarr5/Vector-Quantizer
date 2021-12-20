import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VQGUI extends JFrame {
    private JPanel panel;
    private JButton chooseImageButton;
    private JButton chooseFileToDecompressButton;
    private JButton compressButton;
    private JButton decompressButton;
    private JLabel beforeImageLabel;
    private JLabel afterImageLabel;
    private JLabel errorIndicator;
    private JSpinner codeBookSize;
    private JSpinner vectorHeight;
    private JSpinner vectorWidth;
    private JPanel beforeImagePanel;

    File beforeImage;
    File afterImage;

    BufferedImage beforeImageFile;
    BufferedImage afterImageFile;

    public VQGUI() {
        super("Vector Quantizer");
        setPreferredSize(new Dimension(600, 400));
        chooseImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int option = fileChooser.showOpenDialog((JFrame) panel.getParent().getParent().getParent());
                if(option == JFileChooser.APPROVE_OPTION){
                    beforeImage = fileChooser.getSelectedFile();
                    if(beforeImage == null) {
                        errorIndicator.setText("An error occured");
                        return;
                    }
                    try {
                        beforeImageFile = ImageIO.read(beforeImage.getAbsoluteFile());
                        beforeImageLabel.setIcon(new ImageIcon(beforeImageFile));
                        beforeImageLabel.setHorizontalAlignment(JLabel.CENTER);

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        chooseFileToDecompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int option = fileChooser.showOpenDialog((JFrame) panel.getParent().getParent().getParent());
                if(option == JFileChooser.APPROVE_OPTION){
                    afterImage = fileChooser.getSelectedFile();
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (beforeImageFile == null || (int)codeBookSize.getValue() == 0 || (int)vectorWidth.getValue() == 0 || (int)vectorHeight.getValue() == 0) {
                    errorIndicator.setText("An error occured");
                    return;
                }
                try {
                    Quantizer.compress(beforeImage.getAbsolutePath(), (int)vectorWidth.getValue(), (int)vectorHeight.getValue(), (int)codeBookSize.getValue());
                    String path1 = beforeImage.getAbsolutePath().substring(0, beforeImage.getAbsolutePath().lastIndexOf('.')) + ".quantized";
                    afterImage = new File(path1);
                    Quantizer.decompress(afterImage.getAbsolutePath());
                    String path = afterImage.getAbsoluteFile().toString().substring(0, afterImage.getAbsoluteFile().toString().lastIndexOf('.')) + "-Decompressed.jpg";
                    afterImageFile = ImageIO.read(new File(path));
                    afterImageLabel.setIcon(new ImageIcon(afterImageFile));
                    afterImageLabel.setHorizontalAlignment(JLabel.CENTER);
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (afterImage == null) {
                    errorIndicator.setText("An error occured");
                    return;
                }
                try {
                    Quantizer.decompress(afterImage.getAbsolutePath());
                    String path = afterImage.getAbsoluteFile().toString().substring(0, afterImage.getAbsoluteFile().toString().lastIndexOf('.')) + "-Decompressed.jpg";
                    afterImageFile = ImageIO.read(new File(path));
                    afterImageLabel.setIcon(new ImageIcon(afterImageFile));
                    afterImageLabel.setHorizontalAlignment(JLabel.CENTER);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        VQGUI gui = new VQGUI();
    }
}
