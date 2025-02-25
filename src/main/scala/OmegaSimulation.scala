/**
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with
 * the distribution.  Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.  THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ClusterSchedulingSimulation

import efficiency.ordering_cellstate_resources_policies.CellStateResourcesSorter
import efficiency.pick_cellstate_resources.CellStateResourcesPicker
import efficiency.power_off_policies.PowerOffPolicy
import efficiency.power_on_policies.PowerOnPolicy
import stackelberg.StackelbergAgent

import collection.mutable.HashMap

class OmegaSimulatorDesc(
                          val schedulerDescs: Seq[OmegaSchedulerDesc],
                          runTime: Double,
                          val conflictMode: String,
                          val transactionMode: String)
  extends ClusterSimulatorDesc(runTime){
  override
  def newSimulator(constantThinkTime: Double,
                   perTaskThinkTime: Double,
                   blackListPercent: Double,
                   schedulerWorkloadsToSweepOver: Map[String, Seq[String]],
                   workloadToSchedulerMap: Map[String, Seq[String]],
                   cellStateDesc: CellStateDesc,
                   workloads: Seq[Workload],
                   prefillWorkloads: Seq[Workload],
                   logging: Boolean = false,
                   cellStateResourcesSorter: CellStateResourcesSorter,
                   cellStateResourcesPicker: CellStateResourcesPicker,
                   powerOnPolicy: PowerOnPolicy,
                   powerOffPolicy: PowerOffPolicy,
                   securityLevel1Time: Double,
                   securityLevel2Time: Double,
                   securityLevel3Time: Double,
                   stackelbergStrategy: StackelbergAgent): ClusterSimulator = {
    assert(blackListPercent >= 0.0 && blackListPercent <= 1.0)
    var schedulers = HashMap[String, OmegaScheduler]()
    // Create schedulers according to experiment parameters.
    println("Creating %d schedulers.".format(schedulerDescs.length))
    schedulerDescs.foreach(schedDesc => {
      // If any of the scheduler-workload pairs we're sweeping over
      // are for this scheduler, then apply them before
      // registering it.
      var constantThinkTimes = HashMap[String, Double](
          schedDesc.constantThinkTimes.toSeq: _*)
      var perTaskThinkTimes = HashMap[String, Double](
          schedDesc.perTaskThinkTimes.toSeq: _*)
      var newBlackListPercent = 0.0
      if (schedulerWorkloadsToSweepOver
        .contains(schedDesc.name)) {
        newBlackListPercent = blackListPercent
        schedulerWorkloadsToSweepOver(schedDesc.name)
          .foreach(workloadName => {
            constantThinkTimes(workloadName) = constantThinkTime
            perTaskThinkTimes(workloadName) = perTaskThinkTime
          })
      }
      println("Creating new scheduler %s".format(schedDesc.name))
      schedulers(schedDesc.name) =
        new OmegaScheduler(schedDesc.name,
          constantThinkTimes.toMap,
          perTaskThinkTimes.toMap,
          math.floor(newBlackListPercent *
            cellStateDesc.numMachines.toDouble).toInt)
    })
    val cellState = new CellState(cellStateDesc.numMachines,
      cellStateDesc.cpusPerMachine,
      cellStateDesc.memPerMachine,
      conflictMode,
      transactionMode,
      machinesHet = cellStateDesc.machinesHet,
      machEn = cellStateDesc.machEn,
      machPerf = cellStateDesc.machPerf,
      machSec = cellStateDesc.machSec)
    println("Creating new OmegaSimulator with schedulers %s."
      .format(schedulers.values.map(_.toString).mkString(", ")))
    println("Setting OmegaSimulator(%s, %s)'s common cell state to %d"
      .format(conflictMode,
        transactionMode,
        cellState.hashCode))
    new OmegaSimulator(cellState,
      schedulers.toMap,
      workloadToSchedulerMap,
      workloads,
      prefillWorkloads,
      logging,
      cellStateResourcesSorter = cellStateResourcesSorter,
      cellStateResourcesPicker = cellStateResourcesPicker,
      powerOnPolicy = powerOnPolicy,
      powerOffPolicy = powerOffPolicy,
      securityLevel1Time = securityLevel1Time,
      securityLevel2Time = securityLevel2Time,
      securityLevel3Time = securityLevel3Time,
      stackelbergStrategy = stackelbergStrategy)
  }
}

/**
 * A simple subclass of SchedulerDesc for extensibility to
 * for symmetry in the naming of the type so that we don't
 * have to use a SchedulerDesc for an OmegaSimulator.
 */
class OmegaSchedulerDesc(name: String,
                         constantThinkTimes: Map[String, Double],
                         perTaskThinkTimes: Map[String, Double])
  extends SchedulerDesc(name,
    constantThinkTimes,
    perTaskThinkTimes)

class OmegaSimulator(cellState: CellState,
                     override val schedulers: Map[String, OmegaScheduler],
                     workloadToSchedulerMap: Map[String, Seq[String]],
                     workloads: Seq[Workload],
                     prefillWorkloads: Seq[Workload],
                     logging: Boolean = false,
                     monitorUtilization: Boolean = true,
                     cellStateResourcesSorter: CellStateResourcesSorter,
                     cellStateResourcesPicker: CellStateResourcesPicker,
                     powerOnPolicy: PowerOnPolicy,
                     powerOffPolicy: PowerOffPolicy,
                     securityLevel1Time: Double,
                     securityLevel2Time: Double,
                     securityLevel3Time: Double,
                     stackelbergStrategy: StackelbergAgent)
  extends ClusterSimulator(cellState,
    schedulers,
    workloadToSchedulerMap,
    workloads,
    prefillWorkloads,
    logging,
    monitorUtilization,
    cellStateResourcesSorter = cellStateResourcesSorter,
    cellStateResourcesPicker = cellStateResourcesPicker,
    powerOnPolicy = powerOnPolicy,
    powerOffPolicy = powerOffPolicy,
    securityLevel1Time = securityLevel1Time,
    securityLevel2Time = securityLevel2Time,
    securityLevel3Time = securityLevel3Time,
    stackelbergStrategy = stackelbergStrategy) {
  // Set up a pointer to this simulator in each scheduler.
  schedulers.values.foreach(_.omegaSimulator = this)
}

/**
 * While an Omega Scheduler has jobs in its job queue, it:
 * 1: Syncs with cell state by getting a new copy of common cell state
 * 2: Schedules the next job j in the queue, using getThinkTime(j) seconds
 *    and assigning creating and applying one delta per task in the job.
 * 3: submits the job to CellState
 * 4: if any tasks failed to schedule: insert job at back of queue
 * 5: rolls back its changes
 * 6: repeat, starting at 1
 */
class OmegaScheduler(name: String,
                     constantThinkTimes: Map[String, Double],
                     perTaskThinkTimes: Map[String, Double],
                     numMachinesToBlackList: Double = 0)
  extends Scheduler(name,
    constantThinkTimes,
    perTaskThinkTimes,
    numMachinesToBlackList) {
  println("scheduler-id-info: %d, %s, %d, %s, %s"
    .format(Thread.currentThread().getId(),
      name,
      hashCode(),
      constantThinkTimes.mkString(";"),
      perTaskThinkTimes.mkString(";")))
  
  var omegaSimulator: OmegaSimulator = null
  var privateCellState: CellState = null

  override
  def checkRegistered = {
    super.checkRegistered
    assert(omegaSimulator != null, "This scheduler has not been added to a " +
      "simulator yet.")
  }

  def incrementDailycounter(counter: HashMap[Int, Int]) = {
    val index: Int = math.floor(simulator.currentTime / 86400).toInt
    val currCount: Int = counter.getOrElse(index, 0)
    counter(index) = currCount + 1
  }

  // When a job arrives, start scheduling, or make sure we already are.
  override
  def addJob(job: Job) = {
    assert(simulator != null, "This scheduler has not been added to a " +
      "simulator yet.")

    assert(job.unscheduledTasks > 0)
    super.addJob(job)
    pendingQueue.enqueue(job)
    simulator.log("Scheduler %s enqueued job %d of workload type %s."
      .format(name, job.id, job.workloadName))
    if (!scheduling) {
      omegaSimulator.log("Set %s scheduling to TRUE to schedule job %d."
        .format(name, job.id))
      scheduling = true
      handleJob(pendingQueue.dequeue)
    }
  }

  /**
   * Schedule job and submit a transaction to common cellstate for
   * it. If not all tasks in the job are successfully committed,
   * put it back in the pendingQueue to be scheduled again.
   */
  def handleJob(job: Job): Unit = {
    job.updateTimeInQueueStats(simulator.currentTime)
    syncCellState
    val jobThinkTime = getThinkTime(job)
    omegaSimulator.afterDelay(jobThinkTime) {
      job.numSchedulingAttempts += 1
      job.numTaskSchedulingAttempts += job.unscheduledTasks
      // Schedule the job in private cellstate.
      assert(job.unscheduledTasks > 0)
      val claimDeltas = scheduleJob(job, privateCellState) 
      var commitedDelta = Seq[ClaimDelta]()
      var conflictedDelta = Seq[ClaimDelta]()
      simulator.log(("Job %d (%s) finished %f seconds of scheduling " +
        "thinktime; now trying to claim resources for %d " +
        "tasks with %f cpus and %f mem each.")
        .format(job.id,
          job.workloadName,
          jobThinkTime,
          job.numTasks,
          job.cpusPerTask,
          job.memPerTask))
      if (claimDeltas.length > 0) {
        // Attempt to claim resources in common cellstate by committing
        // a transaction.
        omegaSimulator.log("Submitting a transaction for %d tasks for job %d."
          .format(claimDeltas.length, job.id))
        val commitResult = omegaSimulator.cellState.commit(claimDeltas, true)
        commitedDelta = commitResult.committedDeltas
        conflictedDelta = commitResult.conflictedDeltas
        job.unscheduledTasks -= commitResult.committedDeltas.length
        omegaSimulator.log("%d tasks successfully committed for job %d."
          .format(commitResult.committedDeltas.length, job.id))
        numSuccessfulTaskTransactions += commitResult.committedDeltas.length
        numFailedTaskTransactions += commitResult.conflictedDeltas.length
        if (job.numSchedulingAttempts > 1)
          numRetriedTransactions += 1

        // Record job-level stats.
        if (commitResult.conflictedDeltas.length == 0) {
          numSuccessfulTransactions += 1
          incrementDailycounter(dailySuccessTransactions)
          recordUsefulTimeScheduling(job,
            jobThinkTime,
            job.numSchedulingAttempts == 1)
        } else {
          numFailedTransactions += 1
          incrementDailycounter(dailyFailedTransactions) 
          recordWastedTimeScheduling(job,
            jobThinkTime,
            job.numSchedulingAttempts == 1)
         
        }
      } else {
        simulator.log(("Not enough resources of the right shape were " +
          "available to schedule even one task of job %d, " +
          "so not submitting a transaction.").format(job.id))
        numNoResourcesFoundSchedulingAttempts += 1
      }

      var jobEventType = "" // Set this conditionally below; used in logging.
      // If the job isn't yet fully scheduled, put it back in the queue.
      if (job.unscheduledTasks > 0) {
        // Give up on a job if (a) it hasn't scheduled a single task in
        // 100 tries or (b) it hasn't finished scheduling after 1000 tries.
        if ((job.numSchedulingAttempts > 100 &&
          job.unscheduledTasks == job.numTasks) ||
          job.numSchedulingAttempts > 1000) {
          println(("Abandoning job %d (%f cpu %f mem) with %d/%d " +
            "remaining tasks, after %d scheduling " +
            "attempts.").format(job.id,
            job.cpusPerTask,
            job.memPerTask,
            job.unscheduledTasks,
            job.numTasks,
            job.numSchedulingAttempts))
          numJobsTimedOutScheduling += 1
          jobEventType = "abandoned"
        } else {
          simulator.log(("Job %d still has %d unscheduled tasks, adding it " +
            "back to scheduler %s's job queue.")
            .format(job.id, job.unscheduledTasks, name))
          if((simulator.cellState.numberOfMachinesOn) < simulator.cellState.numMachines){
            recordWastedTimeSchedulingPowering(job, jobThinkTime + (simulator.cellState.powerOnTime/4+0.1))
            simulator.afterDelay(simulator.cellState.powerOnTime/4+0.1) {
              addJob(job)
            }
          }
          else{
            simulator.afterDelay(1) {
              addJob(job)
            }
          }
        }
      } else {
        // All tasks in job scheduled so don't put it back in pendingQueue.
        jobEventType = "fully-scheduled"
      } 
      if(omegaSimulator.cellState.numberOfMachinesOn < omegaSimulator.cellState.numMachines){
        simulator.powerOn.powerOn(omegaSimulator.cellState, job, "omega", commitedDelta, conflictedDelta)
      }
      if (!jobEventType.equals("")) {
       
      }

      omegaSimulator.log("Set " + name + " scheduling to FALSE")
      scheduling = false
      // Keep trying to schedule as long as we have jobs in the queue.
      if (!pendingQueue.isEmpty) {
        scheduling = true
        handleJob(pendingQueue.dequeue)
      }
    }
  }

  def syncCellState {
    checkRegistered
    privateCellState = omegaSimulator.cellState.copy
    simulator.log("%s synced private cellstate.".format(name)) 
  }
}
