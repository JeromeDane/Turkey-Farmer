/**
 * Farmer.java
 * 
 * Extends MovingSprite
 * 
 * The player's character. It should have a location, a location to which it is
 * trying to go, a relative speed, and a counter as to how many turkeys it has
 * caught.
 * 
 * Some descriptions of particularly important methods are provided below. You
 * will probably have to create more methods than just those provided. You are
 * also allowed to change the methods provided if you want. They have been added
 * as a guide.
 * 
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 * 
 */

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;

public class Farmer extends MovingSprite {

	private ArrayList<Turkey> _turkeys;
	private int _numTurkeysCaught;
	private String _deathMessage;
	private long _sprintTime = -1;
	private int _sprintDuration;
	private long _sprintRecoveryTime;
	private TurkeyField _simulation;
	
	public Farmer(float x, float y, float speed) {
		
		// create a moving sprite as usual
		super(x,y, speed);
		
		// set sprinting duration
		_sprintDuration = 5;
		_sprintRecoveryTime = 10;
		
		
		// initialize the turkey watching array
		_turkeys = new ArrayList<>();
		
		// set up animation
		String[] directions2 = {"N", "S", "E", "W"};
		for(int i = 0; i < directions2.length; i++) {
			String dir = directions2[i];
			AnimatedImage anim = new AnimatedImage();
			anim.addImage("images/farmer_" + dir + ".png");
			anim.addImage("images/farmer_" + dir + "2.png");
			anim.addImage("images/farmer_" + dir + "3.png");
			anim.addImage("images/farmer_" + dir + "4.png");
			setDirectionalImage(dir, anim);
		}
		setImageFromDirection("S");
		
		// set the initial number of turkeys caught
		_numTurkeysCaught = 0;
		
		// enable farmer debugging if desired (remove comment)
		// enableDebugMode();
		
	}
	
	/**
	 * Cause the farmer to sprint for a short time, but move much slower
	 * while he recovers afterwards
	 */
	public void sprint() {
		_sprintTime = System.currentTimeMillis();
	}
	
	/**
	 * Override the MovingSprite's move method to allow sprinting
	 */
	public void move() {
		
		// backup original speed
		float originalSpeed = _speed;
		
		// if we have sprinted
		if(_sprintTime != -1) {
		
			// see how long it's been since we started sprinting
			int secondsSprinting = (int) ((System.currentTimeMillis() - _sprintTime) / 1000);
			
			if(secondsSprinting <= _sprintDuration) {
				
				// move twice as fast if sprinting
				setSpeed(_speed * 2);
				
				// tell the player they started sprinting
				if(secondsSprinting == 0) {
					_simulation.tellPlayer("You sprint! Movement speed is doubled.");
				}
				
			} else if(secondsSprinting <=  _sprintDuration + _sprintRecoveryTime) {
				
				if(secondsSprinting == _sprintDuration + 1) {
					_simulation.tellPlayer("You're tired. Movement speed is halved.");
				}
				
				// move half as fast if recovering
				setSpeed((float) (_speed * .5));
				
			} else if(secondsSprinting == _sprintDuration + _sprintRecoveryTime + 1) {
				_simulation.tellPlayer("You've recovered");
			}
		}
		
		super.move();
		
		// restore original speed
		setSpeed(originalSpeed);
		
	}
	

	/**
	 * Try to catch a turykey
	 * 
	 * @param turkeys
	 *            An ArrayList of all the Turkeys on the field
	 * @return The string you want to print to the output
	 */
	public void tryToCatchATurkey() {
		
		// Only look at the healthy turkeys (eating zombie turkeys will make you sick!)
		ArrayList<Turkey> healthyTurkeys = _getRegularTurkeys();
		
		// If there are any healthy turkeys left (if not you're a bad farmer by the way)
		if(healthyTurkeys.size() > 0) {
			
			// look at all the healthy turkeys
			for(Turkey turkey : healthyTurkeys) {
				
				// catch one if it's in range
				if(getDistanceFrom(turkey) <= 45) {
					
					// catch the turkey (remove it from the referenced main turkey array)
					_turkeys.remove(turkey);
					
					// Keep track of how many turkeys I've caught
					_numTurkeysCaught++;
					
					
				}
			}
		}
		
	}

	/**
	 * Store a reference to all the turkeys on the field so that 
	 * the farmer knows how to react to them
	 * 
	 * @param turkeys
	 */
	public void see(ArrayList<Turkey> turkeys) {
		_turkeys = turkeys;				
	}
	
	/**
	 * Store a reference to the simulation so the farmer can
	 * tell the player things
	 * 
	 * @param turkeys
	 */
	public void see(TurkeyField simulation) {
		_simulation = simulation;				
	}
	
	/**
	 * Get the number of turkeys the farmer has caught 
	 * 
	 * @return
	 */
	public int getNumTurkeysCaught() {
		return _numTurkeysCaught;
	}
	
	public boolean isDead() {
		return _deathMessage != null;
	}	
	
	public void makeDead(String message) {
		_deathMessage = message;				
	}
	
	public String getDeathMessage() {
		return _deathMessage;
	}
	
	/**
	 * Get all the other turkeys that not zombies
	 * A good farmer knows the difference between a turky and a turky zombie!
	 * 
	 * @return ArrayList containing all the regular turkeys on the field
	 */
	private ArrayList<Turkey> _getRegularTurkeys() {
		ArrayList<Turkey> turkeys = new ArrayList<>();
		for(Turkey turkey : _turkeys) {
			if(!turkey.isZombie()) {
				turkeys.add(turkey);
			}
		}
		return turkeys;
	}
	
}
