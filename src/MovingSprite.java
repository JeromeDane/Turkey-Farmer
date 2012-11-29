/**
 * MovingSprite.java
 * 
 * Extends Sprite
 * 
 * Extends Sprite and adds movement while handling calculations to allow 
 * moving a given distance along a line rather than only in one of eight
 * directions. Also allows for the specification of an image for 
 * each of four directions so that it looks like the sprite is always
 * facing in the direction that it is moving.
 * 
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 * 
 */


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Random;


public class MovingSprite extends Sprite {

	protected float _targetX, _targetY, _speed;
	protected String _spriteFolderName;
	private int _topBound = -1;
	private int _rightBound = -1;
	private int _bottomBound = -1;
	private int _leftBound = -1;
	private boolean _inDebugMode = false;
	private float _angle;
	private Hashtable<String, BufferedImage> _directionalImages =  new Hashtable<>();
	private Hashtable<String, AnimatedImage> _directionalAnimations =  new Hashtable<>();
	
	private long _lastMoveTime = -1;			// the last time this was moved
	
	public MovingSprite() {
	}
	public MovingSprite(float x, float y) {
		super(x, y);
	}
	public MovingSprite(float x, float y, float speed) {
		this(x, y);
		_speed = speed;
	}

	/**
	 * Get the name of the side the moving sprite is closest to
	 * 
	 * @return
	 */
	public String getClosestSide() {

		float distToTop = getY() - getTopBound();
		float distToRight = getRightBound() - getX();
		float distToBottom = getBottomtBound() - getY();
		float distToLeft = getX() - getLeftBound();

		if(distToTop < distToRight && distToTop < distToBottom && distToTop < distToLeft) {
			return "top";
		} 
		else if(distToRight < distToTop && distToRight < distToBottom && distToRight < distToLeft) {
			return "right";
		} 
		else if(distToBottom < distToTop && distToBottom < distToRight && distToBottom < distToLeft) {
			return "bottom";
		}
		return "left";
	}

	
	/**
	 * Set a directional image
	 * 
	 * @param string
	 */
	public void setDirectionalImage(String direction, String fileName) {
		setDirectionalImage(direction, prepareImage(fileName));
	}
	public void setDirectionalImage(String direction, BufferedImage image) {
		_directionalImages.put(direction, image);
	}
	public void setDirectionalImage(String direction, AnimatedImage image) {
		_directionalAnimations.put(direction, image);
	}
	
	/**
	 * Update this sprite's image from the direction it's facing
	 * 
	 * @param string
	 */
	public void setImageFromDirection(String direction) {
		
		BufferedImage image = null;
		
		// check for an animation sequence
		if(_directionalAnimations.containsKey(direction)) {
			
			if(getDistanceFrom(_targetX, _targetY) > 5) {
				
				// if moving get the current animation image
				image = _directionalAnimations.get(direction).getImage();
			} else {
				
				// if not moving, just get the first static image in the animation seqience
				image = _directionalAnimations.get(direction).getImage(0);
			}
			
		} else 
		// otherwise check for a static image
		if(_directionalImages.containsKey(direction)) {
			image = _directionalImages.get(direction); 
		}
		
		if(image != null) {
			setImage(image);
		}
	}
	
	/**
	 * Set a new target in the opposite direction of the given sprite
	 * 
	 * @param sprite
	 */
	public void setTargetOpposite(Sprite sprite) {
		
		// the new target will be really far away from the offending sprite
		float distance = 999999;	
		
		// calculate angle of move away from sprite
		// see note on move() for credit on this formula
		double angle = Math.atan2(getY() - sprite.getY(), getX() - sprite.getX());
		float deltaX = (float) Math.cos(angle) * distance;
		float deltaY = (float) Math.sin(angle) * distance;
		
		setTarget(getX() + deltaX, getX() + deltaY);
	}
	
	public float getAngle() {
		return _angle;
	}
	
	public void enableDebugMode() {
		_inDebugMode = true;
	}
	public void disableDebugMode() {
		_inDebugMode = false;
	}
	
	public String getDirectionNSEW() {
		
		float angle = getAngle();
		float angleSize =(float) 0.75;
		
		if(Math.abs(angle) < angleSize) {
			return "E";
		} 
		else if(Math.abs(angle) < angleSize * 3) {
			return angle > 0 ? "S" : "N";
		}
		return "W";
		
	}
	
	public void drawCentered(Graphics2D g) {
		super.drawCentered(g);
		
		// change the sprite's image base don direction if image found	
		setImageFromDirection(getDirectionNSEW());
		
		// show line from sprite center to target if debug mode is enabled
		if(_inDebugMode) {
			g.drawLine((int) getX(), (int) getY(), (int) _targetX, (int) _targetY);
			// draw current direction and angle
			g.drawString(getDirectionNSEW() + ": " + getAngle() + "", getX(), getY() + getHeight() / 2 + 10);
		}
	}
	
	/**
	 * Move a certain distance along the line towards the target
	 * 
	 * Note: We knew roughly how to do this, but had to refresh
	 * ourselves on the actual formula. A quick search of Google
	 * turned up http://stackoverflow.com/a/5995931, which we used to 
	 * figure out how to calculate the X and Y deltas 
	 */
	public void move() {
		
		float distanceToTarget = getDistanceFrom(_targetX, _targetY);
		float distance = _speed * _getElapsedTimeSinceLastMove();
		
		// don't calculating the move if we're close to our target 
		// and we can't move forther than the target
		if(distanceToTarget > 2 && distance < distanceToTarget) {	
			
			// calculate angle and x/y delta 
			// see note above for credit on this formula
			_calculateAngle();
			float deltaX = (float) Math.cos(_angle) * distance;
			float deltaY = (float) Math.sin(_angle) * distance;
			
			boolean movingRight = deltaX > 0;
			boolean movingDown = deltaY > 0;
	
			// only move if within bounds, or if entering bounds
			if((movingRight || _x + deltaX > _leftBound) && (!movingRight || _x + deltaX < _rightBound)) {
				_x += deltaX;
			}
			if((movingDown || _y + deltaY > _topBound) && (!movingDown || _y + deltaY < _bottomBound)) {
				_y += deltaY;
			}
		} else {
			// Since we're so close to the target, just move to it
			_x = _targetX;
			_y = _targetY;
		}
	}
		
	
	// see comment on move() method
	private void _calculateAngle() {
		_angle = (float) Math.atan2(_targetY - _y, _targetX - _x);
	}
	
	/**
	 * Set the boundary within which the sprite is allowed to move
	 * 
	 * @param top
	 * @param right
	 * @param bottom
	 * @param left
	 */
	public void setBounds(int top, int right, int bottom, int left) {
		_topBound = top;
		_rightBound = right;
		_bottomBound = bottom;
		_leftBound = left;
	}
	
	
	/**
	 * Set the sprite's movement speed
	 * 
	 * @param speed
	 */
	public void setSpeed(float speed) {
		_speed = speed;
	}
	
	
	/**
	 * Set the moving sprite's eventual target in the x,y coordinate space
	 * REduce "spazzing" around a small target by ignoring small changes 
	 * in target location
	 * 
	 * @param x
	 * @param y
	 */
	public void setTarget(float x, float y) {
		boolean changeX = Math.abs(_targetX - x) > 5;
		boolean changeY = Math.abs(_targetY - y) > 5;
		_targetX =  changeX ? x : _targetX;
		_targetY =  changeY ? y : _targetY;
		// update the angle to make the sprite face the new target
		if(changeX || changeY) {
			_calculateAngle();
		}
	}
	public void setTarget(Sprite sprite) {
		this.setTarget(sprite.getX(), sprite.getY());
	}
	
	public float getRandomXPointWithinBounds() {
		Random rand = new Random();
		return getLeftBound() + rand.nextFloat() * (getRightBound() - getLeftBound());
	}
	public float getRandomYPointWithinBounds() {
		Random rand = new Random();
		return getTopBound() + rand.nextFloat() * (getBottomtBound() - getTopBound());
	}
	
	public int getTopBound() {
		return _topBound;
	}
	public int getRightBound() {
		return _rightBound;
	}
	public int getBottomtBound() {
		return _bottomBound;
	}
	public int getLeftBound() {
		return _leftBound;
	}
	
	private float _getElapsedTimeSinceLastMove() {
		boolean first = (_lastMoveTime == -1);
		long elapsedTime = System.nanoTime() - _lastMoveTime;
		_lastMoveTime = System.nanoTime();
		return (first ? 0.0f : (float) elapsedTime / 1e9f);
	}
}
