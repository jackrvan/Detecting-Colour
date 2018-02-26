import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

class KMeans {
    private int k;
    private BufferedImage img;
    private Color[] domColors;
    private int[][] pixels;
    private double[][] centroids;

    KMeans(int k, BufferedImage bi) {
        this.k = k;
        img = bi;
        domColors = new Color[k];
    }

    BufferedImage setUp() {
        int w = img.getWidth();
        int h = img.getHeight();
        pixels = new int[w][h];

        BufferedImage newImage = new BufferedImage(w, h, img.getType()); //Make new image same size and type as the original
        int[] rgbVals = new int[w*h];
        int knt = 0;

        for(int a = 0; a < w; ++a) {
            for(int b = 0; b < h; ++b) {
                //Store all pixel rgb values in rgbVals
                int val = img.getRGB(a, b);
                //System.out.println("" + a + ", " + b + " = " + val);
                rgbVals[knt++] = val;
            }
        }

        algorithm(rgbVals);

        knt = 0;
        for(int a = 0; a < w; ++a) {
            for(int b = 0; b < h; ++b) {
                //Set each pixel value in newImage
                newImage.setRGB(a, b, rgbVals[knt++]);
            }
        }

        return newImage;
    }

    private void algorithm(int[] rgbs) {
        centroids = new double[k][3]; //3 for each of R G B
        long[] knt = new long[k];

        int w = img.getWidth();
        int h = img.getHeight();

        for(int a = 0; a < k; ++a) {
            //Start with the centroids as random values for R G B
            Random rand = new Random();
            centroids[a][0] = rand.nextDouble() * 255; //Random double between 0.0-255.0
            centroids[a][1] = rand.nextDouble() * 255;
            centroids[a][2] = rand.nextDouble() * 255;
            knt[a] = 0;
        }

        int len = rgbs.length;
        int[] reds = new int[len];
        int[] greens = new int[len];
        int[] blues = new int[len];

        for(int a = 0; a < len; ++a) {
            Color c = new Color(rgbs[a]);
            //Extract amount of each R G B from each pixel
            reds[a] = c.getRed();
            greens[a] = c.getGreen();
            blues[a] = c.getBlue();
        }

        int[] curr = new int[len];
        boolean changed = true;
        int currIter = 1;
        int maxIter = 100;

        while(changed && (currIter <= maxIter)) {
            //Main loop of algorithm
            //We keep looping until all k centroids do not move
            System.out.println("Iteration " + currIter + "/" + maxIter);
            changed = false;
            int[] temp = new int[len];
            int x = 0;
            int y = -1;
            for(int a = 0; a < len; ++a) {
                ++y;
                if(y >= h) {
                    ++x;
                    y = 0;
                }
                if(y >= img.getHeight()) break;

                double closest = Double.MAX_VALUE;

                for(int b = 0; b < k; ++b) {
                    //loop through centroids to find closest one to current pixel
                    double dist = calc_distance(reds, greens, blues, centroids, a, b);
                    if(dist < closest) {
                        temp[a] = b; //Assign pixel a to centroid b
                        closest = dist;
                        pixels[x][y] = b;
                    }
                }
            }

            for(int a = 0; a < len; ++a) {
                if(curr[a] != temp[a]) {
                    changed = true; //We changed at least 1 pixel median
                    curr[a] = temp[a];
                }
            }

            if(changed) {
                for(int a = 0; a < k; ++a) {
                    knt[a] = 0;
                }
                for(int a = 0; a < len; ++a) {
                    int currCentroid = temp[a]; //Get centroid that pixel a is assigned to
                    if((++knt[currCentroid]) == 1) {
                        //If this is the first pixel assigned to this centroid
                        centroids[currCentroid][0] = reds[a];
                        centroids[currCentroid][1] = greens[a];
                        centroids[currCentroid][2] = blues[a];
                    } else {
                        double prevKnt = (double)knt[currCentroid] - 1.0;
                        centroids[currCentroid][0] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][0]+(reds[a]/prevKnt)); //New centroid red value (average)
                        centroids[currCentroid][1] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][1]+(blues[a]/prevKnt));
                        centroids[currCentroid][2] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][2]+(greens[a]/prevKnt));
                    }
                }
            }
            ++currIter;
        }

        for(int a = 0; a < len; ++a) {
            rgbs[a]=(int)centroids[curr[a]][0] << 16 |
                    (int)centroids[curr[a]][1] << 8 | (int)centroids[curr[a]][2];
        }
        for(int a = 0; a < k; ++a) {
            domColors[a] = new Color((int)centroids[a][0], (int)centroids[a][1], (int)centroids[a][2]);
        }

        BufferedImage bi2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        for(int a = 0; a < w; ++a) {
            for(int b = 0; b < img.getHeight(); ++b) {
                Color c = new Color((int)centroids[pixels[a][b]][0], (int)centroids[pixels[a][b]][1], (int)centroids[pixels[a][b]][2]);
                bi2.setRGB(a, b, c.getRGB());
            }
        }
    }

    private double calc_distance(int[] reds, int[] greens, int[] blues, double[][] centroids, int a, int b) {
        return (Math.pow((reds[a] - centroids[b][0]), 2) + Math.pow((blues[a] - centroids[b][1]), 2) +
                Math.pow((greens[a] - centroids[b][2]), 2));
    }
    Color[] getColors() {
        return domColors;
    }

    int[][] getPixels() {
        return pixels;
    }

    double[][] getCentroids() {
        return centroids;
    }
}
