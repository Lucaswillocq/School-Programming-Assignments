import java.util.*;

public class BingoCage
{
	private ArrayList<BingoBall> balls;

	public BingoCage()
	{
		balls = new ArrayList<BingoBall>();

		for(int i=0; i<75; i++)
			{balls.add(new BingoBall(i+1));}

	}

	public BingoBall draw()
	{
		Random rand = new Random();

		BingoBall ball = null;

		if(balls.size()==0)
		{ball = null;}

		else 
		{
			int selection = rand.nextInt(balls.size());
			ball = balls.get(selection);

			balls.remove(ball);
		}

		return ball;
		
	}

	public void reset()
	{
		balls.clear();

		for(int i=0; i<75; i++)
			{balls.add(new BingoBall(i+1));}

	}

}