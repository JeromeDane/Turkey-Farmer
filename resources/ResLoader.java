/**
 * ResLoader.java
 * 
 * Resource loader used by other classes for loading images relative 
 * to this file. This allows for packaging of resources such as images
 * or audio files into jar files so that they can still be referenced
 * properly at execution time, removing the need to download images
 * along with the executable jar file.
 * 
 * Note / Full Disclosure: We did not come up with this idea on our own. Credit for the
 * original idea goes to the collective internet, and the sites resulting 
 * from googling "Embed BufferedImage in jar file" 
 *
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 */

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class ResLoader {

	static ResLoader rl = new ResLoader();
	
	public static BufferedImage getImage(String fileName) {
		
		try {
			return ImageIO.read(rl.getClass().getResource(fileName));
		} catch (Exception e) {
			System.err.println("Unable to find image file " + fileName);
			return null;
		}
	}
	
}
