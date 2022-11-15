import java.util.Random;

public class BingoBall
{
	private int ballnumber;

	public BingoBall (int number)
	{
		ballnumber = number;
	}

	public int getNumber()
	{
		return ballnumber;
	}

	public String toString()
	{
		StringBuilder S = new StringBuilder();

		S.append(Integer.toString(ballnumber));

		return S.toString();
	}
}