import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Window extends JFrame {

    private KMeans km;
    private int k = 4;
    private BufferedImage bi = null;
    private int h, w;
    private JLabel afterImgLabel;
    private double ratio;
    private JLabel[] colorLabel;
    private JTextField inputK;
    private JPanel center;
    private JPanel afterPanel;

    private BufferedImage original; //Original picture after k means

    Window() {
        super("My Window");
        setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));

        getLabels();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1200, 450);
        setVisible(true);
    }

    private void getLabels() {
        ImageIcon imageIcon = new ImageIcon("pic.jpg"); // load the image to a imageIcon

        h = imageIcon.getIconHeight();
        w = imageIcon.getIconWidth();
        ratio = (double)(h)/w;

        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(400, (int)(400*ratio),  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg);  // transform it back

        JLabel imgLabel = new JLabel(imageIcon);
        //imgLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(imgLabel, BorderLayout.WEST);



        centerPanelSetUp();

    }

    private void centerPanelSetUp() {
        center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JButton run = new JButton("Run K Means");
        run.setPreferredSize(new Dimension(150,50));

        run.addActionListener((ActionEvent evt) -> {
            try {
                bi = ImageIO.read(new File("pic.jpg"));
            } catch(IOException e) {
                System.err.print("Could not read pic.jpg");
            }
            km = new KMeans(k, bi);
            bi = km.setUp();
            original = bi;

            afterPanelSetUp();
            add(afterPanel);
            repaint();

            ImageIcon imageIcon2 = new ImageIcon(bi);
            Image newimg2 = imageIcon2.getImage().getScaledInstance(400, (int)(400*ratio), Image.SCALE_SMOOTH);
            imageIcon2 = new ImageIcon(newimg2);

            afterImgLabel.setIcon(imageIcon2);

            Color[] colors = km.getColors();
            for(int a = 0; a < k; ++a) {
                colorLabel[a].setBackground(colors[a]);
            }
            afterPanel.repaint();
            repaint();
        });
        center.add(run);

        inputK = new JTextField();
        inputK.addActionListener((ActionEvent e) -> {
            String text = inputK.getText();
            try {
                k = Integer.parseInt(text);
                if((k < 2) || (k > 255)) {
                    System.err.println("K must be bigger than 1 and less than 256 setting k to 4");
                    k = 4;
                }
                System.out.println("Changing k to " + k);
            } catch(NumberFormatException nfe) {
                System.err.println("That is not an integer try again");
            } finally {
                inputK.setText(""); //Clear after we enter something.
            }
        });
        center.add(inputK);
        add(center);
    }

    void changePixels(int centroid) {
        // Change colour of all pixels associated with centroid.
        int[][] pixels= km.getPixels();
        double[][] centroids = km.getCentroids();
        Color oldColour = new Color((int)centroids[centroid][0], (int)centroids[centroid][1], (int)centroids[centroid][2]);

        Color cc = JColorChooser.showDialog(null, "Choose Colour", oldColour);

        for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
                if(pixels[x][y] == centroid) {
                    bi.setRGB(x, y, cc.getRGB());
                }
            }
        }
        afterImgLabel.setIcon(new ImageIcon((new ImageIcon(bi)).getImage().getScaledInstance(400, (int)(400*ratio), Image.SCALE_SMOOTH)));
        colorLabel[centroid].setBackground(cc);
        afterImgLabel.repaint();
    }

    private void afterPanelSetUp() {
        if(this.isAncestorOf(afterPanel)) {
            remove(afterPanel);
        }
        afterPanel = new JPanel(new FlowLayout());

        afterImgLabel = new JLabel();
        afterImgLabel.setPreferredSize(new Dimension(400, (int) (400 * ratio)));
        afterImgLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        afterPanel.add(afterImgLabel);

        colorLabel = new JLabel[k];
        for(int a = 0; a < k; ++a) {
            colorLabel[a] = null;
            System.out.println("a = " + a);
            colorLabel[a] = new JLabel();
            colorLabel[a].setBorder(BorderFactory.createLineBorder(Color.GREEN));
            colorLabel[a].setOpaque(true);
            colorLabel[a].setPreferredSize(new Dimension(50, 50));

            colorLabel[a].addMouseListener(new ColorClicker(a, this));
            afterPanel.add(colorLabel[a]);
        }

        afterPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        afterPanel.setPreferredSize(new Dimension(425, 400));
        afterPanel.repaint();
        this.repaint();
    }
}
