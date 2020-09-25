public class TowerOfHanoi
{
	private Pole[] poles;

	TowerOfHanoi()
	{

		poles = new Pole[3];
		Pole pole1 = new Pole(7);
		for(int i=7; i>0; i--)
			{pole1.addDisk(new Disk(i));}
		Pole pole2 = new Pole(7);
		Pole pole3 = new Pole(7);
		poles[0] = pole1;
		poles[1] = pole2;
		poles[2] = pole3;


	}

	TowerOfHanoi(int aNumberofDisks)
	{
		poles = new Pole[3];

		if(aNumberofDisks>=1)
		{
			Pole pole1 = new Pole(aNumberofDisks);
			for(int i=aNumberofDisks; i>0; i--)
				{pole1.addDisk(new Disk(i));}
			Pole pole2 = new Pole(aNumberofDisks);
			Pole pole3 = new Pole(aNumberofDisks);
			poles[0] = pole1;
			poles[1] = pole2;
			poles[2] = pole3;
		}	
		else
		{
			Pole pole1 = new Pole(1);
			pole1.addDisk(new Disk(1));
			Pole pole2 = new Pole(1);
			Pole pole3 = new Pole(1);
			poles[0] = pole1;
			poles[1] = pole2;
			poles[2] = pole3;

		}


	}

	public Disk peekTopDisk(int aPoleNumber)
	{

		int poleindex = aPoleNumber-1;

		if(aPoleNumber!=1 && aPoleNumber!=2 && aPoleNumber!= 3)
		{return null;}

		else if(poles[poleindex].getNumberOfDisks()==0)
		{return null;}

		else
		{return poles[poleindex].peekTopDisk();}

		
	}

	public int getNumberOfDisks(int aPoleNumber)
	{
		int poleindex = aPoleNumber-1;

		if(aPoleNumber!= 1 && aPoleNumber!= 2 && aPoleNumber!= 3)
		{return -1;}
		else
		{return poles[poleindex].getNumberOfDisks();}	

	}

	public String toString()
	{

		StringBuilder S = new StringBuilder();


		for(int i=0; i<poles[0].getMaxDiskSize(); i++)
			{S.append(" ");}
		S.append("1");
		for(int i=0; i<poles[0].getMaxDiskSize(); i++)
			{S.append(" ");}
		S.append(" ");
		for(int i=0; i<(poles[1].getMaxDiskSize()); i++)
			{S.append(" ");}
		S.append("2");
		for(int i=0; i<(poles[1].getMaxDiskSize()); i++)
			{S.append(" ");}
		S.append(" ");
		for(int i=0; i<(poles[2].getMaxDiskSize()); i++)
			{S.append(" ");}
		S.append("3");
		for(int i=0; i<poles[2].getMaxDiskSize(); i++)
			{S.append(" ");}
		S.append("\n");

		String pole1array[] = poles[0].toString().split("\n");
		String pole2array[] = poles[1].toString().split("\n");
		String pole3array[] = poles[2].toString().split("\n");
		for(int i=0; i<=poles[0].getMaxNumberOfDisks(); i++)
		{
			S.append(pole1array[i]);
			S.append(" ");
			S.append(pole2array[i]);
			S.append(" ");
			S.append(pole3array[i]);
			S.append("\n");
		}

		S.append(pole1array[pole1array.length-1]+pole2array[pole2array.length-1]+pole3array[pole3array.length-1]+"==");


		return S.toString();
	}

	public boolean move(int fromPole, int toPole)
	{
		int polefromindex = fromPole-1;
		int poletoindex = toPole-1;

		if(!((fromPole==1||fromPole==2||fromPole==3)&&(toPole==1||toPole==2||toPole==3)))
			{return false;}

		else if(poles[polefromindex].getNumberOfDisks()==0)
			{return false;}

		else if(poles[poletoindex].getNumberOfDisks()==poles[poletoindex].getMaxNumberOfDisks())
			{return false;}
		else
		{
			Disk moveddisk = poles[polefromindex].removeDisk();
			poles[poletoindex].addDisk(moveddisk);

			return true;
		}
	}

	public void reset()
	{


		if(poles[0].getNumberOfDisks()>0)
			{
				while(poles[0].getNumberOfDisks()>0)
					{poles[0].removeDisk();}
			}

		if(poles[1].getNumberOfDisks()>0)
		{
			while(poles[1].getNumberOfDisks()>0)
				{poles[1].removeDisk();}
		}

		if(poles[2].getNumberOfDisks()>0)
		{
			while(poles[2].getNumberOfDisks()>0)
				{poles[2].removeDisk();}
		}

		for(int i=poles[0].getMaxDiskSize(); i>0; i--)
			{poles[0].addDisk(new Disk(i));}
	}

}
