package efficiency.power_off_policies.decision.probabilistic

import ClusterSchedulingSimulation.{Job, CellState}
import efficiency.{DistributionUtils, DistributionCache}
import efficiency.power_off_policies.decision.PowerOffDecision
import org.apache.commons.math.distribution.{GammaDistributionImpl, ExponentialDistributionImpl}

/**
 * Created by dfernandez on 22/1/16.
 */
class GammaPowerOffDecision(threshold : Double, windowSize: Int, lostFactor : Double, ts : Double = 30.0) extends PowerOffDecision with DistributionUtils{
  override def shouldPowerOff(cellState: CellState, machineID: Int): Boolean = {
      val allPastTuples = getPastTuples(cellState, windowSize)
    var should = false
    val jobAttributes = getJobAttributes(allPastTuples)

    if(jobAttributes._1 > 0.0 && jobAttributes._3 > 0.0 && jobAttributes._5 > 0.0){
      val alphaCpu = (cellState.availableCpus - (cellState.numberOfMachinesOn * cellState.cpusPerMachine * lostFactor)) / jobAttributes._5
      val alphaMem = (cellState.availableMem - (cellState.numberOfMachinesOn * cellState.memPerMachine * lostFactor)) / jobAttributes._3
       val prob = getGammaDistributionCummulativeProbability( Math.min(alphaCpu,alphaMem), jobAttributes._1, ts)
      should = prob <= threshold
    }
    should

   
  }


  override val name: String = ("gamma-off-threshold:%f-window:%d-lost:%f").format(threshold,windowSize,lostFactor)
}
