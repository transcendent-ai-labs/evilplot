package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.{StrokeStyle, Text, Utils}
import com.cibo.evilplot.geometry.{Above, Align, Drawable, DrawableLater, EmptyDrawable, Extent, Line}

object DiscreteChartDistributable {
  // Returns getters for width and spacing that take extent of the chart in which the drawables are to be distributed
  // and return the width and spacing between the drawables, respectively.
  def widthAndSpacingFunctions(numDrawables: Int, drawableWidth: Option[Double], drawableSpacing: Option[Double]):
  ((Extent => Double), (Extent => Double)) = {
    (drawableWidth, drawableSpacing) match {
      case (None, None) => // Automatically calculate width, spacing.
        val defaultSpacing: Double = 5.0
        ((ext: Extent) => (ext.width - numDrawables * defaultSpacing) / numDrawables,
          (_: Extent) => defaultSpacing)
      case (Some(_width), _) => // Automatically calculate spacing if a width is given, even if spacing is specified.
        ((ext: Extent) => {
          require(numDrawables * _width <= ext.width, f"width ${_width}%.1f is too large for chart of specified size")
          _width
        }, (ext: Extent) => (ext.width - numDrawables * _width) / numDrawables)
      case (None, Some(_spacing)) => // Automatically calculate width if spacing is specified.
        ((ext: Extent) => {
          val remainingSpace: Double = ext.width - numDrawables * _spacing
          require(remainingSpace >= 0, f"spacing ${_spacing}%.1f is too large for chart of specified size")
          remainingSpace / numDrawables
        }, (_: Extent) => _spacing)
    }
  }

  case class XAxis[T](tickNames: Seq[T], widthGetter: (Extent => Double), spacingGetter: (Extent => Double),
                   label: Option[String] = None, rotateText: Double = 0, drawAxis: Boolean = true) extends DrawableLater {
    def apply(extent: Extent): Drawable = {
      lazy val xAxisLabel = Utils.maybeDrawable(label, (msg: String) => Text(msg, 20))
      val spacing: Double = spacingGetter(extent)
      val width: Double = widthGetter(extent)
      val tickSpacing: Double = width + spacing
      val firstTickOffset: Double = tickSpacing / 2.0
      val _ticks = for {
        (name, numTick) <- tickNames.zipWithIndex
        tick = new VerticalTick(5, 1, Some(name.toString), rotateText)
        padLeft = (firstTickOffset + numTick * tickSpacing) - tick.extent.width / 2.0
      } yield tick padLeft padLeft
      if (drawAxis) Align.center(_ticks.group, xAxisLabel).reduce(Above) else EmptyDrawable()
    }
  }

  // For now for discrete data charts just align a vertical gridline with *every* drawable. Later can add an option.
  // This is a purely aesthetic thing anyway.
  case class VerticalGridLines(numLines: Int, widthGetter: (Extent => Double),
                               spacingGetter: (Extent => Double), color: Color = White) extends DrawableLater {
    def apply(extent: Extent): Drawable = {
      val spacing: Double = spacingGetter(extent)
      val width: Double = widthGetter(extent)
      val lineSpacing: Double = width + spacing
      val firstTickOffset: Double = lineSpacing / 2.0
      val _lines = for {
        numLine <- 0 until numLines
        line = StrokeStyle(color)(Line(extent.height, 2)) rotated 90
        padLeft = (firstTickOffset + numLine * lineSpacing) - line.extent.width / 2.0
      } yield line padLeft padLeft
      _lines.group
    }
  }

}
