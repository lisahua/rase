/** 
 * Returns the stroke used to draw the items in a series.
 * @param series  the series (zero-based index).
 * @return The stroke (never <code>null</code>).
 * @since 1.0.6
 */
public Stroke lookupSeriesStroke(int series){
  Stroke result=getSeriesStroke(series);
  if (result == null && this.autoPopulateSeriesStroke) {
    DrawingSupplier supplier=getDrawingSupplier();
    if (supplier != null) {
      result=supplier.getNextStroke();
      setSeriesStroke(series,result,false);
    }
  }
  if (result == null) {
    result=this.baseStroke;
  }
  return result;
}
