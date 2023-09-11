package efficiency.power_on_policies.action.margin

import ClusterSchedulingSimulation.{CellState, ClaimDelta, Job}
import efficiency.power_on_policies.action.PowerOnAction

import scala.util.control.Breaks

/**
 * Created by dfernandez on 15/1/16.
 */
class PowerOnMarginPercAvailableAction(resourcesPercentageMargin : Double) extends PowerOnAction{
 
  override val name: String = ("power-on-percentage-resources-free-margin-action-with-margin:%f").format(resourcesPercentageMargin)

  

  override def numberOfMachinesToPowerOn(cellState: CellState, job: Job, schedType: String, commitedDelta: Seq[ClaimDelta] = Seq[ClaimDelta](), conflictedDelta: Seq[ClaimDelta] =Seq[ClaimDelta]()): Int = {
    var machinesNeeded = 0
    val cpuAvailablePerc = cellState.availableCpus / cellState.onCpus
    val memAvailablePerc = cellState.availableMem / cellState.onMem
    if(Math.min(cpuAvailablePerc, memAvailablePerc) < resourcesPercentageMargin){
      val newMargin = resourcesPercentageMargin - Math.min(cpuAvailablePerc, memAvailablePerc)
      machinesNeeded = (newMargin * (cellState.numberOfMachinesOn)).ceil.toInt
    }
    machinesNeeded
  }
}


