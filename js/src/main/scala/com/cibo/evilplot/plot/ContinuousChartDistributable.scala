package com.cibo.evilplot.plot

import com.cibo.evilplot.{Text, Utils}
import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.geometry.{Above, Align, Beside, Drawable, EmptyDrawable, Extent, Line, WrapDrawable}
import com.cibo.evilplot.numeric.{AxisDescriptor, Bounds}

// TODO: ChartDistributable is not really a useful abstraction. Most of the code is the same.
object ContinuousChartDistributable {
  /* Base trait for axes and grid lines. */
  trait ContinuousChartDistributableBase extends WrapDrawable {
    private[plot] val axisDescriptor: AxisDescriptor
    protected val distributableDimension: Double
    protected val tickThick = 1
    protected val tickLength = 5
    private[plot] lazy val bounds: Bounds = axisDescriptor.axisBounds
    private[plot] lazy val pixelsPerUnit: Double = distributableDimension / bounds.range

    def getLinePosition(coord: Double, distributableDimension: Double): Double =
      (coord - bounds.min) * pixelsPerUnit

  }

  case class XAxis(distributableDimension: Double, axisDescriptor: AxisDescriptor,
                   label: Option[String] = None, drawTicks: Boolean = true) extends ContinuousChartDistributableBase {
      lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 22))
      private val _ticks = for {
        numTick <- 0 until axisDescriptor.numTicks
        coordToDraw = axisDescriptor.axisBounds.min + numTick * axisDescriptor.spacing
        label = Utils.createNumericLabel(coordToDraw, axisDescriptor.numFrac)
        tick = new VerticalTick(tickLength, tickThick, Some(label))

        padLeft = getLinePosition(coordToDraw, distributableDimension) - tick.extent.width / 2.0
      } yield tick padLeft padLeft
      lazy val _drawable = Align.center(_ticks.group, text).reduce(Above)
      override def drawable: Drawable = if (drawTicks) _drawable padTop 2 else EmptyDrawable()
  }

  case class YAxis(distributableDimension: Double, axisDescriptor: AxisDescriptor,
                   label: Option[String] = None, drawTicks: Boolean = true) extends ContinuousChartDistributableBase {
      private lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 20) rotated 270)
      private val _ticks = for {
        numTick <- (axisDescriptor.numTicks - 1) to 0 by -1
        coordToDraw = axisDescriptor.tickMin + numTick * axisDescriptor.spacing
        label = Utils.createNumericLabel(coordToDraw, axisDescriptor.numFrac)
        tick = new HorizontalTick(tickLength, tickThick, Some(label))

        padTop = distributableDimension - getLinePosition(coordToDraw, distributableDimension) - tick.extent.height / 2.0
      } yield tick padTop padTop

      private lazy val _drawable = Align.middle(text padRight 10, Align.rightSeq(_ticks).group).reduce(Beside)
      override def drawable: Drawable = if (drawTicks) _drawable padRight 2 else EmptyDrawable()
  }


  trait GridLines extends ContinuousChartDistributableBase {
    val lineSpacing: Double
    private[plot] val nLines: Int = math.ceil(bounds.range / lineSpacing).toInt
    protected val chartAreaSize: Extent // size of area in which to draw the grid lines.

    protected val minGridLineCoord: Double = axisDescriptor.tickMin
  }

  case class VerticalGridLines(chartAreaSize: Extent, axisDescriptor: AxisDescriptor, lineSpacing: Double,
                                        color: Color = White) extends GridLines {
    protected val distributableDimension: Double = chartAreaSize.width
    // should this requirement be a thing?
    require(nLines != 0)
    private val lines = for {
      nLine <- 0 until nLines
      line = Line(chartAreaSize.height, 1) rotated 90 colored color
      lineWidthCorrection = line.extent.width / 2.0
      padding = getLinePosition(minGridLineCoord + nLine * lineSpacing, chartAreaSize.height) - lineWidthCorrection
    } yield { line padLeft padding }

    override def drawable: Drawable = lines.group
  }

  case class HorizontalGridLines(chartAreaSize: Extent, axisDescriptor: AxisDescriptor,
                                 lineSpacing: Double, color: Color = White) extends GridLines {
      protected val distributableDimension: Double = chartAreaSize.height
      require(nLines != 0) // ditto for this one
      private val lines = for {
        nLines <- (nLines - 1) to 0 by -1
        line = Line(chartAreaSize.width, 1) colored color
        lineCorrection = line.extent.height / 2.0
        padding = chartAreaSize.height - getLinePosition(minGridLineCoord + nLines * lineSpacing, chartAreaSize.width) -
          lineCorrection
      } yield line padTop padding
      override def drawable: Drawable = lines.group
  }

  // TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
  case class MetricLines(chartAreaSize: Extent, axisDescriptor: AxisDescriptor,
                         linesToDraw: Seq[Double], color: Color)
    extends ContinuousChartDistributableBase {
    val distributableDimension = chartAreaSize.width
      val lines = for {
        line <- linesToDraw
        padLeft = (line - bounds.min) * pixelsPerUnit
      } yield Line(chartAreaSize.height, 2) colored color rotated 90 padLeft padLeft
      override def drawable: Drawable = lines.group
  }
}