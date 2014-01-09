/* Copyright (c) 2014 Karol Stasiak
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/

package io.github.karols.hocr4j;

import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

/**
 * Utility class for rendering pages into images.
 */
public class PageRenderer {
    private Color backgroundColor = Color.WHITE;
    private Font boldFont;
    private Font boldItalicFont;
    final private Collection<Pair<Color, Bounds>> coloredRectanglesToDraw = new ArrayList<Pair<Color, Bounds>>();
    private Color defaultRectangleColor = Color.ORANGE;
    private Color fontColor = Color.RED;
    private Font italicFont;
    private Font plainFont;
    final private Collection<Bounds> rectanglesToDraw = new ArrayList<Bounds>();
    private double scale = 1;
    private float strokeWidth = 3;

    /**
     * Creates a default page renderer.
     */
    public PageRenderer() {
        try {
            setFontFamily("Arial", 15);
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * A modifiable collection of rectangles to be drawn on the image using custom colors..
     *
     * @return collection of pairs of a color and a rectangle
     */
    public Collection<Pair<Color, Bounds>> coloredRectanglesToDraw() {
        return coloredRectanglesToDraw;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color for the rendered page.
     *
     * @param backgroundColor new background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Font getBoldFont() {
        return boldFont;
    }

    /**
     * Sets a font for rendering bold non-italic words.
     *
     * @param boldFont new bold font
     */
    public void setBoldFont(Font boldFont) {
        this.boldFont = boldFont;
    }

    public Font getBoldItalicFont() {
        return boldItalicFont;
    }

    /**
     * Sets a font for rendering bold italic words.
     *
     * @param boldItalicFont new bold italic font
     */
    public void setBoldItalicFont(Font boldItalicFont) {
        this.boldItalicFont = boldItalicFont;
    }

    public Color getDefaultRectangleColor() {
        return defaultRectangleColor;
    }

    /**
     * Sets the default rectangle color.
     *
     * @param defaultRectangleColor new color
     */
    public void setDefaultRectangleColor(Color defaultRectangleColor) {
        this.defaultRectangleColor = defaultRectangleColor;
    }

    public Color getFontColor() {
        return fontColor;
    }

    /**
     * Sets font color.
     *
     * @param fontColor new font color
     */
    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Font getItalicFont() {
        return italicFont;
    }

    /**
     * Sets a font for rendering non-bold italic words.
     *
     * @param italicFont new italic font
     */
    public void setItalicFont(Font italicFont) {
        this.italicFont = italicFont;
    }

    public Font getPlainFont() {
        return plainFont;
    }

    /**
     * Sets a font for rendering non-bold non-italic words.
     *
     * @param plainFont new plain font
     */
    public void setPlainFont(Font plainFont) {
        this.plainFont = plainFont;
    }

    public double getScale() {
        return scale;
    }

    /**
     * Sets scale in which the page has to be rendered.
     * For example scale = 0.5 means
     * that the page of size 3000&times;5000 pixels
     * will be rendered as 1500&times;2500 pixels.
     *
     * @param scale new scale
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets stroke width for rendering rectangles.
     *
     * @param strokeWidth new stroke width
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * A modifiable collection of rectangles to be drawn on the image.
     * They will be drawn using the default rectangle color.
     *
     * @return collection of rectangles
     * @see PageRenderer#setDefaultRectangleColor(java.awt.Color)
     */
    public Collection<Bounds> rectanglesToDraw() {
        return rectanglesToDraw;
    }

    /**
     * Renders this page on a blank image.
     * The image is filled with the background color.
     *
     * @param page page to render
     * @return rendered image
     */
    @Nonnull
    public BufferedImage renderOnBlank(@Nonnull Page page) {
        Bounds pageBounds = page.getBounds().scale(scale);
        BufferedImage img = new BufferedImage(pageBounds.getRight(), pageBounds.getBottom(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        renderOnTop(page, img);
        return img;
    }

    /**
     * Renders this page on a blank image and saves it as a PNG file.
     *
     * @param page       page to render
     * @param outputFile output file
     * @throws IOException
     */
    public void renderOnBlank(@Nonnull Page page, @Nonnull File outputFile) throws IOException {
        BufferedImage img = renderOnBlank(page);
        ImageIO.write(img, "png", outputFile);
    }

    /**
     * Renders this page on the given image and saves it as a PNG file.
     * The image is modified, not copied.
     *
     * @param page       page to render
     * @param outputFile output file
     * @throws IOException
     */
    public void renderOnTop(@Nonnull Page page, @Nonnull BufferedImage image, @Nonnull File outputFile) throws IOException {
        renderOnTop(page, image);
        ImageIO.write(image, "png", outputFile);
    }

    /**
     * Renders this page on an image loaded from given input file
     * and saves it as a PNG file.
     *
     * @param page       page to render
     * @param inputFile  input file
     * @param outputFile output file
     * @throws IOException
     */
    public void renderOnTop(@Nonnull Page page, @Nonnull File inputFile, @Nonnull File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        renderOnTop(page, image);
        ImageIO.write(image, "png", outputFile);
    }

    /**
     * Renders this page on an image loaded from given input file.
     *
     * @param page      page to render
     * @param inputFile input file
     * @throws IOException
     */
    public BufferedImage renderOnTop(@Nonnull Page page, @Nonnull File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        renderOnTop(page, image);
        return image;
    }

    /**
     * Renders this page on the given image.
     * The image is modified, not copied.
     *
     * @param page page to render
     */
    public void renderOnTop(@Nonnull Page page, @Nonnull BufferedImage img) {
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.RED);
        for (Area a : page) {
            for (Paragraph p : a) {
                for (Line l : p) {
                    for (Word w : l.words) {
                        if (w.isBold()) {
                            if (w.isItalic()) {
                                g.setFont(boldItalicFont);
                            } else {
                                g.setFont(boldFont);
                            }
                        } else if (w.isItalic()) {
                            g.setFont(italicFont);
                        } else {
                            g.setFont(plainFont);
                        }
                        Bounds b = w.getBounds().scale(scale);
                        g.drawString(w.getText(), b.getLeft(), b.getBottom());
                    }
                }
            }
        }
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(defaultRectangleColor);
        for (Bounds rect : rectanglesToDraw) {
            if (rect != null) {
                Bounds b = rect.scale(scale);
                g.drawRect(b.getLeft(), b.getTop(), b.getWidth(), b.getHeight());
            }
        }
        for (Pair<Color, Bounds> rect : coloredRectanglesToDraw) {
            if (rect != null) {
                g.setColor(rect.getLeft());
                Bounds b = rect.getRight().scale(scale);
                g.drawRect(b.getLeft(), b.getTop(), b.getWidth(), b.getHeight());
            }
        }
    }

    /**
     * Sets all fonts as belonging to the same family.
     * Default family is Arial size 15.
     *
     * @param fontFamilyName family name, e.g. "Arial"
     * @param size           size, e.g. 16
     */
    public void setFontFamily(String fontFamilyName, int size) {
        plainFont = new Font(fontFamilyName, Font.PLAIN, size);
        boldFont = new Font(fontFamilyName, Font.BOLD, size);
        italicFont = new Font(fontFamilyName, Font.ITALIC, size);
        boldItalicFont = new Font(fontFamilyName, Font.BOLD | Font.ITALIC, size);
    }
}
