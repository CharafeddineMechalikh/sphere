package efficiency.pick_cellstate_resources

import ClusterSchedulingSimulation.{Scheduler, CellState, Job}

import scala.collection.mutable.IndexedSeq
import scala.util.control.Breaks

/**
 * Created by dfernandez on 11/1/16.
 */
// This picker doesn't take into account yet shutted down machines nor capacity security margins nor performance 
object BasicReversePicker extends CellStateResourcesPicker{
  override def pickResource(cellState: CellState, job: Job, candidatePool: IndexedSeq[Int], remainingCandidates: Int) = {
    var machineID = -1
    var numTries =0
    var remainingCandidatesVar = remainingCandidates
    val loop = new Breaks;
    loop.breakable {
      for( i <- cellState.numMachines-1 to 0 by -1){
        if (cellState.availableCpusPerMachine(cellState.machinesLoad(i)) >= (job.cpusPerTask + 0.01) && cellState.availableMemPerMachine(cellState.machinesLoad(i)) >= (job.memPerTask + 0.01)) {
          machineID=cellState.machinesLoad(i)
          assert(cellState.isMachineOn(machineID), "Trying to pick a powered off machine with picker : "+name)
          loop.break;
        }
        else{
          numTries+=1
          remainingCandidatesVar -= 1 // This is irrelevant in this implementation, as derivable of numTries. I'll use it in quicksort-like implementations
        }

      }
    }
    new Tuple4(machineID, numTries, remainingCandidatesVar, candidatePool)
  }
  override val name: String = "basic-reverse-picker"
}
