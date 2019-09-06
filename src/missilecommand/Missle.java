package missilecommand;

import java.awt.Color;
import java.awt.Graphics2D;

public class Missle {
	
	private Vector2f velocity;
	private Vector2f position;
        private Vector2f startPosition;
        private Vector2f lastPosition;
        Vector2f nextSegment;
	private Color color;
	private float radius;
        float sin;
        float cos;
        float inc = 0;

	public Missle(Vector2f position, Vector2f endPosition, float angle) {
		this.position = position;
                lastPosition = position;
                nextSegment = position;
                this.startPosition = new Vector2f(321,0,1);
		velocity = Vector2f.polar(angle, 1.0f);
		radius = 0.006f;
		color = Color.GREEN;
                sin = (float)Math.sin(angle);
                cos = (float)Math.cos(angle);
	}

	public Vector2f getPosition() {
		return nextSegment;
	}

	public void draw(Graphics2D g, Matrix3x3f view) {

		g.setColor(color);
		Vector2f topLeft = new Vector2f(lastPosition.x - radius, lastPosition.y
				+ radius);
                nextSegment = new Vector2f(position.x + inc * cos, position.y + inc * sin);
                inc = inc + 2;
		topLeft = view.mul(topLeft);
		Vector2f bottomRight = new Vector2f(position.x + radius, position.y
				- radius);
                lastPosition = topLeft;
		bottomRight = view.mul(bottomRight);
		int circleX = (int) topLeft.x;
		int circleY = (int) topLeft.y;
		int circleWidth = (int) (bottomRight.x - topLeft.x);
		int circleHeight = (int) (bottomRight.y - topLeft.y);
//		g.fillOval(circleX, circleY, circleWidth, circleHeight);
//                g.drawLine((int)startPosition.x, (int)startPosition.y,(int)position.x, (int)position.y);
                g.drawLine((int)startPosition.x, (int)startPosition.y,(int)nextSegment.x,(int)nextSegment.y);
	}

	public void update(float time) {
//		position = position.add(velocity.mul(time));
	}
}
