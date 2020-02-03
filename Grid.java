import java.util.Random;

class Grid{
	int lines;
	int columns;
	int[][] grid;
	
	// tile constants
	// u,r,l,d are always in this order
	public static final int EM = 0; // empty
	public static final int UR = 1; // up right
	public static final int UL = 2;
	public static final int UD = 3;
	public static final int RL = 4;
	public static final int RD = 5;
	public static final int LD = 6;
	public static final int CR = 7; // cross

	public Grid(int lines, int columns){
		this.lines = lines;
		this.columns = columns;
		int i,j;
		grid = new int[lines][columns];
	}

	public void randomize(){
		for(int i=0;i<lines;i++)
			for(int j=0;j<columns;j++)
				grid[i][j] = (int)(Math.random() * 8);
	}

	public void randomize(Random gen){
		for(int i=0;i<lines;i++)
			for(int j=0;j<columns;j++)
				grid[i][j] = (int)(gen.nextDouble() * 8);
	}	

	public void printGrid(){
		print2DArray(grid);
		/*for(int i=0;i<lines;i++){
			for(int j=0;j<columns;j++)
				System.out.print(grid[i][j]+" ");
			System.out.println();
		}*/
	}
	
	public static void print2DArray(int[][] arr){
		for(int i=0;i<arr.length;i++){
			for(int j=0;j<arr[0].length;j++)
				System.out.print(arr[i][j] + " ");
			System.out.println();
		}
	}
		
	public static void main(String[] args){
		Random generator = new Random(12); // seeded RNG (to get the same random grid every time)
		Grid g = new Grid(4,6); // 4 lines 6 columns
		int[][] deg = new int[4][6];
		g.randomize(generator);
		System.out.println("Printing the grid:");
		g.printGrid();
		System.out.println("Degrees:");
		getDegrees(g,deg);
		print2DArray(deg);
	}
	
	/* degree : given the coordinates of a tile, Count how many tiles it
	is connected to. */

	boolean pointsDown(int x, int y){
		return grid[x][y] == CR || grid[x][y] == UD || grid[x][y] == RD || grid[x][y] == LD;
	}
	boolean pointsUp(int x, int y){
		return grid[x][y] == CR || grid[x][y] == UR || grid[x][y] == UL || grid[x][y] == UD;
	}
	boolean pointsLeft(int x, int y){
		return grid[x][y] == CR || grid[x][y] == UL || grid[x][y] == RL || grid[x][y] == LD;
	}
	boolean pointsRight(int x, int y){
		return grid[x][y] == CR || grid[x][y] == RL || grid[x][y] == UR || grid[x][y] == RD;
	}
	boolean connectsUp(int x, int y){
		return x > 0 && pointsUp(x,y) && pointsDown(x-1,y);
	}
	boolean connectsDown(int x, int y){
		return x < grid.length-1 && pointsDown(x,y) && pointsUp(x+1,y);
	}
	boolean connectsLeft(int x, int y){
		return y > 0 && pointsLeft(x,y) && pointsRight(x,y-1);
	}
	boolean connectsRight(int x, int y){
		return y < grid[0].length-1 && pointsRight(x,y) && pointsLeft(x,y+1);
	}

	/* degree : given the coordinates of a tile, Count how many tiles it
	is connected to. */
	public int degree(int x, int y){
		int counter = 0;
		counter += connectsRight(x,y) ? 1 : 0;
		counter += connectsLeft(x,y) ? 1 : 0;
		counter += connectsDown(x,y) ? 1 : 0;
		counter += connectsUp(x,y) ? 1 : 0;
		return counter;
	}

	public static void getDegrees(Grid g, int[][] degrees){
		for (int i = 0; i < degrees.length; i++)
			for (int j = 0; j < degrees[0].length; j++)
				degrees[i][j] = g.degree(i,j);		
	}

		
		

}

