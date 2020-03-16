import java.util.Random;

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
		Random rng = new Random(6); // seeded RNG (to get the same random grid every time)
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
		int[][] m = new int[lines][columns]; // grid of zeroes for algorithms to memorize marked tiles
		int connexComponentsTreated = 0;
		int connexComponents; // needed ?

		// generating a grid :
		// in this case I'm trying to generate an "interesting grid" by making sure it has a degree 3 tile even after removing dead ends.
		boolean foundDeg3AfterFiltering = false;
		int numberIterations = 0;
		while (!foundDeg3AfterFiltering){
			numberIterations++;
			randomize2DArray(g, rng);
			copy2DArray(g,h);
			for (i = 0; i < h.length; i++)
				for (j = 0; j < h[0].length; j++)
					filterLowDegree(h,i,j);
			for (i = 0; i < h.length; i++)
				for (j = 0; j < h[0].length; j++)
					if (degree(h,i,j) >= 3)
						foundDeg3AfterFiltering = true;
		}
		System.out.format("Found an adequate grid after %d iterations\n", numberIterations);

		System.out.println("Printing the grid:");
		print2DArray(g);
		
		// We can start our first algorithm here. Let's reinstantiate the structures we need that is not the grid itself.
		h = new int[lines][columns];
		m = new int[lines][columns];
		colors = new int[lines][columns];
		copy2DArray(g,h);
		for (i = 0; i < h.length; i++)
			for (j = 0; j < h[0].length; j++)
				filterLowDegree(h,i,j);
		System.out.println("Removed dead ends :");			
		print2DArray(h);
		System.out.println("Degrees:");
		setDegrees(h,deg);	// This is for visual information. deg is not involved in the algorithm
		print2DArray(deg);
		System.out.println("Coloring connex components :");
		colorConnexComponents(h, colors);
		print2DArray(colors);
		System.out.println("Finding the longest cycle :");
		PointList bestChoices = new PointList();
		PointList choices = new PointList();
		int bestLength = 0;
		for (i = 0; i < g.length; i++)
			for (j = 0; j < g[0].length; j++)
				if (colors[i][j] == connexComponentsTreated + 1){
					System.out.format("it's about to call the function at (%d, %d)\n", i, j);
					connexComponentsTreated++;
					choices = new PointList();
					choices = findLongestCycleInConnexComponentUsingBacktracking(
						h, m, i, j, 0, choices, choices, 0, 0);
					System.out.format("best choices for this connex component : ");
					choices.print();
					if (bestLength < choices.length){
						System.out.format("new best length, record it\n");
						bestLength = choices.length;
						bestChoices = choices;
					}
				}
		System.out.format("printing best overall choices : ");
		bestChoices.print();
		bestChoices.printMoves();
		System.out.println();

		// we can start our second algorithm here.
		m = new int[lines][columns];
		h = new int[lines][columns];
		copy2DArray(g,h);
		bestChoices = new PointList();
		choices = new PointList();
		for (i = 0; i < h.length; i++)
			for (j = 0; j < h[0].length; j++){
				System.out.format("\nCALLING ON TILE (%d,%d)\n", i, j);
				bestChoices = findLongestCyclePassingThroughTileUsingDFS(h, m, i, j, 0, 0, choices, bestChoices);
			}
		bestChoices.print();
		bestChoices.printMoves();
		System.out.println();
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
	 * Precondition : grid[x][y] != 0. (We don't want to color that empty tile in particular) */
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
	 * Return the number of connex components that were found. (We might not use that but it's easy and cheap to do)
	 * Precondition : mark is a 2D array of zeroes, same dimension as grid */ 
	public static int colorConnexComponents(int[][] grid, int[][] mark){
		int color = 1;
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[0].length; j++){
				if (mark[i][j] == 0 && grid[i][j] != 0){
					colorConnexComponent(grid, mark, color, i, j);
					color++;
				}
			}
		}
		return color-1;
	}

	public static PointList findLongestCyclePassingThroughTileUsingDFS(int[][] grid, int[][] visited, int x, int y, int fromDirection, int depth, PointList choicesMade, PointList bestChoicesMade){
		if (visited[x][y] == -1){ // base case : we're back at the first tile.
			if (depth > bestChoicesMade.length && depth >= 3){
				System.out.format("depth %d>%d, bestChoicesMade : ", depth, bestChoicesMade.length);
				bestChoicesMade = choicesMade.copy(depth);
				bestChoicesMade.print();
			}
			return bestChoicesMade;
		}
		if (depth == 0)	// mark the root node of the DFS tree as visited with -1, mark any other node with depth 
			visited[x][y] = -1;
		else
			visited[x][y] = depth;
		choicesMade = choicesMade.add(x,y,fromDirection);

		System.out.format("choicesMade : ");
		choicesMade.print();

		if (connectsUp(grid,x,y) && visited[x-1][y] <= 0){
			choicesMade.toDirection = 'U';
			bestChoicesMade = findLongestCyclePassingThroughTileUsingDFS(grid, visited, x-1, y, 'D', depth+1, choicesMade, bestChoicesMade);
			choicesMade.delete();
			visited[x-1][y] = (visited[x-1][y] == -1 ? -1 : 0);
		}
		if (connectsRight(grid,x,y) && visited[x][y+1] <= 0){
			choicesMade.toDirection = 'R';
			bestChoicesMade = findLongestCyclePassingThroughTileUsingDFS(grid, visited, x, y+1, 'L', depth+1, choicesMade, bestChoicesMade);
			choicesMade.delete();
			visited[x][y+1] = (visited[x][y+1] == -1 ? -1 : 0);
		}
		if (connectsLeft(grid,x,y) && visited[x][y-1] <= 0){
			choicesMade.toDirection = 'L';
			bestChoicesMade = findLongestCyclePassingThroughTileUsingDFS(grid, visited, x, y-1, 'R', depth+1, choicesMade, bestChoicesMade);
			choicesMade.delete();
			visited[x][y-1] = (visited[x][y-1] == -1 ? -1 : 0);
		}
		if (connectsDown(grid,x,y) && visited[x+1][y] <= 0){
			choicesMade.toDirection = 'D';
			bestChoicesMade = findLongestCyclePassingThroughTileUsingDFS(grid, visited, x+1, y, 'U', depth+1, choicesMade, bestChoicesMade);
			choicesMade.delete();
			visited[x+1][y] = (visited[x+1][y] == -1 ? -1 : 0);
		}
		visited[x][y] = 0;
		return bestChoicesMade;
	}

	/* The following function performs an exhaustive search to look for the longest cycle in a connex component.
	 * It's a tail-recursive function. The first call enters into any tile of the connex component, and each of the following calls 
	 * performs a choice to move to a neighboring tile.
	 * That choice is characterized by the parameters of the call. The call tries to transform those choice parameters into the next
	 * unexplored choice, if there is one, before making the next call. To do so, we keep track of a stack of choices and we may cancel some choices to explore
	 * different past choices. When a neighboring tile that is not the previously visited tile is already visited, we found a cycle; if it's the longest cycle
	 * found yet, we deep-copy the nodes of choicesMade that are in the cycle into bestChoicesMade. 
	 * When all the possible lists of choices have been explored at the end of the last call, bestChoicesMade is returned.
	 *
	 * We call this algorithm on whichever tile comes first for each connex component when scanning the grid left-to-right, up-to-bottom.
	 * If dead ends have been removed prior to this scan, then we know that first tile is gonna connect to exactly two other tiles, down and right.
	 * This algorithm will explore all the choices starting right and then all the choices starting down.
	 * That first tile is an articulation point iff removing it removes the existence of a path between its two neighbors, i.e. we end up with two connex
	 * components. This is equivalent to saying that there is no cycle passing through that first tile.
	 * It would be interesting to know these "first articulation points" in advance, because then we could delete them in a linear scan, 
	 * each deletion followed by a dead end filtering on their two neighbors. However we have to make sure that the other vertices marked as articulation points
	 * stay as articulation points.
	 *
	 * We could try to remove all the bridges too, being aware that this is becomes something about edges and not tiles. If we remove all the bridges, we end up
	 * with just biconnected components, in which every tile is part of a cycle.
	 * 
	 * If the first tile is part of a cycle, then for any choice that begins to the right is the reverse of a certain choice that begins to the left 
	 * (Not true ? You can find cycles that don't connect back to the starting point ? but whether you start going right or down, you should be able to explore
	 * every cycle that that biconnected component has access to I think ?)
	 */
	public static PointList findLongestCycleInConnexComponentUsingBacktracking(
	int[][] grid,
	int[][] visited, 	// not visited iff 0 ; nth visited iff n (>= 1)
	int xToVisit,
	int yToVisit,
	int fromDirection,	// 0 at initial call, otherwise it should be one of the following :  'U', 'R', 'L', 'D'
	PointList choicesMade,	
	PointList bestChoicesMade,
	int currentLength,	// 0 at the initial call. Should be incremented every time we mark a new tile.
	int bestLength)		// 0 at the initial call
	{
		// let's execute the choice we're given no matter what.
		currentLength++;
		visited[xToVisit][yToVisit] = currentLength;
		choicesMade = choicesMade.add(xToVisit, yToVisit, fromDirection); 
		// does the current choice directly yield a cycle ? is it the best yet ?
		if (currentLength > bestLength){
			// yes iff we can connect to a previously visited neighbor that is "old enough".
			// let's check each of the 4 neighbors. There may be a tile that we just came from, we'll make sure to fail the test for that one.
			if (connectsUp(grid,xToVisit,yToVisit) && visited[xToVisit-1][yToVisit] > 0 && currentLength - visited[xToVisit-1][yToVisit] + 1 > bestLength
			&& fromDirection != 'U'){
				bestLength = currentLength - visited[xToVisit-1][yToVisit] + 1;
				choicesMade.toDirection = 'U';
				bestChoicesMade = choicesMade.copy(currentLength - visited[xToVisit-1][yToVisit] + 1);
			}
			if (connectsRight(grid,xToVisit,yToVisit) && visited[xToVisit][yToVisit+1] > 0 && currentLength - visited[xToVisit][yToVisit+1] + 1 > bestLength
			&& fromDirection != 'R' ){
				bestLength = currentLength - visited[xToVisit][yToVisit+1] + 1;
				choicesMade.toDirection = 'R';
				bestChoicesMade = choicesMade.copy(currentLength - visited[xToVisit][yToVisit+1] + 1);
			}
			if (connectsLeft(grid,xToVisit,yToVisit) && visited[xToVisit][yToVisit-1] > 0 && currentLength - visited[xToVisit][yToVisit-1] + 1 > bestLength
			&& fromDirection != 'L' ){
				bestLength = currentLength - visited[xToVisit][yToVisit-1] + 1;
				choicesMade.toDirection = 'L';
				bestChoicesMade = choicesMade.copy(currentLength - visited[xToVisit][yToVisit-1] + 1);
			}
			if (connectsDown(grid,xToVisit,yToVisit) && visited[xToVisit+1][yToVisit] > 0 && currentLength - visited[xToVisit+1][yToVisit] + 1 > bestLength
			&& fromDirection != 'D' ){
				bestLength = currentLength - visited[xToVisit+1][yToVisit] + 1;
				choicesMade.toDirection = 'D';
				bestChoicesMade = choicesMade.copy(currentLength - visited[xToVisit+1][yToVisit] + 1);
			}
		}
		// can we go further to an unvisited tile ? Does it require making a choice ?
		// the order in which we consider these options should matter in order to properly backtrack after.
		if (connectsUp(grid,xToVisit,yToVisit) && visited[xToVisit-1][yToVisit] == 0){
			choicesMade.toDirection = 'U';
			return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, xToVisit-1, yToVisit, 'D', choicesMade, bestChoicesMade, currentLength, bestLength);
		}
		if (connectsRight(grid,xToVisit,yToVisit) && visited[xToVisit][yToVisit+1] == 0){
			choicesMade.toDirection = 'R';
			return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, xToVisit, yToVisit+1, 'L', choicesMade, bestChoicesMade, currentLength, bestLength);
		}
		if (connectsLeft(grid,xToVisit,yToVisit) && visited[xToVisit][yToVisit-1] == 0){
			choicesMade.toDirection = 'L';
			return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, xToVisit, yToVisit-1, 'R', choicesMade, bestChoicesMade, currentLength, bestLength);
		}
		if (connectsDown(grid,xToVisit,yToVisit) && visited[xToVisit+1][yToVisit] == 0){
			choicesMade.toDirection = 'D';
			return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, xToVisit+1, yToVisit, 'U', choicesMade, bestChoicesMade, currentLength, bestLength);
		}
		// if not we must backtrack if possible. Let's cancel the last choice :
		// We have choicesMade.next == null iff the list is "empty". In that case, then we cannot cancel any choice.
		while (choicesMade.next != null){
			visited[choicesMade.x][choicesMade.y] = 0;
			choicesMade = choicesMade.delete();
			currentLength--;
			// choicesMade was already visited. choicesMade.toDirection also indicates a visited tile.
			// is choicesMade pointing to an unvisited tile ?
			if (choicesMade.toDirection == 'U'){ // we want to see if the tile connects right, left or down.
				if (connectsRight(grid, choicesMade.x, choicesMade.y) && choicesMade.fromDirection != 'R'){
					choicesMade.toDirection = 'R';
					return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, choicesMade.x, choicesMade.y+1, 'L', choicesMade, bestChoicesMade, currentLength, bestLength);
				}
				choicesMade.toDirection = 'R';
			}
			if (choicesMade.toDirection == 'R'){ // we want to see if the tile connects left or down.
				if (connectsLeft(grid, choicesMade.x, choicesMade.y) && choicesMade.fromDirection != 'L'){
					choicesMade.toDirection = 'L';
					return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, choicesMade.x, choicesMade.y-1, 'R', choicesMade, bestChoicesMade, currentLength, bestLength);
				}
				choicesMade.toDirection = 'L';
			}
			if (choicesMade.toDirection == 'L'){ // we want to see if the tile connects down.
				if (connectsDown(grid, choicesMade.x, choicesMade.y) && choicesMade.fromDirection != 'D'){
					choicesMade.toDirection = 'D';
					return findLongestCycleInConnexComponentUsingBacktracking(grid, visited, choicesMade.x+1, choicesMade.y, 'U', choicesMade, bestChoicesMade, currentLength, bestLength);
				}
			}
		}
		return bestChoicesMade;
	}

}

class PointList{
	// LIFO structure.
	int x;
	int y;
	int fromDirection; // 'U', 'R', 'L' ,'D' or 0
	int toDirection; 
	int length = 0;
	PointList next;

	// The first node to be instantiated should be a "fake" node signaling the end of a list.
	// That node is whichever one has a member "next" pointing to null.
	// This should allow us to create an "empty" list using new, and then can call non-static methods on it.

	public PointList(){}
	public PointList add(int x, int y, int fromDirection){
		PointList newNode = new PointList();
		newNode.x = x;
		newNode.y = y;
		newNode.fromDirection = fromDirection;
		newNode.length = this.length+1;
		newNode.next = this;
		return newNode;
	}
	public PointList delete(){ 
		if (this.next != null)
			return this.next;
		return this;
	}
	public PointList copy(int cycleLength){	
		// this is used to memorize potential cycle solutions. Length is the length of the cycle found. We only copy the first {cycleLength} nodes.
		// it reverses the list, which should be totally fine for our use case, as long as we reset the length of the last node copied.
		// that length is used to compare the lengths of the longest cycles of different connex components.
		PointList l = this;
		PointList ret = new PointList();
		for (int i = 0; i < cycleLength; i++){
			ret = ret.add(l.x, l.y, l.fromDirection);
			ret.toDirection = l.toDirection;
			l = l.next;
		}
		ret.length = cycleLength;
		return ret;
	}
	public void print(){
		PointList l = this;
		for (; l.next != null; l = l.next)
			System.out.format("(%d,%d) ", l.x, l.y);
		System.out.println();
	}
	public void printMoves(){
		PointList l = this;
		for (; l.next != null; l = l.next)
			System.out.format("%c ", l.toDirection);
		System.out.println();
	}
}


/*
 * idea : for each tile, memorize biggest cycle when going left, the biggest cycle when going right, etc. (doesn't make much sense, but maybe the ideas of dynamic programming can lead to something new ?)
 * idea 2 : start at the most upper-left tile ; find the biggest cycle passing through that tile (i.e. longest path from its down tile to its right tile), then recurse on the rest
 * of the tiles after deleting the first one (we can also filter dead ends on its neighbors after its deletion). (should be easy provided we have an algorithm for longest path...)
 * idea 3 : divide and conquer (hard ?)
 * idea 4 : for a really slow but simple algorithm, use some version of DFS on all the vertices.
 * idea 5 : look up how to find bridges and/or articulation points. a connected graph should decompose into a tree of biconnected components called the block-cut tree of the graph. The blocks are attached to each other at shared vertices called articulation points. A cycle is always contained within a single biconnected component of this tree, because a cycle graph is biconnected. Every vertex of the grid should be within a cycle iff it is part of a biconnected component of more than 2 vertices, because if the graph is biconnected then by definition it remains connected after the removal of that vertex. That means it's possible to take two neighbors
 * of that vertex such that there is another path between those, i.e. the vertex is in a cycle.
 * idea 6 : can we use some kind of random algorithm ?
 */
