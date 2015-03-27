package SEIRmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class AgentExposed implements AgentGeneric{
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int infTick;
	private int expPeriod = 10;
	
	public AgentExposed(ContinuousSpace<Object> space, Grid<Object> grid){
		this.space = space;
		this.grid = grid;
		this.infTick = (int)RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		getParameters();
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		//get current location
		GridPoint pt = grid.getLocation(this);

		//randomise direction of movement
		int xAdj = ranInt(minMove, maxMove);
		int yAdj = ranInt(minMove, maxMove);
		System.out.println(xAdj + " " + yAdj);	
		agentMove(pt, xAdj, yAdj);
		isInfected(infTick);
	}
	
	public void isInfected(int infTick){
		int currTick = (int)RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		GridPoint pt = grid.getLocation(this);
		List<Object> exposed = new ArrayList<Object>();
		
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof AgentExposed) {
				if ((infTick + expPeriod) < currTick){
					exposed.add(obj);
				}
			}
		}
		
		if (exposed.size() > 0) {
				int index = RandomHelper.nextIntFromTo(0, exposed.size() - 1);
				Object obj = exposed.get(index);
				NdPoint spacePt = space.getLocation(obj);
				Context<Object> context = ContextUtils.getContext(obj);
				context.remove(obj);
				AgentInfected agnInf = new AgentInfected(space, grid);
				context.add(agnInf);
				space.moveTo(agnInf, spacePt.getX(), spacePt.getY());
				grid.moveTo(agnInf, pt.getX(), pt.getY());
		}
	}
	
	public void agentMove(GridPoint nPt, int xAdj, int yAdj) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint((nPt.getX()+xAdj) , (nPt.getY()+yAdj));
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
				otherPoint);
		space.moveByVector(this, 1, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}

	public static int ranInt(int min, int max){
		Random rn = new Random();
		int rnInt = rn.nextInt((max-min)+1) + min;
		return rnInt;
	}
	
	private void getParameters(){
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		this.expPeriod = (Integer)params.getValue("exposed_period");
	}
}
