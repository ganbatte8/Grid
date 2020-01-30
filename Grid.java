class Grid{
	int lines;
	int columns;
	int[][] grid;

	public Grid(int lines, int columns){
		this.lines = lines;
		this.columns = columns;
		int i,j;
		grid = new int[lines][columns];
	}

	public void initialize(){
		for(int i=0;i<lines;i++)
			for(int j=0;j<columns;j++)
				grid[i][j] = columns*i+j;
	}

	public void randomize(){
		for(int i=0; i<lines; i++)
			for(int j=0; j<columns; j++)
				grid[i][j] = (int)(Math.random()*8); // 0<=x<8
	}

	public void printGrid(){
		int i,j;
		for(i=0;i<lines;i++){
			for(j=0;j<columns;j++)
				System.out.print(grid[i][j]+" ");
			System.out.println();
		}
	}
		
	public static void main(String[] args){
		Grid g = new Grid(4,6);
		System.out.println("Created a new grid :");
		g.printGrid();
		g.initialize();
		System.out.println("Modified the grid :");
		g.printGrid();
		g.randomize();
		System.out.println("Randomized the grid :");
		g.printGrid();
	}
}
