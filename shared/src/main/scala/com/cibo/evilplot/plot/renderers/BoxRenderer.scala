package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Align, BorderRect, Drawable, Extent, Line, Rect, StrokeStyle}
import com.cibo.evilplot.numeric.BoxPlotSummaryStatistics

trait BoxRenderer extends {
  def render(summary: BoxPlotSummaryStatistics, extent: Extent, index: Int): Drawable
}

object BoxRenderer {
  private val defaultStrokeWidth: Double = 2
  def default(strokeWidth: Double = defaultStrokeWidth, pathColor: Color = DefaultColors.pathColor,
              fillColor: Color = DefaultColors.fillColor): BoxRenderer = new BoxRenderer {
    def render(summary: BoxPlotSummaryStatistics, extent: Extent, index: Int): Drawable = {
      val scale = extent.height / (summary.upperWhisker - summary.lowerWhisker)
      val topWhisker = summary.upperWhisker - summary.upperQuantile
      val uppperToMiddle = summary.upperQuantile - summary.middleQuantile
      val middleToLower = summary.middleQuantile - summary.lowerQuantile
      val bottomWhisker = summary.lowerQuantile - summary.lowerWhisker

      Align.center(
        StrokeStyle(Line(scale * topWhisker, strokeWidth), pathColor).rotated(90),
        BorderRect.filled(extent.width, scale * uppperToMiddle).colored(pathColor).filled(fillColor),
        BorderRect.filled(extent.width, scale * middleToLower).colored(pathColor).filled(fillColor),
        StrokeStyle(Line(scale * bottomWhisker, strokeWidth), pathColor).rotated(90)
      ).reduce(_ above _)
    }
  }
}