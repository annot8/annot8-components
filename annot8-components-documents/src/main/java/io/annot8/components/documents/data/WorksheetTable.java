/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.data;

import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

public class WorksheetTable implements Table {
  private final List<String> columnNames;
  private final int columnCount;
  private final List<Row> rows;

  public WorksheetTable(Sheet sheet, boolean firstRowHeader, int skipRows) {
    Iterator<org.apache.poi.ss.usermodel.Row> rows = sheet.rowIterator();

    int rowIndex = 0;

    List<String> headerColumns;
    for (int i = 0; i < skipRows; i++) {
      if (rows.hasNext()) rows.next();
    }

    if (firstRowHeader && rows.hasNext()) {
      org.apache.poi.ss.usermodel.Row header = rows.next();
      rowIndex++;

      headerColumns = new ArrayList<>(header.getLastCellNum());
      for (int i = 0; i < header.getLastCellNum(); i++) {
        Cell cell = header.getCell(i);
        if (cell == null) {
          headerColumns.add("");
        } else {
          headerColumns.add(cell.getStringCellValue());
        }
      }

    } else {
      headerColumns = new ArrayList<>();
    }

    List<Row> lRows = new ArrayList<>(sheet.getLastRowNum());

    while (rows.hasNext()) {
      WorksheetRow row = new WorksheetRow(rows.next(), rowIndex, headerColumns);
      rowIndex++;

      if (row.isEmpty()) continue;

      lRows.add(row);
    }

    this.columnCount = lRows.stream().mapToInt(Row::getColumnCount).max().orElse(-1);

    if (headerColumns.isEmpty()) {
      for (int i = 0; i < this.columnCount; i++) {
        headerColumns.add("Column " + CellReference.convertNumToColString(i));
      }
    } else if (this.columnCount > headerColumns.size()) {
      for (int i = headerColumns.size(); i < this.columnCount; i++) {
        headerColumns.add("");
      }
    }

    this.columnNames = Collections.unmodifiableList(headerColumns);
    this.rows = Collections.unmodifiableList(lRows);
  }

  @Override
  public int getColumnCount() {
    return columnCount;
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public Optional<List<String>> getColumnNames() {
    return Optional.ofNullable(columnNames);
  }

  @Override
  public Stream<Row> getRows() {
    return rows.stream();
  }
}
