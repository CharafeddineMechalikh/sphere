package efficiency

import ClusterSchedulingSimulation.{CellState, Job}
import org.apache.commons.math.distribution.{ExponentialDistributionImpl, GammaDistributionImpl, NormalDistributionImpl}
import scala.util.control.Breaks._
/**
  * Created by dfernandez on 18/2/16.
  */
trait DistributionUtils {
  def getGammaDistributionCummulativeProbability(alpha: Double, beta: Double, ts: Double): Double ={
    var probability = 0.0
    if(alpha / (ts/beta) <= 10.00){
      DistributionCache.gammaDistributionCacheCalls += 1 
      var a = BigDecimal(alpha).setScale(2, BigDecimal.RoundingMode.FLOOR).toDouble
      if(a <= 0.01){
        a = 0.01
      }
      var b = BigDecimal(beta).setScale(1, BigDecimal.RoundingMode.FLOOR).toDouble
      if(b <= 0.1){
        b = 0.1
      }
      var prob = None : Option[Double]
      if(DistributionCache.gammaDistributionCache.get((a, b, ts)) == null){
        prob = Some(generateGammaDistributionCummulativeProbability(a, b, ts))
        DistributionCache.gammaDistributionCache.put((a, b, ts), prob.get)
      }
      else{
        prob = Some(DistributionCache.gammaDistributionCache.get((a, b, ts)))
      }
      probability = prob.get
    }
    probability
  }

  def getExponentialDistributionCummulativeProbability(interArrival: Double, ts: Double): Double ={
    var probability = 0.0
    var interArr = BigDecimal(interArrival).setScale(1, BigDecimal.RoundingMode.FLOOR).toDouble
    DistributionCache.exponentialDistributionCacheCalls += 1
    if(interArr <= 0.1){
      interArr = 0.1
    }
    var prob = None : Option[Double]
    if(DistributionCache.exponentialDistributionCache.get((interArr, ts)) == null){
      prob = Some(generateExponentialDistributionCummulativeProbability(interArr, ts))
      DistributionCache.exponentialDistributionCache.put((interArr, ts), prob.get)
    }
    else{
      prob = Some(DistributionCache.exponentialDistributionCache.get(interArr, ts))
    }
    probability = prob.get
    probability
  }

  /*def getGammaDistributionCummulativeProbability(alpha: Double, beta: Double, ts: Double): Double ={
    DistributionCache.gammaDistributionCacheCalls += 1 
    var prob = None : Option[Double]
    if(DistributionCache.gammaDistributionCache.get(("%.1f".format(alpha).toDouble, "%.4f".format(alpha).toDouble, ts)) == null){
      prob = Some(generateGammaDistributionCummulativeProbability("%.1f".format(alpha).toDouble, "%.4f".format(alpha).toDouble, ts))
      DistributionCache.gammaDistributionCache.put(("%.1f".format(alpha).toDouble, "%.4f".format(alpha).toDouble, ts), prob.get)
    }
    else{
      prob = Some(DistributionCache.gammaDistributionCache.get(("%.1f".format(alpha).toDouble, "%.4f".format(alpha).toDouble, ts)))
    }
    prob.get
  }*/

  def generateGammaDistributionCummulativeProbability(alpha: Double, beta: Double, ts: Double): Double ={
    DistributionCache.gammaDistributionCacheMiss += 1
    new GammaDistributionImpl(alpha, beta).cumulativeProbability(ts)
  }

  def generateExponentialDistributionCummulativeProbability(interArrival: Double, ts: Double): Double ={
    DistributionCache.exponentialDistributionCacheMiss += 1
    new ExponentialDistributionImpl(interArrival).cumulativeProbability(ts)
  }


  def getPastTuples(cellState: CellState, windowSize: Int): Seq[Tuple2[Double, Job]] ={
  
    val jobCacheLength = cellState.simulator.jobCache.length
    if(jobCacheLength > windowSize+1){
      cellState.simulator.jobCache.slice(jobCacheLength-(windowSize+1), jobCacheLength)
    }
    else{
      cellState.simulator.jobCache
    }
  }


  def getPastTuplesTime(cellState: CellState, timeWindow: Double): Seq[Tuple2[Double, Job]] ={
    val jobCacheLength = cellState.simulator.jobCache.length
    var numElements = 0
    breakable {
      for (i <- jobCacheLength-1 to 0 by -1){
        if (cellState.simulator.jobCache(i)._1 > cellState.simulator.currentTime - timeWindow){
          numElements += 1
        }
        else{
          break
        }
      }
    }
   
    cellState.simulator.jobCache.slice(jobCacheLength-(numElements+1), jobCacheLength)
  }

  def getJobAttributes(pastTuples : Seq[Tuple2[Double, Job]]): Tuple10[Double, Double, Double, Double, Double, Double, Double, Double, Double, Double] ={
    DistributionCache.jobAttributesCacheCalls += 1
    val jobId = pastTuples.map(_._2).map(_.id)
   
    var jobAttrs = DistributionCache.jobAttributesCache.get(jobId)
    if(jobAttrs == null){
      jobAttrs = generateJobAtributes(pastTuples)
      DistributionCache.jobAttributesCache.put(jobId, jobAttrs)
    }
    jobAttrs
  }

  def generateJobAtributes(allPastTuples : Seq[Tuple2[Double, Job]]): Tuple10[Double, Double, Double, Double, Double, Double, Double, Double, Double, Double] ={
    val allPastTuplesLength = allPastTuples.length
    DistributionCache.jobAttributesCacheMiss += 1
    val arraySize = if (allPastTuplesLength > 0) allPastTuplesLength-1 else 0
    val numTasks = new Array[Double](arraySize)
    val duration = new Array[Double](arraySize)
    val interArrival = new Array[Double](arraySize)
    val memConsumed = new Array[Double](arraySize)
    val cpuConsumed = new Array[Double](arraySize)
    for(i <- 1 to allPastTuplesLength-1){
      numTasks(i-1) =  allPastTuples(i)._2.numTasks
      duration(i-1) = allPastTuples(i)._2.taskDuration
      interArrival(i-1) = (allPastTuples(i)._1 - allPastTuples(i-1)._1)
      memConsumed(i-1) = allPastTuples(i)._2.numTasks*allPastTuples(i)._2.memPerTask
      cpuConsumed(i-1) = allPastTuples(i)._2.numTasks*allPastTuples(i)._2.cpusPerTask
    }
    val interArrivalTuple = meanAndStdDev(interArrival)
    val durationTuple = meanAndStdDev(duration)
    val memTuple = meanAndStdDev(memConsumed)
    val cpuTuple = meanAndStdDev(cpuConsumed)
    val numTasksTuple = meanAndStdDev(numTasks)
    (interArrivalTuple._1, interArrivalTuple._2, memTuple._1, memTuple._2, cpuTuple._1, cpuTuple._2, numTasksTuple._1, numTasksTuple._2, durationTuple._1, durationTuple._2)
  }

  def mean[T](item:Traversable[T])(implicit n:Numeric[T]) = {
    n.toDouble(item.sum) / item.size.toDouble
  }

  def variance[T](items:Traversable[T])(implicit n:Numeric[T]) : Double = {
    val itemMean = mean(items)
    val count = items.size
    val sumOfSquares = items.foldLeft(0.0d)((total,item)=>{
      val itemDbl = n.toDouble(item)
      val square = math.pow(itemDbl - itemMean,2)
      total + square
    })
    sumOfSquares / count.toDouble
  }

  def stddev[T](items:Traversable[T])(implicit n:Numeric[T]) : Double = {
    math.sqrt(variance(items))
  }

  def meanAndStdDev[T](items:Traversable[T])(implicit n:Numeric[T]) : Tuple2[Double, Double] = {
    val itemMean = mean(items)
    val count = items.size
    val sumOfSquares = items.foldLeft(0.0d)((total,item)=>{
      val itemDbl = n.toDouble(item)
      val square = math.pow(itemDbl - itemMean,2)
      total + square
    })
    val variance = sumOfSquares / count.toDouble
    val stddev = math.sqrt(variance)
    (itemMean, stddev)
  }

  def getNormalDistributionInverseCummulativeProbability(normalAvg: Double, normalStdDev: Double, normalThreshold: Double): Double ={
    DistributionCache.normalDistributionCacheCalls += 1
       val av = BigDecimal(normalAvg).setScale(4, BigDecimal.RoundingMode.FLOOR).toDouble
    val ns = BigDecimal(normalStdDev).setScale(4, BigDecimal.RoundingMode.FLOOR).toDouble
    var prob = DistributionCache.normalDistributionCache.get((av, ns, normalThreshold))
    if(prob == 0.0){
      prob = generateNormalDistributionInverseCummulativeProbability(av, ns, normalThreshold)
      DistributionCache.normalDistributionCache.put((av, ns, normalThreshold), prob)
    }
    prob
  }

  

  def generateNormalDistributionInverseCummulativeProbability(normalAvg: Double, normalStdDev: Double, normalThreshold: Double): Double ={
    DistributionCache.normalDistributionCacheMiss += 1
    new NormalDistributionImpl(normalAvg, normalStdDev).inverseCumulativeProbability(normalThreshold)
  }
}
