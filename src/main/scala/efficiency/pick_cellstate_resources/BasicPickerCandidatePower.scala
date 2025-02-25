package efficiency.pick_cellstate_resources

import ClusterSchedulingSimulation.{CellState, Job}

import scala.collection.mutable.IndexedSeq
import scala.util.control.Breaks

/**
 * Created by dfernandez on 11/1/16.
 */
// This picker doesn't take into account yet shutted down machines nor capacity security margins nor performance 
object BasicPickerCandidatePower extends CellStateResourcesPicker{
  override def pickResource(cellState: CellState, job: Job, candidatePool: IndexedSeq[Int], remainingCandidates: Int) = {
    var machineID = -1
    var numTries =0
    var remainingCandidatesVar= remainingCandidates
    val loop = new Breaks;
    loop.breakable {
      for( i <- (cellState.numberOfMachinesOn - remainingCandidatesVar) to cellState.numberOfMachinesOn-1){
        val mID = cellState.machinesLoad(i)
        if (cellState.availableCpusPerMachine(mID) >= (job.cpusPerTask + 0.01) && cellState.availableMemPerMachine(mID) >= (job.memPerTask + 0.01)) {
          machineID=mID
          assert(cellState.isMachineOn(machineID), "Trying to pick a powered off machine with picker : "+name)
          loop.break;
        }
        else{
          numTries+=1
          remainingCandidatesVar -=1 // This is irrelevant in this implementation, as derivable of numTries. I'll use it in quicksort-like implementations
        }

      }
    }
    new Tuple4(machineID, numTries, remainingCandidatesVar, candidatePool)
  }
  override val name: String = "power-picker-candidate"
}
