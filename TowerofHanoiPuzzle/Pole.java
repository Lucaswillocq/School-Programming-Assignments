
//Class to represent a Pole object
public class Pole
{
	private Disk[] disks;
	private int numberofDisks;
	private int maxNumberofDisks;
	private int maxDiskSize;
	private char poleChar;

	public Pole(int aMaxNumberOfDisk, int aMaxDiskSize, char aPoleChar)
	{
		if(aMaxNumberOfDisk<1)
		{maxNumberofDisks=1;}
		else
		{maxNumberofDisks = aMaxNumberOfDisk;}

		if(aMaxDiskSize<1)
		{maxDiskSize=1;}
		else
		{maxDiskSize = aMaxDiskSize;}

		poleChar = aPoleChar;

		disks = new Disk[maxNumberofDisks];

		numberofDisks = 0;

	}

	public Pole(int aMaxNumberOfDisk, int aMaxDiskSize)
	{
		this(aMaxNumberOfDisk, aMaxDiskSize, '|');
	}

	public Pole(int aMaxNumberOfDisk)
	{
		this(aMaxNumberOfDisk, aMaxNumberOfDisk, '|');
	}

	public int getMaxNumberOfDisks()
	{
		return maxNumberofDisks;
	}

	public int getMaxDiskSize()
	{
		return maxDiskSize;
	}

	public int getNumberOfDisks()
	{
		return numberofDisks;
	}

	public Disk peekTopDisk()
	{

	if(numberofDisks<1)
		{return null;}
	else
		{return disks[numberofDisks-1];}

	}

	public String toString()
	{
		StringBuilder S = new StringBuilder();

		for(int i=0; i<maxDiskSize; i++)
		{S.append(" ");}
		S.append(poleChar);
		for(int i=0; i<maxDiskSize; i++)
		{S.append(" ");}
		S.append("\n");

		for(int i=(maxNumberofDisks-1); i>-1; i--)
			{
				if(disks[i]!=null)
				{
					int emptyamount = (maxDiskSize-disks[i].getSize());
					

					for(int j=0; j<emptyamount; j++)
					{S.append(" ");}

					S.append(disks[i].toString());

					for(int j=0; j<emptyamount; j++)
					{S.append(" ");}

				}
				else
				{

					for(int j=0; j<maxDiskSize; j++)
					{S.append(" ");}
					S.append(poleChar);
					for(int j=0; j<maxDiskSize; j++)
					{S.append(" ");}
				}	

					S.append("\n");

				}

		for(int i=0; i<(2*maxDiskSize)+1; i++)
		{S.append("=");}

		return S.toString();

	}

	public boolean addDisk(Disk aDisk)
	{	
		if(numberofDisks < maxNumberofDisks && aDisk.getSize() <= maxDiskSize)
		{
			disks[numberofDisks] = aDisk;
			numberofDisks++;
			return true;
		}
		else
		{return false;}

	}

	public Disk removeDisk()
	{
		if(numberofDisks <= 0)
		{return null;}
		else
		{
			numberofDisks--;
			Disk result = disks[numberofDisks];
			disks[numberofDisks] = null;
			return result;
		}

	}

}
