/**
 * Turkey.java
 * 
 * Extends MovingSprite
 * 
 * The class representing the Turkey object in the game. This class needs to
 * have some representation of current location, a reference to the Farmer, and
 * a speed.
 * 
 * Creating a new Turkey - Turkeys should appear at the edge of the screen.
 * Thus, your constructor for a Turkey should take a Farmer, a speed, and the x
 * and y maximum values as its parameters. Randomly choose which side the turkey
 * will appear at and then randomly choose which side the turkey will exit at
 * (not the same side as the entrance)
 * 
 * Move method - The move method for the turkey will be more complicated than
 * that of the Farmer. The Farmer always walks directly toward the mouse
 * pointer. The turkey will walk in a straight line toward the randomly chosen
 * exit side. If the farmer gets close (within 50 pixels), then the turkey
 * should move directly away from the Farmer at double its speed. Once it is far
 * enough away, it resumes its original path.
 *
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 * 
 */

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import com.sun.org.apache.xpath.internal.axes.SelfIteratorNoPredicate;

public class Turkey extends MovingSprite {

	private ArrayList<Turkey> _turkeys;
	private Farmer _farmer;
	private int _personalSpaceRadius = 20;
	private long _infectionStarted = 0;
	private int _incubationInSeconds = 9;
	private boolean _wasZombie = false;
	private BufferedImage _zImageN;
	private BufferedImage _zImageS;
	private BufferedImage _zImageE;
	private BufferedImage _zImageW;
	
	// zombies are short sighted and don't recognize farmers
	private int _zombieFarmerSightLength = 150;		
	// zombies are short sighted but know a turkey when they see one
	private int _zombieTurkeySightLength = 250;		
	
	public Turkey(float x, float y) {
		super(x, y);
		
		// set up animation
		String[] directions = {"N", "S", "E", "W"};
		for(int i = 0; i < directions.length; i++) {
			String dir = directions[i];
			AnimatedImage anim = new AnimatedImage();
			anim.addImage("images/turkey_" + dir + ".png");
			anim.addImage("images/turkey_" + dir + "2.png");
			anim.addImage("images/turkey_" + dir + "3.png");
			setDirectionalImage(dir, anim);
		}
		
	}
	
	/**
	 * Override MovingSprite's constructor to just take a speed
	 * since we'll be setting the bounds and starting points later
	 * 
	 * @param speed
	 */
	public Turkey(float speed) {
		this(0,0);
		_speed = speed;
	}
	
	
	/**
	 * Move the turkey according to its speed and the elapsedTime since the last
	 * refresh of the screen.
	 * 
	 * @param elapsedTime
	 *            seconds since last update
	 */
	public void move() {
		
		
		// store the original speed
		float originalSpeed = _speed;
		
		if(isZombie()) {
			_moveAsZombie();
		} else {
			_moveAsTurkey();
		}
		
		// find the closest side
		String closestSide = getClosestSide();
		
		// if too close to the closest side then move to the opposite side
		if(_getDistanceFromSide(closestSide) < _personalSpaceRadius) {
			
			switch(closestSide) {

				case "top":
					setTarget(getRandomXPointWithinBounds(), getBottomtBound());
					break;
				
				case "right":
					setTarget(getLeftBound(), getRandomYPointWithinBounds());
					break;
					
				case "bottom":
					setTarget(getRandomXPointWithinBounds(), getTopBound());
					break;
					
				case "left":
					setTarget(getRandomXPointWithinBounds(), getRandomYPointWithinBounds());
					break;
			}
		}
		
		// keep turkeys from freezing at a previously set target by setting a new random target
		if(getDistanceFrom(_targetX, _targetY) < 2) {
			setTarget(getRandomXPointWithinBounds(), getRandomYPointWithinBounds());
		}
		
		super.move();
		setSpeed(originalSpeed);
	}
	
	private void _chaseFarmer() {
		// braaaaaaiiiinnnsssss! Run faster!
		setSpeed((float) (_speed * 1.5));
		setTarget(_farmer);
	}
	
	/**
	 * Apply zombie turkey movement
	 */
	private void _moveAsZombie() {
		
		//infect any turkeys that got too close
		for(Turkey turkey : _turkeys) {
			if(!turkey.isInfected() && getDistanceFrom(turkey) < 35) {
				turkey.infect();
			}
		}
		
		// kill the farmer if he's too close
		if(getDistanceFrom(_farmer) < 25) {
			_farmer.makeDead("You have been eaten by a zombie turkey! Click HERE to try again.");
		}
		
		// how far away am I from the farmer?
		float distanceFromFarmer = getDistanceFrom(_farmer);
		
		// check for the closest turkey if there are any
		ArrayList<Turkey> healthyTurkeys = _getHealthyTurkeys();
		if(healthyTurkeys.size() > 0) {
			// create a bogus turkey for comparison and code happiness
			Turkey closestTurkey = new Turkey(0,0) ;
			// set up a variable to store the smallest distance
			float smallestDistance = -1;
			// find the closest healthy turkey
			for(Turkey turkey : healthyTurkeys) {
				float testDistance = getDistanceFrom(turkey);
				if(smallestDistance == -1 || testDistance < smallestDistance) {
					smallestDistance = testDistance;
					closestTurkey = turkey;
				}
			}
			
			// Chase the farmer if he is within units and closer than the closest turkey  
			if(distanceFromFarmer < _zombieFarmerSightLength && distanceFromFarmer < smallestDistance) {
				_chaseFarmer();
			} else if(getDistanceFrom(closestTurkey) < _zombieTurkeySightLength){
				// otherwise chase the closest turkey if it's in range
				setTarget(closestTurkey);
			}
			
		} else 
		// If there are no heathy turkeys then chase the farmer if he's close enough	
		if(distanceFromFarmer < _zombieFarmerSightLength) {
			_chaseFarmer();
		}
		
		// otherwise stay the course (don't set a new target)
			
	}
	
	/**
	 * Get all the healthy turkeys on the field.
	 * Zombies know healthy meat when the see it.
	 */
	private ArrayList<Turkey> _getHealthyTurkeys() {
		ArrayList<Turkey> turkeys = new ArrayList<>();
		for(Turkey turkey : _turkeys) {
			if(!turkey.isZombie() && !turkey.isInfected()) {
				turkeys.add(turkey);
			}
		}
		return turkeys;
	}
	
	private void _moveAsTurkey() {

		// if close to farmer then run away
		if(getDistanceFrom(_farmer) <= 50) {
			
			// The turkey is scared of the farmer! 
			setTargetOpposite(_farmer);
			
			// Run away really fast!
			setSpeed(_speed * 2);
			
		}	
		
		// If to close to another turkey then respect its personal space
		for(Turkey otherTurkey : _turkeys) {
			
			// I'm not afraid of myself, and I don't know what zombies are
			if(otherTurkey != this && !otherTurkey.isZombie()) {
				
				// respect the other turkey's personal space but stick around long enough so he sees me too
				if(getDistanceFrom(otherTurkey) < _personalSpaceRadius - 5) {
					
					setTargetOpposite(otherTurkey);
				}
				
			}
			
		}
		
	}
	
	/**
	 * The turkey sees the farmer so it can react to him
	 * 
	 * @param farmer
	 */
	public void see(Farmer farmer) {
		_farmer = farmer;
	}
	
	/**
	 * The turkey sees all the turkeys on the field so it can react to them
	 * 
	 * @param farmer
	 */
	public void see(ArrayList<Turkey> turkeys) {
		_turkeys = turkeys;
	}
	
	/**
	 * Override MovingSprite's setBounds method to do everything MovingSprite does
	 * as well as determine a random starting edge
	 * 
	 */
	public void setBounds(int top, int right, int bottom, int left) {
		
		// set the bounds as normal
		super.setBounds(top, right, bottom, left);
		
		// set a random starting edge and target
		_setRandomStartingEdgeAndTarget();

	}
	
	public void drawCentered(Graphics2D g) {
		
		super.drawCentered(g);
		
		if(isInfected() && !isZombie()) {
			int secondsLeft = _incubationInSeconds - _getSecondsSinceInfected();
			g.drawString(secondsLeft + "", getX() - 4, getY() + 6);
		}
		
	}

	protected int _getSecondsSinceInfected() {
		return (int) ((System.currentTimeMillis() - _infectionStarted) / 1000); 
	}
	
	
	private float _getDistanceFromSide(String side) {
		float distanceFromSide = 0;
		switch(side) {

			case "top":
				distanceFromSide = getY() - getTopBound();
				break;
			
			case "right":
				distanceFromSide = getRightBound() - getX();
				break;
				
			case "bottom":
				distanceFromSide = getBottomtBound() - getY();
				break;
				
			case "left":
				distanceFromSide = getX() - getLeftBound();
				break;
		}
		return distanceFromSide;
	}

	private String _getRandomSide() {
	
		// define a random number generator
		Random rand = new Random();
		
		// move turkey to a random starting edge and tell it where the target is
		float randSideSelector = rand.nextFloat();
		
		if(randSideSelector < 0.25) {
			return "top";
		} else if(randSideSelector < 0.5) {
			return "right";
		} else if(randSideSelector < 0.75) {
			return "bottom";
		}
		return "left";
	}
	
	/**
	 * Set a random starting edge for the Turkey as well as a random target on
	 * the opposite edge
	 * 
	 */
	private void _setRandomStartingEdgeAndTarget() {
		
		// set a random starting point within the bounds
		setX(getRandomXPointWithinBounds());
		setY(getRandomYPointWithinBounds());
		
		// randomly move to a side
		switch(_getRandomSide()) {

			// start at top and move down
			case "top":
				setY(getTopBound());
				setTarget(getRandomXPointWithinBounds(), getBottomtBound());
				break;
			
			// start on right and move left
			case "right":
				setX(getRightBound());
				setTarget(getLeftBound(), getRandomYPointWithinBounds());
				break;
				
			// start on the bottom and move up
			case "bottom":
				setY(getBottomtBound());
				setTarget(getRandomXPointWithinBounds(), getTopBound());
				break;
				
			// start on left and move right
			case "left":
				setX(getLeftBound());
				setTarget(getRightBound(), getRandomYPointWithinBounds());
				break;
		}
		
	}

	public boolean isZombie() {
		boolean isZombie = (isInfected() && _getSecondsSinceInfected() >  _incubationInSeconds);

		// initialize some zombie things
		if(isZombie && !_wasZombie) {
			// set up animation
			String[] directions = {"N", "S", "E", "W"};
			for(int i = 0; i < directions.length; i++) {
				String dir = directions[i];
				AnimatedImage anim = new AnimatedImage();
				anim.addImage("images/zturkey_" + dir + ".png");
				anim.addImage("images/zturkey_" + dir + "2.png");
				anim.addImage("images/zturkey_" + dir + "3.png");
				setDirectionalImage(dir, anim);
			}
			_wasZombie = true;
		}
		return isZombie;
	}
	
	public boolean isInfected() {
		return _infectionStarted > 0;
	}
	
	public void infect() {
		_infectionStarted = System.currentTimeMillis();		
	}
	public void infect(int incubationTime) {
		_incubationInSeconds = incubationTime;
		this.infect();		
	}
	
	

	

}
