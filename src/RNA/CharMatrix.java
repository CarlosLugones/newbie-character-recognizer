package rna;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Stores the state of an image of 64x64 pixels.
 * Each pixel is rendered as a square pixel of 10x10 pixels.
 * It is rendered over the graphic context that is provided.
 * @author Carlos Ariel <carojas@estudiantes.uci.cu>
 */
public class CharMatrix implements Rendereable {

    private boolean[][] pixels;
    private final int bound = 300;
    public static final int width = 32;
    public static final int scale = 6;
    private int brushSize = 1;
    
    private Graphics graphics;
    private BufferedImage previousImage;
    private final boolean wire = false;

    /**
     * 
     * @param graphics - The graphic context
     */
    public CharMatrix(Graphics graphics) {
        this.pixels = new boolean[width][width];
        this.graphics = graphics;
    }
    
    /**
     * Gets the value of the pixel in the position (x,y)
     * @param x the x component of the pixel's coordenade
     * @param y the y component of the pixel's coordenade
     * @return true if pixel is black, false if pixel is white   
     */
    public boolean get(int x, int y) {
        return pixels[x][y];
    }        
    
    /**
     * Sets the value of the pixel in the position (x,y)
     * @param x the x component of the pixel's coordenade
     * @param y the y component of the pixel's coordenade
     * @param value the value to be set
     */
    public void set(int x, int y, boolean value){
        if (brushSize > 1) {
            try {
                for (int i=x; i<x+brushSize; i++) {
                    for (int j=y; j<y+brushSize; j++) {
                        pixels[i][j] = value;
                    }
                }
            } catch (Exception e) {
            
            }
        } else {
            pixels[x][y] = value;
        }
    }
    
    public void set(double[] d) {
        int x=0;
        int y=0;
        for (int i=0; i<d.length; i++) {
            boolean value = false;
            if (d[i]==0) {
                value = false;
            } else {
                value = true;
            }
            set(x, y, value);
            if (x==width-1) {
                x = 0;
                y++;
            } else {
                x++;
            }
        }
    }      

    public Graphics getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }
    
    /**
     * Renders the image in the graphic context
     */
    @Override
    public void render(){
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.drawRenderedImage(getImage(), AffineTransform.getTranslateInstance(0, 0));
        
    }
    
    private BufferedImage getImage() {
        
        previousImage = new BufferedImage(bound, bound, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = previousImage.createGraphics();     
        
        // Reset
        g.setColor(Color.white);
        g.fillRect(0, 0, bound, bound);
        
        // Pixel's state (black rect)
        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                boolean value = get(i,j);
                if (value == true)
                    g.setColor(Color.black);
                else
                    g.setColor(Color.white);
                g.fillRect(i*scale, j*scale, scale, scale);
            }
        }
        
        // Wire
        if (wire) {
            g.setColor(Color.black);
            for (int i=1; i<=width; i++){
                g.drawLine(0, i*scale, bound, i*scale);
                g.drawLine(i*scale, 0, i*scale, bound);
            }
        }
        
        
        g.dispose();
        
        return previousImage;
    }
    
    /**
     * Transforms the matrix into a int[].
     * Each position is filled with an integer according with the rule:
     * false --> 0, true --> 1
     * @return 
     */
    public int[] getLinearVector() {
        int[] result = new int[width*width];
        int[][] result2 = new int[width][width];
        int pos = 0;
        int minx = 1000, miny = 1000;
        for (int y=0; y<width; y++) {
            for (int x=0; x<width; x++) {
                int value = 0;
                if (pixels[x][y] == false) value = 0;
                if (pixels[x][y] == true)  value = 1;
                if(value==1)
                {
                    minx = Math.min(x, minx);
                    miny = Math.min(y, miny);
                }
                //result[pos] = value;
                //pos++;
            }
        }
        for(int y = 0; y < width; y++)
        {
            for(int x = 0; x < width; x++)
            {
                int value = 0;
                if (pixels[x][y] == false) value = 0;
                if (pixels[x][y] == true)  value = 1;
                if(value == 1)
                {
                    result2[x-minx][y-miny] = 1;
                }
            }
        }
        for(int y = 0; y < width; y++)
        {
            for(int x = 0; x < width; x++)
            {
                result[pos++] =  result2[x][y];
            }
        }
        return result;
    }
    
    public void clear() {
        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                pixels[i][j] = false;
            }
        }
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }    
    
    @Override
    public String toString(){
        String aux = "";
        for (int i=0; i<width; i++) {
            for (int j=0; j<width; j++) {
                boolean value = get(j,i);                
                if (value == true)
                    aux += "*";
                else
                    aux += " ";
            }
            aux += "\r\n";
        }
        return aux;
    }
    
}
