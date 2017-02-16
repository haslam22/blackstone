package gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 *
 * @author Hassan
 */
public class GomokuStone implements Icon {
    
    public static enum StoneColor {
        BLACK, WHITE
    }
    
    private int width;
    private int height;
    private final StoneColor color;
    private BufferedImage image;
    private float alpha;
    
    public GomokuStone(StoneColor color) {
        this.width = 0;
        this.height = 0;
        this.color = color;
    }    
    
    public GomokuStone(StoneColor color, float alpha) {
        this.width = 0;
        this.height = 0;
        this.color = color;
        this.alpha = alpha;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double coef = Math.min((double) width / (double) 250, 
                (double) height / (double) 250);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(coef, coef);
        paint(g2d);
        g2d.dispose();
        g.drawImage(image, x, y, null);    
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }    
    
    private void paint(Graphics2D g) {
        if(this.alpha != 0) {
            AlphaComposite alphacom = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha);
            g.setComposite(alphacom);
        }
        Shape shape = new Ellipse2D.Double(0, 0, 250, 250);
        g.setPaint(Color.WHITE);
        g.fill(shape);
        
        Color[] gradientColors = new Color[2];
        
        if(this.color == StoneColor.BLACK) {
            gradientColors[0] = new Color(0xA0A0A0);
            gradientColors[1] = new Color(0xE6000000, true);
        } else if(this.color == StoneColor.WHITE) {
            gradientColors[0] = Color.WHITE;
            gradientColors[1] = new Color(0xA0A0A0);
        }

        shape = new Ellipse2D.Double(0, 0, 250, 250);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(0.5, 0.5), 0.5f, 
                new Point2D.Double(0.75, 0.75), 
                new float[]{0, 1}, 
                gradientColors, 
                MultipleGradientPaint.CycleMethod.NO_CYCLE, 
                MultipleGradientPaint.ColorSpaceType.SRGB, 
                new AffineTransform(250, 0, 0, 250, 0, 0))
        );
        g.fill(shape);
        g.setPaint(new Color(0x4D000000, true));
        g.setStroke(new BasicStroke(1.2f, 0, 0, 4));
        g.draw(shape);
    }
}
