package efficiency.power_off_policies.action

import ClusterSchedulingSimulation.{CellState, ClaimDelta, Job}

import scala.util.control.Breaks

/**
 * Created by dfernandez on 15/1/16.
 */
object DefaultPowerOffAction extends PowerOffAction{
  override def powerOff(cellState: CellState, machineID: Int): Unit = {
    val state = cellState.isMachineOn(machineID) 
    if(cellState.isMachineOn(machineID) && cellState.allocatedCpusPerMachine(machineID) <= 0.00001 && cellState.allocatedMemPerMachine(machineID) <= 0.00001) {
      cellState.powerOffMachine(machineID)
      cellState.simulator.log(("Shutting down the machine with machine ID : %d in the power off policy : %s").format(machineID, name)) 
    }
    else{
      cellState.simulator.log(("Can not shut down the machine with machine ID : %d in the power off policy : %s because it has allocated %f cpus and %f mem").format(machineID, name, cellState.allocatedCpusPerMachine(machineID), cellState.allocatedMemPerMachine(machineID)))
     }
  }

  override val name: String = "default"
}
