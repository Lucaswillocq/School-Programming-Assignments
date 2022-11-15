import java.util.Random;

public class BingoCard
{
	private BingoNumber[][] bingonumbers;
	
	public BingoCard()
	{
		Random rand = new Random();
		bingonumbers = new BingoNumber[5][5];

		int[] firstcolumn = new int[5];
		for(int i=0; i<firstcolumn.length; i++)
			{
				firstcolumn[i] = rand.nextInt(15)+1;

				for(int j =0; j<i; j++)
				{
					if(firstcolumn[i]==firstcolumn[j])
					{i--;}
				}
			}


		int[] secondcolumn = new int[5];
		for(int i=0; i<secondcolumn.length; i++)
			{
				secondcolumn[i] = rand.nextInt(15)+16;

				for(int j =0; j<i; j++)
				{
					if(secondcolumn[i]==secondcolumn[j])
					{i--;}
				}
			}


		int[] thirdcolumn = new int[5];
		for(int i=0; i<thirdcolumn.length; i++)
			{
				thirdcolumn[i] = rand.nextInt(15)+31;

				for(int j =0; j<i; j++)
				{
					if(thirdcolumn[i]==thirdcolumn[j])
					{i--;}
				}
			}


		int[] fourthcolumn = new int[5];
		for(int i=0; i<fourthcolumn.length; i++)
			{
				fourthcolumn[i] = rand.nextInt(15)+46;

				for(int j =0; j<i; j++)
				{
					if(fourthcolumn[i]==fourthcolumn[j])
					{i--;}
				}
			}


		int[] fifthcolumn = new int[5];
		for(int i=0; i<fifthcolumn.length; i++)
			{
				fifthcolumn[i] = rand.nextInt(15)+61;

				for(int j =0; j<i; j++)
				{
					if(fifthcolumn[i]==fifthcolumn[j])
					{i--;}
				}
			}


		for(int i=0; i<5; i++)
		{
			bingonumbers[i][0] = new BingoNumber(firstcolumn[i]);
		}

		for(int i=0; i<5; i++)
		{
			bingonumbers[i][1] = new BingoNumber(secondcolumn[i]);
		}

		for(int i=0; i<5; i++)
		{
			bingonumbers[i][2] = new BingoNumber(thirdcolumn[i]);
		}

		for(int i=0; i<5; i++)
		{
			bingonumbers[i][3] = new BingoNumber(fourthcolumn[i]);
		}

		for(int i=0; i<5; i++)
		{
			bingonumbers[i][4] = new BingoNumber(fifthcolumn[i]);
		}

		bingonumbers[2][2].mark();

	}

	public String toString()
	{

		StringBuilder S = new StringBuilder();
		S.append("  B  I  N  G  O ");
		S.append("\n");
		S.append("+--+--+--+--+--+");
		S.append("\n");
		for(int i=0; i<bingonumbers.length; i++)
		{
			for(int j =0; j<bingonumbers[i].length; j++)
			{
				S.append("|");
				S.append(bingonumbers[i][j]);
			}
			S.append("|");
			S.append("\n");
			S.append("+--+--+--+--+--+");
			S.append("\n");
		}

		return S.toString();
	}

	public BingoNumber getNumber(int row, int column)
	{
		return bingonumbers[row][column];
	}

	public String toSidebySideString()
	{
		StringBuilder S = new StringBuilder();
		S.append("  B  I  N  G  O");
		S.append("\n");
		S.append("+--+--+--+--+--+");
		S.append("\n");
		for(int i=0; i<bingonumbers.length; i++)
		{
			for(int j =0; j<bingonumbers[i].length; j++)
			{
				S.append("|");
				S.append(bingonumbers[i][j]);
			}
			S.append("|");
			S.append("\n");
			S.append("+--+--+--+--+--+");
			S.append("\n");
		}
		
		StringBuilder Unmarkedcard = new StringBuilder();
		Unmarkedcard.append("  B  I  N  G  O");
		Unmarkedcard.append("\n");
		Unmarkedcard.append("+--+--+--+--+--+");
		Unmarkedcard.append("\n");
		for(int i=0; i<2; i++)
		{
			for(int j =0; j<bingonumbers[i].length; j++)
			{
				Unmarkedcard.append("|");
				Unmarkedcard.append(bingonumbers[i][j].toNumberString());
			}
			Unmarkedcard.append("|");
			Unmarkedcard.append("\n");
			Unmarkedcard.append("+--+--+--+--+--+");
			Unmarkedcard.append("\n");
		}
		Unmarkedcard.append("|"+bingonumbers[2][0].toNumberString()
							+"|"+bingonumbers[2][1].toNumberString()
							+"|"+"XX"+"|"+bingonumbers[2][3].toNumberString()
							+"|"+bingonumbers[2][4].toNumberString()+"|");
		Unmarkedcard.append("\n");
		Unmarkedcard.append("+--+--+--+--+--+");
		Unmarkedcard.append("\n");
		for(int i=3; i<5; i++)
		{
			for(int j=0; j<bingonumbers[i].length; j++)
			{
				Unmarkedcard.append("|");
				Unmarkedcard.append(bingonumbers[i][j].toNumberString());
			}
			Unmarkedcard.append("|");
			Unmarkedcard.append("\n");
			Unmarkedcard.append("+--+--+--+--+--+");
			Unmarkedcard.append("\n");
		}


		StringBuilder Side = new StringBuilder();
		String[] realcard = S.toString().split("\n");
		String[] oldcard = Unmarkedcard.toString().split("\n");

		for(int i=0; i<12; i++)
		{
			Side.append(realcard[i]);
			Side.append(" ");
			Side.append(oldcard[i]);
			Side.append("\n");
		}

		return Side.toString();

	}

	public boolean mark(int number)
	{
		boolean flag = false;

		for(int i=0; i<5; i++)
		{
			for(int j=0; j<5; j++)
			{
				if(bingonumbers[i][j].getNumber()==number)
					{
						bingonumbers[i][j].mark();
						flag = true;
					}
				else
					{flag =false;}
			}
		}
		return flag;
	}

}


