
public class Disk
{
	private int diskSize;
	private String diskString;
	private char diskChar;
	private char poleChar;

	public Disk(int aDiskSize, char aDiskChar, char aPoleChar)
	{
		if(aDiskSize <= 0)
		{diskSize = 1;}
		else
		{diskSize = aDiskSize;}
		diskChar = aDiskChar;
		poleChar = aPoleChar;

		StringBuilder S = new StringBuilder();
		for(int i=0; i<diskSize; i++)
			{S.append(diskChar);}
		S.append(poleChar);
		for(int i=0; i<diskSize; i++)
			{S.append(diskChar);}
		diskString = S.toString();
	}

	public Disk(int aDiskSize)
	{
		this(aDiskSize, '*', '|');

	}

	public int getSize()
	{
		return diskSize;
	}

	public String toString()
	{
		return diskString;

	}

}
