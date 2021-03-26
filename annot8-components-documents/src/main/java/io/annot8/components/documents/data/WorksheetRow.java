/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class WorksheetRow implements io.annot8.common.data.content.Row {
  private final List<Object> cells;
  private final int index;
  private final List<String> columnNames;

  public WorksheetRow(Row row, int index, List<String> columnNames) {
    List<Object> lRow = new ArrayList<>(row.getLastCellNum());
    for (int i = 0; i < row.getLastCellNum(); i++) {
      Cell cell = row.getCell(i);
      if (cell == null) {
        lRow.add(null);
      } else {
        lRow.add(getCellValue(cell, cell.getCellType()));
      }
    }

    cells = Collections.unmodifiableList(lRow);
    this.index = index;
    this.columnNames = Collections.unmodifiableList(columnNames);
  }

  @Override
  public List<String> getColumnNames() {
    return columnNames;
  }

  @Override
  public int getColumnCount() {
    return cells.size();
  }

  @Override
  public int getRowIndex() {
    return index;
  }

  @Override
  public Optional<Object> getValueAt(int index) {
    if (index < 0) throw new IllegalArgumentException("Index must be greater than 0");

    if (index >= cells.size()) return Optional.empty();

    return Optional.ofNullable(cells.get(index));
  }

  public boolean isEmpty() {
    if (cells == null || cells.isEmpty()) return true;

    if (cells.stream().noneMatch(Objects::nonNull)) return true;

    return cells.stream().map(Object::toString).allMatch(String::isEmpty);
  }

  private Object getCellValue(Cell cell, CellType cellType) {
    switch (cellType) {
      case BOOLEAN:
        return cell.getBooleanCellValue();
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          // TODO: If formatted to just display date, then just return LocalDate instead?
          // TODO: Also need to consider durations, and just times
          // TODO: Can we handle times with an offset?
          return cell.getLocalDateTimeCellValue();
        } else {
          return cell.getNumericCellValue();
        }
      case FORMULA:
        return getCellValue(cell, cell.getCachedFormulaResultType());
      case ERROR:
      case _NONE:
      case BLANK:
      default:
        return null;
    }
  }
}
