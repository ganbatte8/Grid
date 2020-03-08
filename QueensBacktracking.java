
class List{
	int item = -1; 
	int length = 0;
	List next;
	public List(){}
	public List(int x){item = x;}
	public List prepend(int x){ // no side effect on 'this'
		List newNode = new List(x);
		newNode.next = this;
		newNode.length = this.length+1;
		return newNode;
	}
	public List delete(){
		return this.next;
	}

	public void print(){
		List ref = this;
		while (ref.length > 0){
			System.out.print(ref.item +" ");
			ref = ref.next;
		}
		System.out.println();
	}
}

class SolutionsList{
	int[] options;
	SolutionsList next;
	public SolutionsList(){}
	public SolutionsList(int[] arr){options = arr;}
	public SolutionsList prepend(List l){ // no side effect on 'this'
		// deep-copy all the values of l into an array
		int[] nextOptions = new int[l.length]; 
		for (int i = l.length-1 ; i >= 0; i--, l = l.next)
			nextOptions[i] = l.item;
		SolutionsList newNode = new SolutionsList(nextOptions);
		newNode.next = this;
		return newNode;
	}

	public void print(){
		SolutionsList ref = this;
		while (ref.options != null){
			for (int x: ref.options)
				System.out.print(x+" ");
			System.out.println();
			ref = ref.next;
		}
	}
}

class Queens{
	public static void main(String[] args){
		int n = 5;
		boolean[][] grid = new boolean[n][n];
		int i,j;
		for (i = 0 ; i < n; i++){
			for (j = 0; j < n; j++)
				System.out.print((grid[i][j] ? "1 " : "0 "));
			System.out.print("\n");
		}

		SolutionsList output = new SolutionsList(); // first "item" is ignored
		List options = new List(); // same
		output = findSolutions(grid, options, output, 0, 0); 
		// output is a list of arrays
		options.print();
		output.print();
	}

	public static SolutionsList findSolutions(
	boolean[][] grid, 		// state of the grid
	List options, 			// list of the options executed thus far on the grid
	SolutionsList solutions, 	// list of the lists of options that yielded a solution thus far
	int choiceDepth, 		// the line we want to choose 
	int choiceColumn)		// the column we want to choose
	{
		// Let's assume the choice indicated is allowed ; let's execute it.
		grid[choiceDepth][choiceColumn] = true;
		options = options.prepend(choiceColumn);
		// does this choice directly yield a solution ?
		if (choiceDepth == grid.length-1){
			solutions = solutions.prepend(options);
		}
		// whether we have a solution or not, we need to know what is the next option we want to try.
		// Can we prepend another option to our options list in order to find a solution that uses our current choice ?
		choiceDepth++;
		if (choiceDepth <= grid.length-1){
			for (choiceColumn = 0; choiceColumn <= grid.length-1; choiceColumn++){
				if (canPlaceAt(grid, choiceDepth, choiceColumn)){
					return findSolutions(grid, options, solutions, choiceDepth, choiceColumn);
				}
			}
		}
		choiceDepth--;
		// if not, then we must backtrack until we find another valid option, if we can.

		while (choiceDepth >= 0){
			// backtrack one step :
			choiceColumn = options.item;	
			grid[choiceDepth][options.item] = false;
			options = options.delete();	
			// we've already explored this pair of (choiceDepth, choiceColumn). 
			// What if we increment choiceColumn ?
			while (choiceColumn < grid.length-1){
				choiceColumn++;
				if (canPlaceAt(grid, choiceDepth, choiceColumn))
					return findSolutions(grid, options, solutions, choiceDepth, choiceColumn);
			}
			// if incrementing choiceColumn is not enough, then we must go another step back, if possible.
			// This is handled by the while loop we're in.
			choiceDepth--;
		}
		return solutions;
	}


	public static boolean canPlaceAt(boolean[][] grid, int x, int y){
		int k;
		// no queen on the same line and column ?
		for (k = 0 ; k < grid.length; k++)
			if (grid[x][k] || grid[k][y])
				return false;
		// no queen on the same diagonals ?
		for (k = 1; x+k < grid.length && y+k < grid.length; k++)
			if (grid[x+k][y+k]) return false;
		for (k = 1; x+k < grid.length && y-k >= 0; k++)
			if (grid[x+k][y-k]) return false;
		for (k = 1; x-k >= 0 && y+k < grid.length; k++)
			if (grid[x-k][y+k]) return false;
		for (k = 1; x-k >= 0 && y-k >= 0 ; k++)
			if (grid[x-k][y-k]) return false;
		return true;
	}


		

}

