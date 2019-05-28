package main.scala.types

case class TriggerTimeout(
  height:   Long,
  round:    Int,
  duration: Duration,
  timeout:  Timeout)