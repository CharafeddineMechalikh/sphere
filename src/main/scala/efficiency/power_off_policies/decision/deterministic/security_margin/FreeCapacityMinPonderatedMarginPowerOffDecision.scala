package efficiency.power_off_policies.decision.deterministic.security_margin

import ClusterSchedulingSimulation.CellState
import efficiency.power_off_policies.decision.PowerOffDecision

/**
   * Created by dfernandez on 15/1/16.
   */
class FreeCapacityMinPonderatedMarginPowerOffDecision(percentage : Double) extends PowerOffDecision{
    override def shouldPowerOff(cellState: CellState, machineID: Int): Boolean = {
   
      assert(percentage > 0 && percentage < 1, "Security margin percentage value must be between 0.001 and 0.999")
        percentage < 1 - Math.max(cellState.totalOccupiedCpus/cellState.totalCpus, cellState.totalOccupiedMem/cellState.totalMem)
    }

    override val name: String = ("free-capacity-min-ponderated-margin-power-off-decision-with-perc:%f").format(percentage)
  }
