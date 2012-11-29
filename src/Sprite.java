/**
 * Sprite.java
 * 
 * Provides functionality for easily drawing images on a two dimensional
 * graphic object. It also allows for the drawing of an image offset
 * so that it is centered around its x/y coordinate rather than drawn
 * so that the top left corner is at the x/y coordinate
 * 
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 * 
 */


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.xml.internal.ws.api.ResourceLoader;


/**
 * Base class for all drawable game sprites.  
 * 
 * @author Jerome Dane & Sandra Paulos
 *
 */
public class Sprite {
	protected float _x, _y;			// location in x,y coordinate space
	protected int _max;				// maximum value in coordinate space
	protected BufferedImage _image;	// container for sprite's image
	
	public Sprite() {
		
	}
	public Sprite(String imageFileName) {
		setImage(imageFileName);
	}
	public Sprite(float x, float y) {
		setX(x);
		setY(y);
	}
	public Sprite(float x, float y, String imageFileName) {
		this(x,y);
		setImage(imageFileName);
	}
	
	
	/**
	 * Draw the sprite on a 2d graphic
	 * 
	 * @param g - The Graphics2D object on which to draw the sprite
	 */
	public void draw(Graphics2D g) {
		if(_image != null) {
			
			g.drawImage(_image, null, (int) getX(), (int) getY());
		}
	}
	
	/**
	 * Draw the sprite on a 2d graphic centered around its x,y point
	 * 
	 * @param g - The Graphics2D object on which to draw the sprite
	 */
	public void drawCentered(Graphics2D g) {
		if(_image != null) {
		
			g.drawImage(_image, null, _getOffsetX(), _getOffsetY());
		}
	}
	
	/**
	 * Get the sprite's x coordinate
	 * 
	 * @return - The value of the sprite's x location in an x,y coordinate space
	 */
	public float getX() {
		return _x;
	}

	/**
	 * Get the sprite's y coordinate
	 * 
	 * @return - The value of the sprite's y location in an x,y coordinate space
	 */
	public float getY() {
		return _y;
	}
	
	/** 
	 * Prepare a BufferedImage
	 * 
	 * @param fileName
	 */
	public BufferedImage prepareImage(String fileName) {
		try {
//			return ImageIO.read(new File(fileName));
			return ResLoader.getImage(fileName);
		} catch (Exception e) {
			System.err.println("Unable to find image file " + fileName);
			return null;
		}
	}
	
	/**
	 * Set the image from a file name of the sprite to be drawn
	 * 
	 * @param fileName - Name of file to be used for sprite's image
	 */
	public void setImage(String fileName) {
		BufferedImage image = prepareImage(fileName);
		if(image != null) {
			setImage(image);
		}
	}
	

	/** 
	 * Set the sprite's image from an existing image
	 * 
	 * @param image - A BufferedImage to use as the sprite's image
	 */
	public void setImage(BufferedImage image) {
		_image = image;
	}
	
	/**
	 * Set the sprite's x location in an x,y coordinate space
	 * 
	 * @param x - The sprite's x location in an x,y coordinate space
	 */
	public void setX(float x) {
		_x = x;
	}
	public void setX(int x) {
		_x = (float) x;
	}
	
	public int getHeight() {
		return _image.getHeight();
	}
	public int getWidth() {
		return _image.getWidth();
	}
	
	/**
	 * Set the sprite's x location in an x,y coordinate space
	 * 
	 * @param x - The sprite's x location in an x,y coordinate space
	 */
	public void setY(float y) {
		_y = y;
	}
	public void setY(int y) {
		_y = (float) y;
	}
	
	/**
	 * Get the The x offset at which the image's top left corner will be 
	 * drawn in order to be centered around the sprite's x coordinate
	 * 
	 * @return x coordinate
	 */
	protected int _getOffsetX() {
		return (int) getX() - (_image.getWidth() / 2);
	}
	
	/**
	 * Get the the x offset at which the image's top left corner will be 
	 * drawn in order to be centered around the sprite's x coordinate
	 * 
	 * @return x coordinate
	 */
	protected int _getOffsetY() {
		return (int) getY() - (_image.getHeight() / 2);
	}
	
	/**
	 * Get the distance between this and another sprite from
	 * their center points
	 * 
	 * @param sprite
	 * @return
	 */
	public float getDistanceFrom(Sprite sprite) {
		return this.getDistanceFrom(sprite.getX(), sprite.getY());
	}
	
	/**
	 * Get the distance between this and an x,y point
	 * 
	 * @param sprite
	 * @return
	 */
	public float getDistanceFrom(float x, float y) {
		
		float xDist = Math.abs(_x - x);
		float yDist = Math.abs(_y - y);
		
		return (float) Math.sqrt(xDist * xDist + yDist * yDist);
		
	}
	
	
	
}
