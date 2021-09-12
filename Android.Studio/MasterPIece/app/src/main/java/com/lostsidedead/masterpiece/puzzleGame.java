package com.lostsidedead.masterpiece;

import android.util.Log;
import android.util.AttributeSet;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import java.util.Random;
import android.view.*;
import android.widget.*;

public class puzzleGame extends MpView implements View.OnTouchListener {
	public static final String TAG = "puzzleGame";
	public static final String MP_STR="MP.DEMO";
	public final int BLOCK_NULL = -1, BLOCK_FADE = -2;
	public final int START = 1;
	public final int GAME = 2, NUM_BLOCKS=7, GAME_OVER=3;
	public int game_mode = START;
	public Random randGen = new Random();
	public static int GAME_DELAY=200;
	public boolean paused = false;
	public TextView txtview;
	public Spinner spin_ctrl;
	
	public void setPaused(boolean b) {
		paused = b;
	}
	
	class Block {
		public int x,y;
		public int colors[] = new int[3];
		
		public void newBlock() {
			x = (game_grid.MAX_W/2);
			y = 0;
			do {
				colors[0] = randGen.nextInt(NUM_BLOCKS);
				colors[1] = randGen.nextInt(NUM_BLOCKS);
				colors[2] = randGen.nextInt(NUM_BLOCKS);
			} while(colors[0] == colors[1] && colors[0] == colors[2]);
		}
		
		public void shiftColors() {
			int temp_colors[] = new int[3];
			temp_colors[0] = colors[0];
			temp_colors[1] = colors[1];
			temp_colors[2] = colors[2];
			colors[0] = temp_colors[2];
			colors[1] = temp_colors[0];
			colors[2] = temp_colors[1];
		}
	}
	
	public void setMode(int mode) {
		game_mode = mode;
		update();
	}
	
	class Grid {
		
		public int blockSize = 24;
		private int MAX_W = 12;
		private int MAX_H = blockSize;
		private int score=0;
		private int lines=0;
		public int grid[][];
		public int fade_grid[][];
		public Block gblock = new Block();
		
		boolean active=false;
		
		public Grid() {
			active=false;
			score = 0;
		}
		
		public void resizeGrid(int nw, int nh) {
			grid = new int [nw][nh];
			MAX_W = nw;
			MAX_H = nh;
			fade_grid = new int[nw][nh];
		}
		
		public void initGrid() {
			for(int i = 0; i < MAX_W; ++i) {
				for(int z = 0; z < MAX_H; ++z) {
					grid[i][z] = BLOCK_NULL;
				}
			}
			gblock.newBlock();
			score = 0;
			active=true;
				
		
			lines=0;
		}
		
		public int proxGrid(int i, int z) {
			if(i > MAX_W-1 || z > MAX_H-1) return BLOCK_NULL;
			return grid[i][z];
		}
		
		public void moveDown() {
			if(active == false) return;
			
			
			if(gblock.x >= 0 && (gblock.y+3 < MAX_H-1)) {	
				if(grid[gblock.x][gblock.y+3] != BLOCK_NULL) {
					mergeBlock();
					return;
				}	
			}
			
					
			if(gblock.y+2 < MAX_H-1)
				gblock.y++;
			else 
				mergeBlock();
		}
		
		public void mergeBlock() {
				if(gblock.y <= 2) {
				setMode(GAME_OVER);	
				spin_ctrl.setVisibility(Spinner.VISIBLE);
				return;
			}
			
			grid[gblock.x][gblock.y] = gblock.colors[0];
			grid[gblock.x][gblock.y+1] = gblock.colors[1];
			grid[gblock.x][gblock.y+2] = gblock.colors[2];
			gblock.newBlock();
		}
		
		public void moveLeft() {
			if(gblock.x >= 1 && proxGrid(gblock.x-1, gblock.y+2) != BLOCK_NULL)
				return;
				
			if(gblock.x > 0) 
				gblock.x--;		
		}
		
		public void moveRight() {
					
			if(gblock.x < MAX_W-2 && proxGrid(gblock.x+1, gblock.y+2) != BLOCK_NULL)
				return;
			
			if(gblock.x < MAX_W-1)
				gblock.x++;
		}
		
		public void drawGrid(Canvas c) {
			
			if(txtview.getText() != "")
				txtview.setText("");
			
			if(active==false) return;
			int offset_x=0;
			int offset_y=45;
			for(int i = 0; i < MAX_W; ++i) {
				for(int z = 0; z < MAX_H; ++z) {
					if(grid[i][z] >= 0)
						c.drawBitmap(bits[grid[i][z]],offset_x+(i*blockSize), offset_y+(z*blockSize),painter);
					else if(grid[i][z] == BLOCK_FADE) {
						int num = randGen.nextInt(7);
						c.drawBitmap(bits[num], offset_x+(i*blockSize), offset_y+(z*blockSize), painter);
						if(fade_grid[i][z] > 4) {
							fade_grid[i][z] = 0;
							grid[i][z] = BLOCK_NULL;
						}
						fade_grid[i][z] ++;				
					}
				}
			}
			
			painter.setARGB(255, 0, 0, 0);
			c.drawRect(0, 5, 100, 40, painter);
			paint.setARGB(255, 0, 0, 255);
			String str="Score: " + score;
			if(paused == true) str = "Paused";
			c.drawText(str, 5, 25, paint);
			int cx = gblock.x;
			int cy = gblock.y;
			c.drawBitmap(bits[gblock.colors[0]], offset_x+(cx*blockSize), offset_y+(cy*blockSize), painter);
			++cy;
			c.drawBitmap(bits[gblock.colors[1]], offset_x+(cx*blockSize), offset_y+(cy*blockSize), painter);
			++cy;
			c.drawBitmap(bits[gblock.colors[2]], offset_x+(cx*blockSize), offset_y+(cy*blockSize), painter);
		}
		
		public void update() {
			if(paused == false && active == true) {
				if(left_active == true) {
					moveLeft();
					left_active = false;
				}
				if(right_active == true) {
					moveRight();
					right_active = false;
				}
				moveDown();
				procBlocks();
			}
		}
		
		public void checkFor_moveDown() {
			for(int i = 0; i < MAX_W; ++i) {
				for(int z = 0; z < MAX_H-1; ++z) {
					if(grid[i][z] >= 0 && grid[i][z+1] == BLOCK_NULL) {
						grid[i][z+1] = grid[i][z];
						grid[i][z] = BLOCK_NULL;
					}
				}
			}
		}
		
		
		public void increaseSpeed() {
			if(GAME_DELAY > 100)
			GAME_DELAY-=50;
		}
		
		
		public void addScore(int n) {
			score += n;
			lines ++;
			if((lines % 14) == 0)
				increaseSpeed();
		}
		
		public void procBlocks() {
			
			checkFor_moveDown();
			for(int i = 0; i < MAX_W-2; ++i) {
				for(int z = 0; z < MAX_H; ++z) {
					int cur_color = grid[i][z];
					if(z+2 < MAX_H && cur_color >= 0 && cur_color == grid[i][z+1] && cur_color == grid[i][z+2]) {
						grid[i][z] = BLOCK_FADE;
						grid[i][z+1] = BLOCK_FADE;
						grid[i][z+2] = BLOCK_FADE;
						addScore(3);
						if(z+3 < (MAX_H-1) && cur_color == grid[i][z+3]) {
							grid[i][z+3] = BLOCK_FADE;
							score += 1;
						}
						continue;
					}
					if(i+2 < MAX_W && cur_color >= 0 && cur_color == grid[i+1][z] && cur_color == grid[i+2][z]) {
						grid[i][z] = BLOCK_FADE;
						grid[i+1][z] = BLOCK_FADE;
						grid[i+2][z] = BLOCK_FADE;
						addScore(3);
						
						if(i+3 < (MAX_W-1) && cur_color == grid[i+3][z]) {
							grid[i+3][z] = BLOCK_FADE;
							score += 1;
						}
						continue;
					}
					
					if(i+2 < MAX_W && z+2 < MAX_H) {
						if(cur_color >= 0 && cur_color == grid[i+1][z+1] && cur_color == grid[i+2][z+2]) {
							grid[i][z] = BLOCK_FADE;
							grid[i+1][z+1] = BLOCK_FADE;
							grid[i+2][z+2] = BLOCK_FADE;
							addScore(3);
							continue;
						}
					}
					if(i-2 >= 0 && z-2 >= 0)
					{
						if(cur_color >= 0 && cur_color == grid[i-1][z-1] && cur_color == grid[i-2][z-2]) {
							grid[i][z] = BLOCK_FADE;
							grid[i-1][z-1] = BLOCK_FADE;
							grid[i-2][z-2] = BLOCK_FADE;
							addScore(3);
							continue;
						}
					}
					
					if(z-2 >= 0 && i+2 < MAX_W) {
						if(cur_color >= 0 && cur_color == grid[i+1][z-1] && cur_color == grid[i+2][z-2]) {
							grid[i][z] = BLOCK_FADE;
							grid[i+1][z-1] =  BLOCK_FADE;
							grid[i+2][z-2] = BLOCK_FADE;
							addScore(3);
							continue;
						}
					}
					
					if(i-2 >= 0 && z+2 < MAX_H) {
						if(cur_color >= 0 && cur_color == grid[i-1][z+1] && cur_color == grid[i-2][z+2]) {
							grid[i][z] = BLOCK_FADE;
							grid[i-1][z+1] = BLOCK_FADE;
							grid[i-2][z+2] = BLOCK_FADE;
							addScore(3);
							continue;
						}
					}
					
				}
			}
			
		}
	}
	
	private Bitmap bits[];
	private Bitmap arrows[];
	private Bitmap logo;
	
	private Paint painter = new Paint();
	public Grid game_grid = new Grid();
	public int scr_width=0,scr_height=0;
	
	
	
	class updateHandler extends Handler {

	  @Override
        public void handleMessage(Message msg) {
            puzzleGame.this.update();
            puzzleGame.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
	}
	
	private updateHandler update = new updateHandler();
	
	public Bitmap background = null;
	
	public puzzleGame(Context context, AttributeSet attrs) {
		super(context, attrs);	
		Resources r = this.getContext().getResources();
		bits = new Bitmap[10];
		arrows = new Bitmap[3];
	
		logo = loadImage(r.getDrawable(R.drawable.mp), 353, 285);
		bits[0] = loadImage(r.getDrawable(R.drawable.block1));
		bits[1] = loadImage(r.getDrawable(R.drawable.block2));
		bits[2] = loadImage(r.getDrawable(R.drawable.block3));
		bits[3] = loadImage(r.getDrawable(R.drawable.block4));
		bits[4] = loadImage(r.getDrawable(R.drawable.block5));
		bits[5] = loadImage(r.getDrawable(R.drawable.block6));
		bits[6] = loadImage(r.getDrawable(R.drawable.block7));
		bits[7] = loadImage(r.getDrawable(R.drawable.block8));
		arrows[0] = loadImage(r.getDrawable(R.drawable.left), 60, 60);
		arrows[1] = loadImage(r.getDrawable(R.drawable.right), 60, 60);
		arrows[2] = loadImage(r.getDrawable(R.drawable.up), 60, 60);
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		setOnTouchListener(this);
	}

	@Override
	public void onSizeChanged(int w, int h, int ow, int oh) {
		if(game_grid.active == false) {
			game_grid.resizeGrid(w/game_grid.blockSize, ((h-100)/game_grid.blockSize)-2);
			game_grid.initGrid();
			scr_width = w;
			scr_height = h;
			game_grid.active=true;
			Resources r = this.getContext().getResources();
			background = loadImage(r.getDrawable(R.drawable.bg), scr_width, scr_height);
			
		}
	}
	
	public void setPosition(int x, int y) {
		if(x >= 0 && x < game_grid.MAX_W && y >= 0 && y+2 < game_grid.MAX_H) {
			
			if(game_grid.grid[x][y] == BLOCK_NULL && game_grid.grid[x][y+1] == BLOCK_NULL && game_grid.grid[x][y+2] == BLOCK_NULL)	{
				game_grid.gblock.x = x;
			}
		}
	}

	public boolean left_active, right_active;
	
	public boolean onTouch(View v, MotionEvent e) {
		if(game_mode == GAME) {
			
			if(e.getX() > (scr_width-60)/2 && e.getX() < ((scr_width-60)/2)+60 && e.getY() > scr_height-60 && e.getY() < scr_height) {
				userControls(MOVE_SHIFT);
				return false;
			}
			
			if(e.getX() > 50 && e.getX() < 110 && e.getY() > scr_height-60 && e.getY() < scr_height) {
				left_active = true;	
				return true;
			}
			else
				left_active = false;
			
			if(e.getX() > scr_width-110 && e.getX() < (scr_width-50) && e.getY() > scr_height-60 && e.getY() < scr_height) {
				right_active = true;
				return true;
			} else {
				right_active = false;
			}
			
			int offset_x=0;
			int offset_y=45;
			
			for(int i = 0; i < game_grid.MAX_W; ++i) {
				for(int z = 0; z < game_grid.MAX_H; ++z) {
					int cx = offset_x+(i*game_grid.blockSize);
					int cy = offset_y+(z*game_grid.blockSize);
					if(cx >= e.getX() && cx <= e.getX()+game_grid.blockSize && cy >= e.getY() && cy < e.getY()+game_grid.blockSize) {
						setPosition(i,z);
						return true;
					}
				}
			}
			
			
		} else if(game_mode == START || game_mode == GAME_OVER) {
			int logo_x, logo_y;
			logo_x = (scr_width/2)-(logo.getWidth()/2);
			logo_y = (scr_height/2)-(logo.getHeight()/2);
			if(e.getX() > logo_x && e.getX() < logo_x+logo.getWidth() && e.getY() > logo_y && e.getY() < logo_y+logo.getHeight()) {
				setMode(GAME);
				game_grid.initGrid();
				spin_ctrl.setVisibility(Spinner.INVISIBLE);
				int pos = spin_ctrl.getSelectedItemPosition();
				GAME_DELAY=300;
				switch(pos) { 
				case 0:
					GAME_DELAY=450;
					break;
				case 1:
					GAME_DELAY=300;
					break;
				case 2:
					GAME_DELAY=200;
					break;
				}
			}
		}
		
		return false;
	}
	
	public void drawScreen(Canvas canvas) {
		game_grid.drawGrid(canvas);
		canvas.drawBitmap(arrows[0], 50, scr_height-60, paint);
		canvas.drawBitmap(arrows[1], scr_width-110, scr_height-60, paint);
		canvas.drawBitmap(arrows[2], (scr_width-60)/2, scr_height-60, paint);
	}

	public void drawStart(Canvas canvas) {
		paint.setARGB(255,255,255,255);
		if(txtview.getText() != "Tap Logo to Start") txtview.setText("Tap Logo to Start");
		//canvas.drawText("Tap Logo to Start", 5, scr_height-50, paint);
		canvas.drawBitmap(logo, (scr_width/2)-(logo.getWidth()/2), (scr_height/2)-(logo.getHeight()/2), paint);
	}
	
	public void drawOverScreen(Canvas can) {
		paint.setARGB(255,255,255,255);
		//can.drawText("Game Over, your score: " + game_grid.score,25,scr_height-50, paint);
		txtview.setText("Game Over, your score: " + game_grid.score + "\nvisit http://lostsidedead.com");
		can.drawBitmap(logo, (scr_width/2)-(logo.getWidth()/2), (scr_height/2)-(logo.getHeight()/2), paint);
	}
	
	public static final int MOVE_LEFT=1, MOVE_RIGHT=2, MOVE_DOWN=3, MOVE_SHIFT=4;
	
	public void userControls(int direction) {
	
		switch(direction) {
		case MOVE_SHIFT:
			game_grid.gblock.shiftColors();
			break;
		case MOVE_LEFT:
			game_grid.moveLeft();
			break;
		case MOVE_RIGHT:
			game_grid.moveRight();
			break;
		}
		
	}
	
	
	
	public Bitmap loadImage(Drawable d) {
		return loadImage(d, game_grid.blockSize,game_grid.blockSize);
	}
	
	public Bitmap loadImage(Drawable d, int w, int h) {
		Bitmap newImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas can = new Canvas(newImage);
		d.setBounds(0, 0, w, h);
		d.draw(can);
		return newImage;
	}
	
	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		
		if(background != null)
			c.drawBitmap(background, 0, 0, paint);
		
		switch(game_mode) {
		case START: 
			drawStart(c);
			break;
		case GAME:
			drawScreen(c);
			break;
		case GAME_OVER:
			drawOverScreen(c);
			break;
		}
	}
	
	
	
	public void update() {
		update.sleep(GAME_DELAY);
		if(game_mode == GAME) game_grid.update();
	}
}
