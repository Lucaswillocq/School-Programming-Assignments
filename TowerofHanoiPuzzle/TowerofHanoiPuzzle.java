//Driver class to play the Tower of Hanoi game

import java.util.Scanner;


public class TowerofHanoiPuzzle
{
	public static void main(String [] args)
	{
		boolean quit = false;

		Scanner userinput = new Scanner(System.in);

		while(quit != true)
		{
			System.out.println("Welcome to Tower of Hanoi Puzzle");
			System.out.println("Take a pick:");
			System.out.println("1) Play a Tower of Hanoi Puzzle");
			System.out.println("2) Exit");
			int choice = userinput.nextInt();

			while(choice!=1 && choice!=2)
			{
				System.out.println("Invalid option");
				System.out.println("Take a pick:");
				System.out.println("1) Play a Tower of Hanoi Puzzle");
				System.out.println("2) Exit");
				choice = userinput.nextInt();
			}

			if(choice ==1)
				{StartGame(userinput);}
			else
				{quit = true;}
		}
		

	}

	public static void StartGame(Scanner userinput)
	{
		boolean backtomenu = false;
		while(backtomenu!= true)
		{
			System.out.println(" ");
			System.out.println("How many disks would you like to play (between 1 and 64):");
			int disknumber = userinput.nextInt();
			while(disknumber<1 || disknumber>64)
			{
				System.out.println("Invalid option");
				System.out.println("How many disks would you like to play (between 1 and 64):");
				disknumber = userinput.nextInt();
			}

			TowerOfHanoi game = new TowerOfHanoi(disknumber);
			System.out.println(game);
			System.out.println(" ");

			System.out.println("The goal is to move all "+disknumber+" disks from pole 1 to pole 3");
			System.out.println("The least number of moves for "+disknumber+ " disks is "+((int)Math.pow(2,disknumber)-1));
			System.out.println("Are you ready to play? (y/n):");

			String confirm = userinput.next();

			while(!confirm.equals("y") && !confirm.equals("Y") && !confirm.equals("n") && !confirm.equals("N"))
			{
				System.out.println("Invalid Option");
				System.out.println("Are you ready to play? (y/n):");
				confirm = userinput.next();
			}

			if(confirm.equals("y") || confirm.equals("Y"))
			{PlayGame(game, userinput, disknumber);}

			else if(confirm.equals("n") || confirm.equals("N"))
			{backtomenu = true;}
			backtomenu = true;
		}
	}
	public static void PlayGame(TowerOfHanoi game, Scanner userinput, int disknumber)
	{	boolean returntomenu = false;
		int moves = 0;
		userinput.nextLine();
		while(returntomenu!= true)
		{
			System.out.println(" ");
			System.out.println(game);
			System.out.println("Number of Moves: "+moves);

			System.out.println("Enter <from><space><to> to move a disk (or type 0<space>0 to go back to main menu): ");
			String move = userinput.nextLine();

			String [] movebreak = move.split(" ");
			int frompole = Integer.parseInt(movebreak[0]);
			int topole = Integer.parseInt(movebreak[1]);

			if(frompole==0 && topole ==0)
			{returntomenu=true;}

			else
			{
				Disk topfromdisk = game.peekTopDisk(frompole);
				Disk toptodisk = game.peekTopDisk(topole);

				if(!((frompole== 1 || frompole== 2 || frompole== 3)&&(topole ==1 || topole==2 || topole== 3)))
				{System.out.println("You did not insert a valid pole number.");}

				else if(game.peekTopDisk(frompole)==null)
				{System.out.println("There's no disk on this pole.");}

				else if((toptodisk!=null && topfromdisk!=null) && (topfromdisk.getSize() > toptodisk.getSize()))
				{
					System.out.println("You cannot move the top disk from pole "+frompole+" to pole "+topole+".");
					System.out.println("The top disk of pole "+frompole+" is larger than the top disk of pole "+topole+".");
					moves++;
				}
				else if((toptodisk==null&&topfromdisk!=null) || toptodisk.getSize()>topfromdisk.getSize() )
				{
					game.move(frompole, topole);
					moves++;
				}

				if(topole==3 && game.getNumberOfDisks(3) == disknumber)
				{

					System.out.println(" ");
					System.out.println(game);
					System.out.println(" ");
					System.out.println("Congratulations!!!");
					System.out.println("Number of moves: "+moves);
					System.out.println("The least number of moves for "+disknumber+" disks is "+((int)Math.pow(2,disknumber)-1));
					game.reset();
					returntomenu = true;
				}

			}
		}
	}
}





