/** * AnimatedImage.java
 *
 * This class handles the animation of imagges by storing
 * a sequence of static images and returning the current
 * image in the sequence based on the time it should take
 * for the animation to complete.
 *
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class AnimatedImage {
	private ArrayList<BufferedImage> _images;
	private long _startTime;
	private int _durationInMs; 
	
	public AnimatedImage() {
		
		// set up a start time for use in animation speed
		_startTime = System.currentTimeMillis();
		
		// initialize a bucket for images
		_images = new ArrayList<>();
		
		// set default amimation duration in milliseconds 
		_durationInMs = 1000;
	}
	public AnimatedImage(int duration) {
		this();
		
		_durationInMs = duration;
		
	}
	
	
	/**
	 * Add an image onto the animation sequence
	 */
	public void addImage(BufferedImage image) {
		_images.add(image);
	}
	public void addImage(String fileName) {
		addImage(prepareImage(fileName));
	}
	
	/**
	 * Set how many milliseconds it takes the animation to complete
	 * 
	 * @param duration
	 */
	public void setDuration(int duration) {
		_durationInMs = duration;
	}

	/** 
	 * Prepare a BufferedImage
	 * 
	 * @param fileName
	 */
	public BufferedImage prepareImage(String fileName) {
		try {
			return ResLoader.getImage(fileName);
		} catch (Exception e) {
			System.err.println("Unable to find image file " + fileName);
			return null;
		}
	}
	
	
	/**
	 * Get an image by index
	 * 
	 * @return
	 */
	public BufferedImage getImage(int index) {
		if(index < _images.size()) {
			return _images.get(index);
		} 
		return null;
	}
	
	/**
	 * Get the current image
	 * 
	 * @return
	 */
	public BufferedImage getImage() {
		
		// create a container image
		BufferedImage image = null;
		
		// figure out how many MS per animation image
		long msPerImage = _durationInMs / _images.size();
		
		// figure out how far we are into the animation cycle
		long ms = System.currentTimeMillis() - _startTime;
		long msInCycle = ms % _durationInMs;

		// check to see which image we should use
		for(int i = 0; i < _images.size(); i++) {
			if(msInCycle < msPerImage * (i + 1)) {
				image = _images.get(i);
				break;
			}
		}
		
		return image;
	}
	
}
