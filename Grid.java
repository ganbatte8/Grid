import java.util.Random;


class PathNode{
	PathNode[] children = {null, null, null, null}; // urld
	PathNode parent;
	int x;
	int y;
	int depth;

	public PathNode(){}	
	public PathNode(PathNode parent){
		this.parent = parent;	
	}
	public PathNode(int x, int y, PathNode parent, int depth){
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.depth = depth;
	}

	public void add(int x, int y, int index){
		children[index] = new PathNode(this);
		children[index].x = x;
		children[index].y = y;
	}
}

class Grid{
	
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


	public static void randomize2DArray(int[][] arr){
		for(int i=0; i<arr.length; i++)
			for(int j=0; j<arr[0].length; j++)
				arr[i][j] = (int)(Math.random() * 8);
	}

	public static void randomize2DArray(int[][] arr, Random gen){
		for(int i=0; i<arr.length; i++)
			for(int j=0; j<arr[0].length; j++)
				arr[i][j] = (int)(gen.nextDouble() * 8);
	}	

	
	public static void print2DArray(int[][] arr){
		for(int i=0;i<arr.length;i++){
			for(int j=0;j<arr[0].length;j++)
				System.out.print(arr[i][j] + " ");
			System.out.println();
		}
	}

	public static void copy2DArray(int[][] from, int[][] to){
		for (int i=0; i<from.length; i++)
			for (int j=0; j<from[0].length; j++)
				to[i][j] = from[i][j];
	}
		
	public static void main(String[] args){
		Random rng = new Random(20); // seeded RNG (to get the same random grid every time)
		int i,j;
		int lines = 20;
		int columns = 50;
		// if lines = 20, columns = 50 :
		// seed 2 has a cycle of length 8
		// seed 3 has a cycle of length 6
		// seed 12 has a cycle of length 10
		int[][] g = new int[lines][columns]; // grid
		int[][] deg = new int[lines][columns]; // degrees
		int[][] colors = new int[lines][columns]; // colors for connex components
		int[][] h = new int[lines][columns]; // grid copy
		PathNode n = new PathNode(); // tile tree for memorizing paths
		randomize2DArray(g, rng);
		System.out.println("Printing the grid:");
		print2DArray(g);
		// I want to make a copy of g and filter the dead ends. This should get rid of all the trees.
		// The remaining connex components are those that have a cycle.
		// We may still have bridges though, so not every remaining tile will be part of a cycle.
		copy2DArray(g,h);
		for (i = 0; i < h.length; i++)
			for (j = 0; j < h[0].length; j++)
				filterLowDegree(h,i,j);
		System.out.println("Removed dead ends :");			
		print2DArray(h);
		System.out.println("Degrees:");
		setDegrees(h,deg);
		print2DArray(deg);
		System.out.println("Coloring connex components :");
		colorConnexComponents(h, colors); 
		print2DArray(colors);
		// DFS : build PathTree from a certain root tile, get deepest node that connects back to the starting point
	}
	
	
	// functions testing a tile
	static boolean pointsDown(int[][] arr, int x, int y){
		return arr[x][y] == CR || arr[x][y] == UD || arr[x][y] == RD || arr[x][y] == LD;
	}
	static boolean pointsUp(int[][] arr, int x, int y){
		return arr[x][y] == CR || arr[x][y] == UR || arr[x][y] == UL || arr[x][y] == UD;
	}
	static boolean pointsLeft(int[][] arr, int x, int y){
		return arr[x][y] == CR || arr[x][y] == UL || arr[x][y] == RL || arr[x][y] == LD;
	}
	static boolean pointsRight(int[][] arr, int x, int y){
		return arr[x][y] == CR || arr[x][y] == RL || arr[x][y] == UR || arr[x][y] == RD;
	}
	static boolean connectsUp(int[][] arr, int x, int y){
		return x > 0 && pointsUp(arr,x,y) && pointsDown(arr,x-1,y);
	}
	static boolean connectsDown(int[][] arr, int x, int y){
		return x < arr.length-1 && pointsDown(arr,x,y) && pointsUp(arr,x+1,y);
	}
	static boolean connectsLeft(int[][] arr, int x, int y){
		return y > 0 && pointsLeft(arr,x,y) && pointsRight(arr,x,y-1);
	}
	static boolean connectsRight(int[][] arr, int x, int y){
		return y < arr[0].length-1 && pointsRight(arr,x,y) && pointsLeft(arr,x,y+1);
	}

	/* degree : given the coordinates of a tile, Count how many tiles it
	is connected to. */
	public static int degree(int[][] arr, int x, int y){
		return (connectsRight(arr,x,y)?1:0) + (connectsLeft(arr,x,y)?1:0) + (connectsDown(arr,x,y)?1:0) + (connectsUp(arr,x,y)?1:0);
	}

	public static void setDegrees(int[][] from, int[][] to){
		for (int i = 0; i < from.length; i++)
			for (int j = 0; j < from[0].length; j++)
				to[i][j] = degree(from, i, j);		
	}

	public static void filterLowDegree(int[][] arr, int x, int y){
		// if degree is 1, clear the tile and repeat on the tile it connects to
		int nextX = x;
		int nextY = y;
		while (degree(arr,x,y) == 1){
			if (connectsRight(arr,x,y))
				nextY++;
			else if (connectsLeft(arr,x,y))
				nextY--;
			else if (connectsUp(arr,x,y))
				nextX--;
			else
				nextX++;
			arr[x][y] = 0;
			x = nextX;
			y = nextY;
		}
		// if degree is 0 wherever we are, clear the tile
		if (degree(arr,x,y) == 0)
			arr[x][y] = 0;
	}
	

	/* colorConnexComponent : color tile x,y as well as all the tiles from the same connex component.
	 * Precondition : grid[x][y] != 0*/
	public static void colorConnexComponent(int[][] grid, int[][] mark, int color, int x, int y){	
		mark[x][y] = color;
		if (connectsUp(grid,x,y) && mark[x-1][y] != color)
			colorConnexComponent(grid, mark, color, x-1, y);
		if (connectsRight(grid,x,y) && mark[x][y+1] != color)
			colorConnexComponent(grid, mark, color, x, y+1);
		if (connectsDown(grid,x,y) && mark[x+1][y] != color)
			colorConnexComponent(grid, mark, color, x+1, y);
		if (connectsLeft(grid,x,y) && mark[x][y-1] != color)
			colorConnexComponent(grid, mark, color, x, y-1);
	}
	
	/* colorConnexComponents : color all the connex components with different colors (integers).
	 * Precondition : mark is a 2D array of zeroes, same dimension as grid */ 
	public static void colorConnexComponents(int[][] grid, int[][] mark){
		int color = 1;
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[0].length; j++){
				if (mark[i][j] == 0 && grid[i][j] != 0){
					colorConnexComponent(grid, mark, color, i, j);
					color++;
				}
			}
		}
	}

	public static void dfs(int[][] grid, int x, int y){
		// intilialize grid of zeroes
		int maxCycleLength = 0;
		int currentCycleLength = 0;
		int[][] mark = new int[grid.length][grid[0].length];

		// label x,y as discovered
		// for all adjacent tiles that are not labeled as discovered, recursively call on vertex w

		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[0].length; j++){
				;
			}
		}
	}	
	
	/* dfsRecursion : build a path tree from the root tile (x,y) and memorize the (any) deepest node connecting back to the starting point.
	initial call : dfsRecursion(grid, mark, i, j, 0, rootNode, null)*/
	public static void dfsRecursion(int[][] grid, int[][] mark, int x, int y, int depth, PathNode node, PathNode deepest){
		mark[x][y] = 1;
		depth++;

		if (connectsRight(grid, x, y)){
			if (mark[x][y+1] == 0){ // unmarked
				node.children[1] = new PathNode(x, y+1, node, depth);
				dfsRecursion(grid, mark, x, y+1, depth, node.children[1], deepest);
			}
			else if (mark[x][y+1] == 2 && depth >= 3 && depth > deepest.depth)
				deepest = node; // cycle found
		}
		if (connectsLeft(grid, x, y)){
			if (mark[x][y-1] == 0){
				node.children[2] = new PathNode(x, y-1, node, depth);
				dfsRecursion(grid, mark, x, y-1, depth, node.children[2], deepest);
			}
			else if (mark[x][y-1] == 2 && depth >= 3 && depth > deepest.depth)
				deepest = node; // cycle found
		}
		if (connectsDown(grid, x, y)){
			if (mark[x+1][y] == 0){
				node.children[3] = new PathNode(x+1, y, node, depth);
				dfsRecursion(grid, mark, x+1, y, depth, node.children[3], deepest);
			}
			else if (mark[x+1][y] == 2 && depth >= 3 && depth > deepest.depth)
				deepest = node; // cycle found
		}
		if (connectsUp(grid, x, y)){
			if (mark[x-1][y] == 0){
				node.children[0] = new PathNode(x-1, y, node, depth);
				dfsRecursion(grid, mark, x-1, y, depth, node.children[0], deepest);
			}
			else if (mark[x-1][y] == 2 && depth >= 3 && depth > deepest.depth)
 				deepest = node; // cycle found
			
		}
	}
}

