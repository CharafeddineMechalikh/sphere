package efficiency.power_on_policies.action

import ClusterSchedulingSimulation.{ClaimDelta, Job, CellState}

import scala.util.control.Breaks

/**
 * Created by dfernandez on 15/1/16.
 */
trait PowerOnAction {
   val name : String
 

  def numberOfMachinesToPowerOn(cellState: CellState, job: Job, schedType: String, commitedDelta: Seq[ClaimDelta] = Seq[ClaimDelta](), conflictedDelta: Seq[ClaimDelta] =Seq[ClaimDelta]()): Int
}
