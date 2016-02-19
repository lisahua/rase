/** 
 * Returns the stroke used to outline the items in a series.
 * @param series  the series (zero-based index).
 * @return The stroke (never <code>null</code>).
 * @since 1.0.6
 */
public Stroke lookupSeriesOutlineStroke(int series){
  Stroke result=getSeriesOutlineStroke(series);
  if (result == null && this.autoPopulateSeriesOutlineStroke) {
    DrawingSupplier supplier=getDrawingSupplier();
    if (supplier != null) {
      result=supplier.getNextOutlineStroke();
      setSeriesOutlineStroke(series,result,false);
    }
  }
  if (result == null) {
    result=this.baseOutlineStroke;
  }
  return result;
}
