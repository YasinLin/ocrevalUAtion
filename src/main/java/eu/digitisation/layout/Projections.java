/*
 * Copyright (C) 2013 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.digitisation.layout;

import eu.digitisation.image.Bimage;
import eu.digitisation.image.Display;
import eu.digitisation.io.FileType;
import eu.digitisation.math.ArrayMath;
import eu.digitisation.math.Plot;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.JAI;

/**
 * A printed text
 *
 * @author R.C.C.
 * @version 20131110
 */
public class Projections extends Bimage {

    double threshold; // the threshold applied for ine segmentation

    /**
     *
     * @param file
     * @throws java.io.IOException
     * @throws NullPointerException if the file format is unsupported
     */
    public Projections(File file) throws IOException {
        super(JAI.create("FileLoad",
                file.getCanonicalPath()).getAsBufferedImage(),
                BufferedImage.TYPE_INT_RGB);
        readProperties();
    }

    private void readProperties() {
        Properties prop = new Properties();
        try {
            InputStream in
                    = FileType.class.getResourceAsStream("/General.properties");
            prop.load(in);
            String s = prop.getProperty("line.threshold");
            if (s != null && s.length() > 0) {
                threshold = Double.valueOf(s);
            } else { // defualt value
                threshold = -0.4;
            }
            System.err.println("threshold=" + threshold);
        } catch (IOException ex) {
            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @return gray level obtained by adding R, G and B components: 0 is minimum
     * and 765 maximum.
     */
    private int darkness(int rgb) {
        Color c = new Color(rgb);
        return 765 - c.getRed() - c.getGreen() - c.getBlue();
    }

    /**
     * For debugging: print vertical projection of gray levels
     */
    public void stats() {
        for (int y = 0; y < getHeight(); ++y) {
            int sum = 0;
            for (int x = 0; x < getWidth(); ++x) {
                int rgb = getRGB(x, y);
                sum += darkness(rgb);
            }
            System.out.println(y + " " + sum);
        }
    }

    /**
     * Sum RGB values for all x
     *
     * @param y the value of y
     * @return
     */
    private int[] sumRGB(int y) {
        int[] s = new int[3];
        for (int x = 0; x < getWidth(); ++x) {
            int rgb = getRGB(x, y);
            s[0] += (rgb & 0xff0000) >> 16;
            s[1] += (rgb & 0x00ff00) >> 8;
            s[2] += (rgb & 0x0000ff);
        }
        return s;
    }

    /**
     * Project gray values over Y-axis
     *
     * @return the darkness for every row (x-value) in the image.
     */
    private int[] yprojection() {
        int[] values = new int[getHeight()];
        for (int y = 0; y < getHeight(); ++y) {
            int sum = 0;
            for (int x = 0; x < getWidth(); ++x) {
                int rgb = getRGB(x, y);
                sum += darkness(rgb);
            }
            values[y] = sum;
        }
        return values;
    }

    /**
     * Split image into component lines
     */
    public void slice() {
        int[] values = yprojection();
        ArrayList<Integer> limits = new ArrayList<Integer>();
        double B = ArrayMath.average(values);
        //double A = Math.max(Stat.max(values) - B, B - Stat.min(values));
        double sigma = ArrayMath.std(values);
        int upper = 0;
        boolean inner = false;
        double[] Y = new double[values.length];
        double[] Z = new double[values.length]; // normalized values

        for (int y = 0; y < getHeight(); ++y) {
            double nval = (values[y] - B) / sigma; // normalized value
            System.out.println(y + " " + nval);
            Y[y] = y;
            Z[y] = nval;
            if (inner) {
                if (nval < threshold) {
                    limits.add(y);
                    inner = false;
                }
            } else {
                if (nval > threshold) {
                    limits.add(y);
                    inner = true;
                }
            }
        }
        addBoxes(limits);
        new Plot(Y, Z).show(400, 400, 40);
    }

    /**
     * Add boxes to image
     *
     * @param limits
     */
    private void addBoxes(List<Integer> limits) {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN};
        for (int n = 0; n + 1 < limits.size(); ++n) {
            int s = n / 2;
            int y = limits.get(n);
            int h = limits.get(++n) - y; // rectangle height
            int x = 10 * (n % 2); // avoid full overlapping 
            int w = getWidth() - 10 - 2 * x; // rectangle width
            Polygon poly = new Polygon();
            poly.addPoint(x, y);
            poly.addPoint(x + w, y);
            poly.addPoint(x + w, y + h);
            poly.addPoint(x, y + h);

            add(poly, colors[s % 4], 1);
        }
    }

    public static void main(String[] args) throws Exception {
        String ifname = args[0];
        String ofname = args[1];
        File ifile = new File(ifname);
        File ofile = new File(ofname);
        Projections p = new Projections(ifile);
        p.slice();
        //p.addLabel("(x,y)=(100,50)", 100, 50);
        p.write(ofile);
        System.err.println("Output image in " + ofname);
        Display.draw(p, 600, 900);
    }

}
