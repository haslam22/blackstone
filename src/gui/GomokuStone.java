package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Hassan
 */
public class GomokuStone {
    
    public static enum StoneColor {
        BLACK, WHITE
    }
    
    private final StoneColor color;
    private float alpha;    
    
    public GomokuStone(StoneColor color) {
        this.color = color;
    }
    
    public GomokuStone(StoneColor color, float alpha) {
        this.color = color;
        this.alpha = alpha;
    }
    
    private RadialGradientPaint getGradientPaint(int x, int y, int width, 
            int height) {
        Color[] colors = new Color[2];
        
        if(color == StoneColor.BLACK) {
            colors[0] = new Color(0xA0A0A0);
            colors[1] = new Color(0xE6000000, true);
        }
        else if(color == StoneColor.WHITE) {
            colors[0] = Color.WHITE;
            colors[1] = new Color(0xA0A0A0);
        }
        
        return new RadialGradientPaint(
                new Point2D.Double(0.5, 0.5), 
                0.5f, 
                new Point2D.Double(0.75, 0.75), 
                new float[]{0, 1}, 
                colors, 
                NO_CYCLE, 
                SRGB, 
                new AffineTransform(width, 0, 0, height, x, y)
        );
    }
    
    public void paintStone(Graphics2D g2d, int x, int y, int width, int height,
            Color backgroundColor) {
        
        if(this.alpha != 0) {
            AlphaComposite alphacom = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(alphacom);
        }
        Shape stone = new Ellipse2D.Double(x, y, width, height);
        g2d.setPaint(backgroundColor);
        g2d.fill(stone);
        g2d.setPaint(this.getGradientPaint(x, y, width, height));
        g2d.fill(stone);
    }
    
}
