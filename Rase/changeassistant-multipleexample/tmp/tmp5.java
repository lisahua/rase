/** 
 * Returns the paint used to fill an item drawn by the renderer.
 * @param series  the series index (zero-based).
 * @return The paint (never <code>null</code>).
 * @since 1.0.6
 */
public Paint lookupSeriesPaint(int series){
  Paint seriesPaint=getSeriesPaint(series);
  if (seriesPaint == null && this.autoPopulateSeriesPaint) {
    DrawingSupplier supplier=getDrawingSupplier();
    if (supplier != null) {
      seriesPaint=supplier.getNextPaint();
      setSeriesPaint(series,seriesPaint,false);
    }
  }
  if (seriesPaint == null) {
    seriesPaint=this.basePaint;
  }
  return seriesPaint;
}
