package efficiency.power_on_policies.action.unsatisfied

import ClusterSchedulingSimulation.{CellState, ClaimDelta, Job}
import efficiency.power_on_policies.action.PowerOnAction

import scala.util.control.Breaks

/**
 * Created by dfernandez on 15/1/16.
 */
object DefaultPowerOnAction extends PowerOnAction{
 

  override val name: String = "default"

  override def numberOfMachinesToPowerOn(cellState: CellState, job: Job, schedType: String, commitedDelta: Seq[ClaimDelta] = Seq[ClaimDelta](), conflictedDelta: Seq[ClaimDelta] =Seq[ClaimDelta]()): Int = {
      Math.max((job.cpusStillNeeded / cellState.cpusPerMachine).ceil.toInt, (job.memStillNeeded / cellState.memPerMachine).ceil.toInt)
  }
}
