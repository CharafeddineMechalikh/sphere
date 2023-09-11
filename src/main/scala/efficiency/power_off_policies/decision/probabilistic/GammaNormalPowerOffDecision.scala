package efficiency.power_off_policies.decision.probabilistic

import ClusterSchedulingSimulation.{CellState, Job}
import efficiency.{DistributionUtils, DistributionCache}
import efficiency.power_off_policies.decision.PowerOffDecision
import org.apache.commons.math.distribution.{NormalDistributionImpl, GammaDistributionImpl}

/**
 * Created by dfernandez on 22/1/16.
 */
class GammaNormalPowerOffDecision(normalThreshold: Double, threshold : Double, windowSize: Int, ts : Double = 130.0) extends PowerOffDecision with DistributionUtils{

  override def shouldPowerOff(cellState: CellState, machineID: Int): Boolean = {
     val allPastTuples = getPastTuples(cellState, windowSize)
    var should = false
    val jobAttributes = getJobAttributes(allPastTuples)

    if(jobAttributes._1 > 0.0 && jobAttributes._2 > 0.0 && jobAttributes._3 > 0.0 && jobAttributes._4 > 0.0 && jobAttributes._5 > 0.0 && jobAttributes._6 > 0.0){
      val alphaCpu = cellState.availableCpus / getNormalDistributionInverseCummulativeProbability(jobAttributes._5, jobAttributes._6, normalThreshold)
      val alphaMem = cellState.availableMem / getNormalDistributionInverseCummulativeProbability(jobAttributes._3, jobAttributes._4, normalThreshold)
      var beta = getNormalDistributionInverseCummulativeProbability(jobAttributes._1, jobAttributes._2, 1-normalThreshold)
      if (beta < 0 )
        beta = 0.1 
      val prob = getGammaDistributionCummulativeProbability( Math.min(alphaCpu,alphaMem), beta , ts)
      should = prob <= threshold
  }
    should
  }

  override val name: String = ("gamma-normal-power-off-decision-with-normal-threshold:%f-and-gamma-threshold:%f-and-window-size:%d-and-ts:%f").format(normalThreshold,threshold,windowSize,ts)
}
