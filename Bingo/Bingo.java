import java.util.*;
import java.io.*;

public class Bingo
{

	public static void main(String args[]) throws IOException
	{
		boolean quit = false;

		while(quit != true)
		{
			setupGame();
		}
	}
	public static void setupGame() throws IOException
	{
		Scanner S = new Scanner(new FileInputStream("players.txt"));
		Scanner userinput = new Scanner(System.in);

		double housemoney = S.nextDouble();
		S.nextLine();

		int playeramount = S.nextInt();
		BingoPlayer[] players = new BingoPlayer[playeramount];
		S.nextLine();

		for(int i=0; i<playeramount; i++)
		{
			String rawname = S.nextLine();
			String [] namebreak = rawname.split(",");
			String firstname = namebreak[0];
			String lastname = namebreak[1];
			double playermoney = Double.parseDouble(namebreak[2]);
			BingoPlayer player = new BingoPlayer(firstname, lastname, playermoney);
			players[i] = player;
		}

		for(int i=0; i<players.length; i++)
		{
			for(int j = i+1; j<players.length;j++)
			{
				String s1 = players[i].getFullName();
				String s2 = players[j].getFullName();
				int value = s1.compareTo(s2);
				if(value>0)
				{
					BingoPlayer temp  = players[j];
					players[j] = players[i];
					players[i] = temp;
				}		
			}		
		}

		System.out.println("******************************");
		System.out.println("* Welcome to SCI Bingo House *");
		System.out.println("******************************");
		System.out.println("These are all available players:");
		for(int i=0; i<playeramount; i++)
		{System.out.println((i+1)+". "+ players[i].getFullInfo());}

		int quitcounter = 0;
		double potmoney = 0.0;
		ArrayList<BingoPlayer> playingplayers = new ArrayList<BingoPlayer>();
		for(int i=0; i<playeramount; i++)
		{
			System.out.println(players[i].getFullName()+", would you like to play this round? (y/n)");
			String choice = userinput.next();
			while(!choice.equals("y") && !choice.equals("Y") && !choice.equals("n") && !choice.equals("N"))
			{
				System.out.println("Invalid Option");
				System.out.println(players[i].getFullName()+", would you like to play ths round? (y/n)");
				choice = userinput.next();
			}

			if(choice.equals("y") || choice.equals("Y"))
			{
				System.out.println("How many Bingo cards would you like to buy? (1-4):");
				userinput.nextLine();
				int cardsamount = userinput.nextInt();
				while(cardsamount<1 || cardsamount>4 || cardsamount>players[i].getMoney())
				{
					System.out.println("Invalid amount.");
					System.out.println("How many Bingo cards would you like to buy? (1-4):");
					userinput.nextLine();
					cardsamount = userinput.nextInt();
				}
				BingoCard[] hand = new BingoCard[cardsamount];
				for(int j=0; j<cardsamount; j++)
				{
					BingoCard aCard = new BingoCard();
					hand[j]=aCard;
				}
					
				players[i].addBingoCards(hand);
				int givemoney = players[i].remove(cardsamount);
				housemoney = housemoney + (double)givemoney/2;
				potmoney = potmoney + (double)givemoney/2;
				playingplayers.add(players[i]);
			}

			else if(choice.equals("n") || choice.equals("N"))
			{quitcounter++;}

			if(quitcounter==playeramount)
			{
				System.out.println("Thank you!");
				System.exit(0);
			}
		}

		ArrayList<BingoBall> usedballs = new ArrayList<BingoBall>();
		BingoCage cage = new BingoCage();
		showGame(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);

	}

	public static void showGame(int playeramount, BingoPlayer[] players, Scanner userinput, BingoCage cage, ArrayList<BingoBall> usedballs, double potmoney, double housemoney, ArrayList<BingoPlayer> playingplayers) throws IOException
	{
		System.out.println("House: $"+housemoney);
		System.out.println(" ");
		System.out.println("Pot: $"+potmoney);
		System.out.println(" ");
		System.out.print("Balls: [");
		for(int i=0; i<usedballs.size(); i++)
		{
			System.out.print(usedballs.get(i));
			System.out.print(",");
		}
		System.out.print("]");
		System.out.println("\n");
		for(int i=0; i<playingplayers.size(); i++)
		{System.out.println((i+1)+"."+playingplayers.get(i));}

		int nonbingo = 0;
		for(int i=0; i<playingplayers.size(); i++)
		{
			if(playingplayers.get(i).isBingo())
			{
				System.out.println(playingplayers.get(i).getFullName()+" says this card is a winning card");
				BingoCard[] winningcards = playingplayers.get(i).getBingoCards();
				for(int j=0; j<winningcards.length; j++)
				{System.out.println(winningcards[j].toSidebySideString());}
				System.out.println("These are the balls that have been drawn so far this round:");
				System.out.print("Balls: [");
				for(int j=0; j<usedballs.size(); j++)
				{
					System.out.print(usedballs.get(j));
					System.out.print(",");
				}
				System.out.print("]");
				System.out.println("\n");
				System.out.println("Is it a winning card? (y/n):");
				userinput.nextLine();
				String confirm = userinput.nextLine();
				if(confirm.equals("Y") || confirm.equals("y"))
				{
						System.out.println("This round is over");
						playingplayers.get(i).add(potmoney);
						saveData(housemoney, playeramount, players);
				}

			}
			else
				{nonbingo++;}
		}
		if(nonbingo==playingplayers.size())
		{
			System.out.println("There is no winning card yet.");
			Turn(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);
		}
	}

	public static void Turn(int playeramount, BingoPlayer[] players, Scanner userinput, BingoCage cage, ArrayList<BingoBall> usedballs, double potmoney, double housemoney, ArrayList<BingoPlayer> playingplayers) throws IOException
	{
		System.out.println("1) Draw a ball, 2) Draw balls until Bingo:");
		int ballchoice = userinput.nextInt();
		if(ballchoice ==1)
		{oneTurn(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);}
		else if(ballchoice ==2)
		{BingoTurn(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);}
	}

	public static void oneTurn(int playeramount, BingoPlayer[] players, Scanner userinput, BingoCage cage, ArrayList<BingoBall> usedballs, double potmoney, double housemoney, ArrayList<BingoPlayer> playingplayers) throws IOException
	{
		BingoBall ball = cage.draw();
		usedballs.add(ball);
		for(int i=0; i<playingplayers.size(); i++)
		{playingplayers.get(i).marks(ball.getNumber());}
		showGame(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);
	}
	public static void BingoTurn(int playeramount, BingoPlayer[] players, Scanner userinput, BingoCage cage, ArrayList<BingoBall> usedballs, double potmoney, double housemoney, ArrayList<BingoPlayer> playingplayers) throws IOException
	{
		BingoBall ball = cage.draw();
		usedballs.add(ball);
		for(int i=0; i<playingplayers.size(); i++)
		{playingplayers.get(i).marks(ball.getNumber());}

		System.out.println("House: $"+housemoney);
		System.out.println(" ");
		System.out.println("Pot: $"+potmoney);
		System.out.println(" ");
		System.out.print("Balls: [");
		for(int i=0; i<usedballs.size(); i++)
		{
			System.out.print(usedballs.get(i));
			System.out.print(",");
		}
		System.out.print("]");
		System.out.println("\n");
		for(int i=0; i<playingplayers.size(); i++)
		{System.out.println((i+1)+"."+playingplayers.get(i));}

		int nonbingo = 0;
		for(int i=0; i<playingplayers.size(); i++)
		{
			if(playingplayers.get(i).isBingo())
			{
				System.out.println(playingplayers.get(i).getFullName()+" says this card is a winning card");
				BingoCard[] winningcards = playingplayers.get(i).getBingoCards();
				for(int j=0; j<winningcards.length; j++)
				{System.out.println(winningcards[j].toSidebySideString());}
				System.out.println("These are the balls that have been drawn so far this round:");
				System.out.print("Balls: [");
				for(int j=0; j<usedballs.size(); j++)
				{
					System.out.print(usedballs.get(j));
					System.out.print(",");
				}
				System.out.print("]");
				System.out.println("\n");
				System.out.println("Is it a winning card? (y/n):");
				userinput.nextLine();
				String confirm = userinput.nextLine();
				if(confirm.equals("Y") || confirm.equals("y"))
				{
						System.out.println("This round is over");
						playingplayers.get(i).add(potmoney);
						saveData(housemoney, playeramount, players);
				}

			}
			else
				{nonbingo++;}
		}
		if(nonbingo==playingplayers.size())
		{
			System.out.println("There is no winning card yet.");
			BingoTurn(playeramount, players, userinput, cage, usedballs, potmoney, housemoney, playingplayers);
		}
	}
	public static void saveData(double housemoney, int playeramount, BingoPlayer[] players) throws IOException
	{
		PrintWriter P = new PrintWriter("players.txt");
		P.print(housemoney);
		P.print("\n");
		P.print(playeramount);
		P.print("\n");
		for(int i=0; i<players.length; i++)
		{
			String [] playerbreak = players[i].getFullName().split(" ");
			String playerfirstname = playerbreak[0];
			String playerlastname = playerbreak[1];
			P.print(playerfirstname+","+playerlastname+","+players[i].getMoney());
			P.print("\n");
		}
		P.close();
	}
}



