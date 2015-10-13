package mechanics;

import java.awt.Point;


public class State extends Point
{
	
	public State(int x, int y) {
		this.x = x; //closest cappable flag
		this.y = y; //last player capture
	}
	
	
	/*copies the newState object in to this state
	 */
	public void copy(State newState)
	{
		x = newState.x;
		y = newState.y;
	}
	
	public boolean equals(Object Obj)
	{
		State st = (State)Obj;
		return (x == st.x && y==st.y);
	}
	
}
