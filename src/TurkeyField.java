/**
 * TurkeyField.java
 * 
 * The TurkeyField is the field of play for the game. It passes messages between
 * the Farmer and the Turkeys. It also picks up the commands from the mouse and
 * does the appropriate action. This is the class that will have the main method
 * to start the game.
 * 
 * @authors Jerome Dane, Sandra Poulos 
 * @compids jd7yj, sp5uk 
 * @lab 1111
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class TurkeyField {
	
	public static String version = "0.1.0";

	// give ourselves a global flag for use in turning debugging on and off
	private boolean _inDebugMode = false;
	
	// global configuration that doesn't change
	private int _canvasSize = 750;
	private int _topFieldBound = 50;
	private int _rightFieldBound = 650;
	private int _bottomFieldBound = 650;
	private int _leftFieldBound = 50;
	private int _maxMessageDurationInSeconds = 6;
	private SimpleCanvas canvas;

	// things that will change
	private Farmer _farmer;
	private float _farmerSpeed;
	private float _turkeySpeed;
	private int _numStartingTurekeys;
	private ArrayList<Turkey> _turkeys;
	private int _maxNumTurkeys;
	private Sprite _title;
	private Sprite _titleZombies;
	private Sprite _background;
	private Sprite _gameOver;
	private Sprite _youWin;
	private long _startTime;
	private int _secondsPassed;
	private long _lastMessageTimeInSeconds;
	private int _gameStartTimeInSeconds;
	private String _message;
	private Font _messageFont, _regularFont;
	private Color _defaultDrawColor;
	private int _secondsBetweenTurkeysMin;
	private int _secondsBetweenTurkeysMax;
	private int _secondsBetweenTurkeys;
	private int _numTurkeysToWin;
	private float _infectionChance;
	private int _numWins;
	
	/**
	 * The Constructor - This method should instantiate a new canvas, create a
	 * new player character, and create the first four turkeys in random
	 * locations around the board.
	 */
	public TurkeyField() {
		_numWins = 0;
		_messageFont = new Font("Arial", 1, 16);
		_regularFont = new Font("Arial", 0, 13);
		// set up initial sprites
		float center = _canvasSize / 2;
		_background = new Sprite(0, 0, "images/background.gif");
		_title = new Sprite(_leftFieldBound, 10, "images/title.png");
		_titleZombies = new Sprite(_leftFieldBound, 10, "images/title_zombies.png");
		_gameOver = new Sprite(center, center - 20, "images/game_over.png");
		_youWin = new Sprite(center - 40, center - 20, "images/you_win.png");
		_defaultDrawColor = new Color(0xffffff);
		reset();	
	}
	
	/**
	 * Reset the game and start over
	 */
	public void reset() {
		
		// set the starting number of turkeys
		_numStartingTurekeys = 5 - _numWins;
		
		// set chance new turkeys will already be infected
		_infectionChance = (float) (0.1 + (.05 * _numWins));
		
		// set the number of turkeys required to win
		_numTurkeysToWin = 10 + (5 * _numWins);
		
		// set up initial farmer and turkey speeds
		_farmerSpeed = 55;
		_turkeySpeed = 20;
		
		// define the max number of total turkeys that can be on the field
		_maxNumTurkeys = 30 + (5 * _numWins);
		
		// define an initial number of seconds between turkeys
		_secondsBetweenTurkeys = 10;
		
		// set up the minimum and maximum time betweeen turkeys for use in randomness
		_secondsBetweenTurkeysMin = 2;
		_secondsBetweenTurkeysMax = 20;
		
		// tell the game it hasn't started yet
		_gameStartTimeInSeconds = -1;

		_turkeys = new ArrayList<>();
		
		// Create canvas object with 500x500 spatial dimensions.
		canvas = new SimpleCanvas(_canvasSize, _canvasSize, this);

		float center = _canvasSize / 2; 
		
		// add farmer to field and tell him where he can go, as well as inf about the turkeys
		_farmer = new Farmer(center, center, _farmerSpeed);
		_farmer.setBounds(_topFieldBound, _rightFieldBound, _bottomFieldBound, _leftFieldBound);
		_farmer.see(_turkeys);
		_farmer.see(this);
		if(_inDebugMode) {
			_farmer.enableDebugMode();
		}
		
		// clear any messages
		tellPlayer("");
		
		_secondsPassed = 0;
		_startTime = System.currentTimeMillis();
	}
	
	public Farmer getFarmer() {
		return _farmer;
	}
	
	protected float _getRandomCanvasRange() {
		Random rand = new Random();
		return 50 + (_canvasSize - 100) * rand.nextFloat();
	}
	
	/** 
	 * Build a turkey, give it some randomness, and do other neat turkey makin' stuff
	 * 
	 * @return A fully functional turkey
	 */
	protected Turkey _buildTurkey() {
		Random rand = new Random();
		float sideTrigger = rand.nextFloat();
		
		// generate a random turkey speed multiplier (.7 to 1.3)
		float speedMultiplier = (float) (.7 + rand.nextFloat() * .6);
				
		// create a turkey - some turkeys are a little faster or slower than others
		Turkey turkey = new Turkey(_turkeySpeed * speedMultiplier);
		
		// The turkey sees the farmer so it can react to him
		turkey.see(_farmer);
		
		// The turkey sees all the turkeys on the field so it can react to them
		turkey.see(_turkeys);
		
		// set an initial image
		turkey.setImageFromDirection("S");
		
		if(_inDebugMode) {
			turkey.enableDebugMode();
		}
		
		// tell the turkey where it can go and generate a random starting point on an edge
		turkey.setBounds(_topFieldBound, _rightFieldBound, _bottomFieldBound, _leftFieldBound);

		return turkey;
		
	}
	
	/**
	 * This method should control all of your mouse actions. The mouse activity
	 * is picked up by the SimpleCanvas and then it should call this method,
	 * passing either the button that was pressed or some other flag.
	 */
	public void mouseAction(float x, float y, int button) {

		// Set the farmer's target to the current mouse location (if it's worth it)
		if(_farmer.getDistanceFrom(x, y) > 5) {
			_farmer.setTarget(x, y);
		}

		// No mouse button click
		if (button == -1) {
			// Keep calm and carry on 
		}

		// Left mouse button click
		if (_gameStartTimeInSeconds != -1 && _secondsPassed > _gameStartTimeInSeconds && button == 1) {
			
			boolean atBottomOfScreen = y > _bottomFieldBound;
			
			if(atBottomOfScreen && _farmer.getNumTurkeysCaught() >= _numTurkeysToWin) {
				
				// make the game harder and restart if the game was won
				_numWins++;
				reset();
			
			} else if(atBottomOfScreen && _farmer.isDead()){
				
				// reset the game if the farmer is dead
				reset();
				
			} else {
				
				// Try to catch a turkey
				_farmer.tryToCatchATurkey();
				
			}
			
		}

		// right mouse button click
		if (button == 3) {
			
			// make the farmer sprint
			_farmer.sprint() ;
		}
	}
	
	/**
	 * This is the main drawing function that is automatically called whenever
	 * the canvas is ready to be redrawn. The 'elapsedTime' argument is the
	 * time, in seconds, since the last time this function was called.
	 * 
	 * Other things this method should do: - draw the turkeys and the farmer on
	 * the screen - tell the turkeys and farmer to move - check to see if the
	 * game is over after they move - halt the game if the game is over - update
	 * the number of ticks by 1 - add a new turkey every 20000 ticks
	 */
	public void draw(Graphics2D g, float elapsedTime) {
		
		// check for no more healthy turkeys
		if(_getNumZombies() == _maxNumTurkeys) {
			_farmer.makeDead("There are no more healthy turkeys in the world. Click HERE to retry.");
		}
		
		boolean gameWon = _farmer.getNumTurkeysCaught() >= _numTurkeysToWin;
		
		// draw the background
		_background.draw(g);
		
		// draw the title
		if(_getNumZombies() == 0) {
			_title.draw(g);
		} else {
			_titleZombies.draw(g);
		}
		
		if(!gameWon) {
		
			// update timer stuff if the farmer is still alive
			if(!_farmer.isDead()) {
				int secondsPassed = (int) ((System.currentTimeMillis() - _startTime) / 1000);
				// if a whole second has passed, increment the number of seconds passed in the game and tell the story
				if(secondsPassed > _secondsPassed) {
					_secondsPassed++;
					_advanceStory();
				}
			} else {
				// tell the player how the farmer died
				_gameOver.drawCentered(g);
				tellPlayer(_farmer.getDeathMessage());
			}
	
			// Let the farmer move and show the game timer, etc. if the game has started
			if(_gameStartTimeInSeconds != -1 && _secondsPassed >= _gameStartTimeInSeconds) {
				
				// move the farmer if he'a still alive
				if(!_farmer.isDead()) {
					_farmer.move();
				}
				
				// restore the regular font
				g.setFont(_regularFont);
				
				// draw game timer timer
				
				_drawGameTimer(g);
				
				// draw turkey catching progress
				g.drawString("You caught " + _farmer.getNumTurkeysCaught() + " of " + _numTurkeysToWin + " turkeys", _rightFieldBound - 180, 30);
				
			} else {
				// make the farmer face south because we're in the intro
				_farmer.setTarget(_farmer.getX(), _farmer.getY() + 1);
			}
			
			if(!_farmer.isDead()) {
				_farmer.drawCentered(g);
			}
	
			// move and draw all the turkeys if the farmer is still alive
			if(!_farmer.isDead()) {
				for (Turkey turkey : _turkeys) {
					turkey.move();
					turkey.drawCentered(g);
				}
			}
		} else {
			
			// the game has been won!
			
			tellPlayer("Click HERE to play again with a little more challenge");
			_drawGameTimer(g);
			_youWin.drawCentered(g);
			
		}
		
		// draw message to player
		g.setColor(_defaultDrawColor);
		g.setFont(_messageFont);
		g.drawString(_message, _leftFieldBound, _bottomFieldBound + 45);
		
	}
	
	private int _getNumZombies() {
		int numZombies = 0;
		for(Turkey turkey : _turkeys) {
			if(turkey.isZombie()) {
				numZombies++;
			}
		}
		return numZombies;
	}
	private void _drawGameTimer(Graphics2D g) {
		String timerStr = "Time: " + _formatSecondsAsMMSS(_secondsPassed - _gameStartTimeInSeconds);
		g.drawString(timerStr, _rightFieldBound - 60, _bottomFieldBound + 40);
	}

	/**
	 * Advance the game story based on the number seconds that have passed
	 * 
	 */
	private void _advanceStory() {
		// clear any old messages
		if(_secondsPassed - _lastMessageTimeInSeconds > _maxMessageDurationInSeconds) {
			tellPlayer("");
		}
		
		switch ((int) _secondsPassed) {
			case 1:
				tellPlayer("Turkey Farmer v" + TurkeyField.version + " by Jerome Dane - http://goo.gl/WjDfM");
				break;
			case 5:
				tellPlayer("Catch " + _numTurkeysToWin + " turkeys for Thanksgiving dinner.");
				break;
			case 9:
				tellPlayer("Be careful! There's a strange illness going around.");
				break;
			case 14:
				tellPlayer("");
				break;
			case 15:
				// add the starting number of healthy turkeys to the field
				for(int i = 0; i < _numStartingTurekeys; i++) {
					_turkeys.add(_buildTurkey());
				}
				// set the game's actual start time
				_gameStartTimeInSeconds = _secondsPassed;
				break;
			case 18:
				// give the user a hint if they haven't caught anything after 3 seconds
				if(_farmer.getNumTurkeysCaught() == 0) {
					tellPlayer("Left-click while near a turkey to catch it!");
				}
				break;
			case 21:
				// give the user a hint if they haven't caught anything after 3 seconds
				tellPlayer("Right-click to sprint.");
				break;
			case 25:
				tellPlayer("");
				break;
			case 26:
				tellPlayer("Catch the sick turkey before something bad happens!");
				break;
			case 27:
				// add a sick turkey with a low incubation time so that it's likely they'll miss it
				Turkey turkey = _buildTurkey();
				turkey.infect(4);
				// make it hard to catch the first sick turkey by starting it on the farthest side
				switch(_farmer.getClosestSide()) {
					case "top":
						turkey.setY(_bottomFieldBound);
						break;
					case "right":
						turkey.setX(_leftFieldBound);
						break;
					case "bottom":
						turkey.setY(_topFieldBound);
						break;
					case "left":
						turkey.setX(_rightFieldBound);
						break;
				}
				_turkeys.add(turkey);
		}
		
		// if we're past the intro  and there aren't already too any turkeys, start adding them normally
		if(_secondsPassed > 30 && _turkeys.size() < _maxNumTurkeys && _secondsPassed % _secondsBetweenTurkeys == 0) {
				
			Turkey turkey = _buildTurkey();
			
			// check to see if it's already infected
			Random rand = new Random();
			if(rand.nextFloat() < _infectionChance) {
				// if it's infected, give it a random incubation time from 5 to 20 seconds
				turkey.infect(5 + (int) (15 * rand.nextFloat()));
			}
			
			// add the turkey to the field
			_turkeys.add(turkey);
			
			// update the number of seconds until the next turkey to add some variety
			_secondsBetweenTurkeys = _secondsBetweenTurkeysMin + 
					(int) (rand.nextFloat() * (_secondsBetweenTurkeysMax - _secondsBetweenTurkeysMin));
			
		}
	}
	
	public void tellPlayer(String message) {
		_message = message;
		_lastMessageTimeInSeconds = _secondsPassed;
	}
	
	private String _formatSecondsAsMMSS(int secondTotal) {

		int minutes = (int) Math.floor(secondTotal / 60);
		int seconds = secondTotal % 60;
	
		return (
				(minutes < 10 ? "0" : "") + minutes
				+ ":" + (seconds< 10 ? "0" : "") + seconds 
			);

	}
	
	
	

	/**
	 * Your standard main method. Nothing for you to change here.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TurkeyField simulator = new TurkeyField();
		simulator.play();
	}

	/**
	 * This method starts the game. Nothing for you to change here.
	 */
	public void play() {
		canvas.setupAndDisplay();
	}
}
