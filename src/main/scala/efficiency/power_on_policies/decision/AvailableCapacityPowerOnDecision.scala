package efficiency.power_on_policies.decision

import ClusterSchedulingSimulation.{CellState, ClaimDelta, Job}

/**
 * Created by dfernandez on 15/1/16.
 */
class AvailableCapacityPowerOnDecision(availabilityFactor: Double) extends PowerOnDecision{
  override def shouldPowerOn(cellState: CellState, job: Job, schedType: String, commitedDelta: Seq[ClaimDelta], conflictedDelta: Seq[ClaimDelta]): Boolean = {
  
    assert(availabilityFactor > 0.0, "Availability ")
    job!=null && job.unscheduledTasks > 0 && job.cpusStillNeeded*availabilityFactor > cellState.availableCpus && (job.turnOnRequests.length <=1 || (cellState.simulator.currentTime - job.turnOnRequests(job.turnOnRequests.length-1)) > cellState.powerOnTime*1.05)

  }

  override val name: String = ("availability-capacity:%f").format(availabilityFactor)
}
