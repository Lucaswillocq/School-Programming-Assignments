import java.util.*;

public class BingoPlayer
{	
	private String lastname;
	private String firstname;
	private double money;
	private BingoCard hand[];
	private int numberofcards;

	public BingoPlayer(String first, String last, double amount)
	{
		firstname = first;

		lastname = last;

		money = amount;

		hand = new BingoCard[0];

		numberofcards = 0;
	}

	public String getFullName()
	{
		return firstname+" "+lastname;
	}

	public String getFullInfo()
	{
		return firstname+" "+lastname+"($"+money+")";
	}

	public double getMoney()
	{
		return money;
	}

	public String toString()
	{
		StringBuilder S = new StringBuilder();

		S.append(getFullInfo());
		S.append("\n");
		S.append("\n");

		if(hand.length==0)
			{
				S.append("No Bingo Cards");
				S.append("\n");
			}
		else
		{	
			String[][] cString = new String[hand.length][];
			for(int i=0; i<cString.length; i++)
			{
				cString[i]=hand[i].toString().split("\n");
				
			}

			for(int i=0; i<cString[0].length; i++)
			{
				for(int j=0; j<cString.length; j++)
					{
						S.append(cString[j][i]);
						S.append("  ");
					}
				S.append("\n");
			}
		}

		return S.toString();
	}

	public boolean isBingo()
	{
		boolean bingo = false;

		for(int i=0; i<hand.length; i++)
		{	
			int counter1 = 0;
			int counter2 = 0;
			int counter3 = 0;
			int counter4 = 0;
			int counter5 = 0;
			int counter6 = 0;
			int counter7 = 0;
			int counter8 = 0;
			int counter9 = 0;
			int counter10 = 0;
			int counter11 = 0;
			int counter12 = 0;
			BingoCard card = hand[i]; 
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,j).isMarked())
					{counter1++;}
				if(counter1== 5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				int k = 4-j;
				if(card.getNumber(j,k).isMarked())
					{counter2++;}
				if(counter2==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(0,j).isMarked())
					{counter3++;}
				if(counter3==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(1,j).isMarked())
					{counter4++;}
				if(counter4==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(2,j).isMarked())
					{counter5++;}
				if(counter5==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(3,j).isMarked())
					{counter6++;}
				if(counter6==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(4,j).isMarked())
					{counter7++;}
				if(counter7==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,0).isMarked())
					{counter8++;}
				if(counter8==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,1).isMarked())
					{counter9++;}
				if(counter9==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,2).isMarked())
					{counter10++;}
				if(counter10==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,3).isMarked())
					{counter11++;}
				if(counter11==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,4).isMarked())
					{counter12++;}
				if(counter12==5)
					{bingo = true;}
			}
		}

		return bingo;
	}

	public BingoCard[] getBingoCards()
	{
		ArrayList<BingoCard> winningcards = new ArrayList<BingoCard>();
		boolean bingo = false;

		for(int i=0; i<hand.length; i++)
		{	
			bingo = false;
			int counter1 = 0;
			int counter2 = 0;
			int counter3 = 0;
			int counter4 = 0;
			int counter5 = 0;
			int counter6 = 0;
			int counter7 = 0;
			int counter8 = 0;
			int counter9 = 0;
			int counter10 = 0;
			int counter11 = 0;
			int counter12 = 0;
			BingoCard card = hand[i]; 
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,j).isMarked())
					{counter1++;}
				if(counter1== 5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				int k = 4-j;
				if(card.getNumber(j,k).isMarked())
					{counter2++;}
				if(counter2==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(0,j).isMarked())
					{counter3++;}
				if(counter3==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(1,j).isMarked())
					{counter4++;}
				if(counter4==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(2,j).isMarked())
					{counter5++;}
				if(counter5==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(3,j).isMarked())
					{counter6++;}
				if(counter6==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(4,j).isMarked())
					{counter7++;}
				if(counter7==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,0).isMarked())
					{counter8++;}
				if(counter8==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,1).isMarked())
					{counter9++;}
				if(counter9==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,2).isMarked())
					{counter10++;}
				if(counter10==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,3).isMarked())
					{counter11++;}
				if(counter11==5)
					{bingo = true;}
			}
			for(int j=0; j<5;j++)
			{
				if(card.getNumber(j,4).isMarked())
					{counter12++;}
				if(counter12==5)
					{bingo = true;}
			}
				if(bingo)
				{winningcards.add(hand[i]);}
		}	
				BingoCard[] winningcardarray = new BingoCard[winningcards.size()];

				for(int i=0; i<winningcards.size();i++)
				{winningcardarray[i]=winningcards.get(i);}
				return winningcardarray;

	}

	public int remove(int amount)
	{
		if(amount<money)
			{
				money = money-amount;
				return amount;
			}
		else
			{
				int intmoney = (int)money;
				double doublemoney = (double)intmoney;
				money = money - doublemoney;
				return intmoney;
			}
	}

	public void add(double amount)
	{
		money = money + amount;
	}

	public void addBingoCards(BingoCard[] cards)
	{
		hand = new BingoCard[cards.length];
		for(int i=0; i<hand.length; i++)
		{
			hand[i] = cards[i];
			numberofcards++;
		}
	}

	public void marks(int number)
	{
		for(int i=0; i<hand.length; i++)
		{
			BingoCard bc = hand[i];
			for(int j=0; j<5;j++)
			{
				for(int k=0; k<5; k++)
				{
					if(bc.getNumber(j,k).getNumber()==number)
						{bc.getNumber(j,k).mark();}
				}
			}
		}
	}
	public void clear()
	{
		numberofcards = 0;
		hand = new BingoCard[0];
	}
}



