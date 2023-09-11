package efficiency.power_on_policies.action.unsatisfied

import ClusterSchedulingSimulation.{CellState, ClaimDelta, Job}
import efficiency.power_on_policies.action.PowerOnAction

import scala.util.control.Breaks

/**
 * Created by dfernandez on 15/1/16.
 */
class PowerOnMarginFixedMachinesAction(numMachinesMargin : Int) extends PowerOnAction{
 

  override val name: String = "power-on-fixed-machines-margin-action"

  override def numberOfMachinesToPowerOn(cellState: CellState, job: Job, schedType: String, commitedDelta: Seq[ClaimDelta], conflictedDelta: Seq[ClaimDelta]): Int = {
    numMachinesMargin
  }
}
