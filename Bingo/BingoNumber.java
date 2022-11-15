public class BingoNumber
{
	private int number;
	private boolean marked;
	private String stringnumber;

	public BingoNumber(int aNumber)
	{
		number = aNumber;

		stringnumber = Integer.toString(number);

		marked = false;

	}

	public int getNumber()
	{
		return number;
	}

	public boolean isMarked()
	{
		if(marked==true)
		{return true;}
		else
		{return false ;}
	}

	public String toString()
	{
		StringBuilder S = new StringBuilder();

		if(marked==true)
			{S.append(stringnumber);}
		else
		{
			if(number<10)
				{S.append(" "+number);}
			else
				{S.append(number);}
		}	

		return S.toString();

	}

	public String toNumberString()
	{
		StringBuilder S = new StringBuilder();

		if(number<10)
				{S.append(" "+number);}
			else
				{S.append(number);}

		return S.toString();

	}

	public void mark()
	{
		marked = true;

		stringnumber = "XX";

	}


}