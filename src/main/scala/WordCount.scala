import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.lang.Double.isNaN

/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 */
object WordCount {

  case class MatchData(id1:Int, id2:Int, scores:Array[Double], matched: Boolean)
  def main(args: Array[String]) {

    var logger = Logger.getLogger(this.getClass())
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf().setAppName("WordCount")
    conf.registerKryoClasses(Array(classOf[org.apache.hadoop.io.LongWritable], classOf[org.apache.hadoop.io.Text]))
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val textFile = sc.textFile("D:\\AAS\\data\\donation\\block_*\\block_*.csv")

    val linesWithoutHeader = textFile.filter(!isheader(_))
    val matchDataRdd = linesWithoutHeader.map(parse)
    matchDataRdd.cache()
    val stats = (0 until 9).map(i =>
      matchDataRdd.map(md => md.scores(i)).filter(!isNaN(_)).stats()
    )
    println(stats(0).toString())
  }

  def isheader(line:String) = line.contains("id_1")
  def toDouble(s: String)= if ("?".equals(s)) Double.NaN else s.toDouble
  def parse(s: String): MatchData = {
    val pieces = s.split(',')
    val id1 = pieces(0).toInt
    val id2 = pieces(1).toInt
    val scores = pieces.slice(2,11).map(toDouble)
    val matched = pieces(11).toBoolean
    MatchData(id1, id2, scores, matched)
  }
}
