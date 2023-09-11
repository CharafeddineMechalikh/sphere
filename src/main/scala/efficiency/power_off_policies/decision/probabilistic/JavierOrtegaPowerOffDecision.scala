package efficiency.power_off_policies.decision.probabilistic

import ClusterSchedulingSimulation.{CellState, ExpExpExpWorkloadGenerator}
import ClusterSchedulingSimulation.Workloads.tasksHeterogeneity
import efficiency.DistributionUtils
import efficiency.power_off_policies.decision.PowerOffDecision

/**
 * Created by dfernandez on 22/1/16.
 */
class JavierOrtegaPowerOffDecision(threshold : Double, windowSize: Int, ts : Double = 30.0, numSimulations : Int) extends PowerOffDecision with DistributionUtils{

  override def shouldPowerOff(cellState: CellState, machineID: Int): Boolean = {
      val allPastTuples = getPastTuples(cellState, windowSize)
    var should = false
    val jobAttributes = getJobAttributes(allPastTuples)
    var results = Array.fill(numSimulations){false}


    for(i <- 0 to numSimulations-1){
      if(jobAttributes._1 > 0.0 && jobAttributes._3 > 0.0 && jobAttributes._5 > 0.0 && jobAttributes._7 > 0.0 && jobAttributes._9 > 0.0){
        val workloadGenerator = new ExpExpExpWorkloadGenerator(workloadName = "Batch".intern(),
          initAvgJobInterarrivalTime = jobAttributes._1,
          avgTasksPerJob = jobAttributes._7,
          avgJobDuration = jobAttributes._9,
          avgCpusPerTask = jobAttributes._5,
          avgMemPerTask = jobAttributes._3,
          heterogeneousTasks = false)

        val newWorkload = workloadGenerator.newWorkload(timeWindow = ts)
        val jobs = newWorkload.getJobs
        val shouldIteration = jobs.map(job => job.cpusPerTask * job.numTasks).sum < cellState.availableCpus
        results(i) = shouldIteration;
      }
    }
    should = results.count(_ == true) / numSimulations > threshold
    should

  }




  override val name: String = ("javier-ortega-off-threshold:%f-window:%d-ts:%f-numSim:%d").format(threshold,windowSize,ts,numSimulations)
}
