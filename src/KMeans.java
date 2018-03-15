import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

class KMeans {
    private int k;
    private BufferedImage img; //Our original image
    private Color[] domColors; //The k colors we end up with at the end
    private int[][] pixels; //Array of pixel values in the image
    private double[][] centroids; //k centroids centroids[k][0] = red value of centroid 0
    private long[] knt;

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
                int val = img.getRGB(a, b); //Rgb val of pixel a b.
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
        knt = new long[k];

        int w = img.getWidth();
        int h = img.getHeight();

        //startCentroidsRandom();
        startCentroidsDivided();

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
            boolean[] centroidUsed = new boolean[k]; //This makes sure at least 1 centroid is assigned to each centroid
            for(int i = 0; i < k; ++i) {
                centroidUsed[i] = false;
            }

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
                centroidUsed[temp[a]] = true; //Pixel temp[a] is assigned to centroid so that centroid was used
            }

            for(int i = 0; i < k; ++i) {
                if(!centroidUsed[i]) {
                    System.out.println("We are changing centroid " + i);
                    //If we are not using this centroid we can change the colour of it
                    Random rand = new Random();
                    centroids[i][0] = rand.nextDouble() * 255;
                    centroids[i][1] = rand.nextDouble() * 255;
                    centroids[i][2] = rand.nextDouble() * 255;
                    changed = true; //We changed something
                }
            }

            for(int a = 0; a < len; ++a) {
                if(curr[a] != temp[a]) {
                    //System.out.println("IF " + curr[a] + " and temp = " + temp[a]);
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
                        double prevKnt = (double)knt[currCentroid] - 1.0; //How many pixels are assigned to currCentroid
                        centroids[currCentroid][0] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][0]+(reds[a]/prevKnt)); //New centroid red value (average)
                        centroids[currCentroid][1] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][1]+(greens[a]/prevKnt));
                        centroids[currCentroid][2] = (prevKnt/(prevKnt+1.0))*(centroids[currCentroid][2]+(blues[a]/prevKnt));
                    }
                }
            }
            ++currIter;
        }

        //Set each pixel equal to the centroid colour
        for(int a = 0; a < len; ++a) {
            rgbs[a]=(int)centroids[curr[a]][0] << 16 |
                    (int)centroids[curr[a]][1] << 8 | (int)centroids[curr[a]][2];
        }
        for(int a = 0; a < k; ++a) {
            domColors[a] = new Color((int)centroids[a][0], (int)centroids[a][1], (int)centroids[a][2]);
        }

        BufferedImage bi2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        for(int a = 0; a < w; ++	a) {
            for(int b = 0; b < img.getHeight(); ++b) {
                Color c = new Color((int)centroids[pixels[a][b]][0], (int)centroids[pixels[a][b]][1], (int)centroids[pixels[a][b]][2]);
                bi2.setRGB(a, b, c.getRGB());
            }
        }
    }

    //Following 2 methods are 2 ways to start the centroids
    private void startCentroidsRandom() {
        //Method to fill centroids at start
        for(int a = 0; a < k; ++a) {
            //Start with the centroids as random values for R G B
            Random rand = new Random();
            centroids[a][0] = rand.nextDouble() * 255; //Random double between 0.0-255.0
            centroids[a][1] = rand.nextDouble() * 255;
            centroids[a][2] = rand.nextDouble() * 255;
            knt[a] = 0;
        }
    }

    private void startCentroidsDivided() {
        int chunk = 256/k;
        for(int a = 0; a < k; ++a) {
            //Algorithm splits 256 into k chunks so we get an even split of values
            centroids[a][0] = ((a*chunk) + ((a+1) * chunk) - 1)/2;
            centroids[a][1] = ((a*chunk) + ((a+1) * chunk) - 1)/2;
            centroids[a][2] = ((a*chunk) + ((a+1) * chunk) - 1)/2;
            knt[a] = 0;
        }
    }

    private double calc_distance(int[] reds, int[] greens, int[] blues, double[][] centroids, int a, int b) {
    	//Calculate distance from colour ato centroid b
        return (Math.pow((reds[a] - centroids[b][0]), 2) + Math.pow((greens[a] - centroids[b][1]), 2) +
                Math.pow((blues[a] - centroids[b][2]), 2));
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
