package csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

public class CSVFile {

	public final String file;

	public CSVFile(String file) {
		this.file = file;
	}

	public static int readCSV(String file, String columnName, String localname)
			throws FileNotFoundException, IOException {
		int result = 0;
		// read file widh CSVReader class
		try (CSVReader csvReader = new CSVReader(new FileReader(file))) {
			String[] values = null;
			int indexColumn = 0;
			while ((values = csvReader.readNext()) != null) {
				String lines = values[0];
				// get the index of the required column name
				// check if line has column name -> firstline
				// then split the string in columns
				// then loop over string array and check if value equals column name
				if (lines.indexOf(columnName) != -1) {
					String[] column = lines.split(";");
					for (int i = 0; i < column.length; i++) {
						if (column[i].equals(columnName)) {
							indexColumn = i;
						}
					}
				}
				// split string in columns and store the column with names to string name
				String[] column = lines.split(";");
				String name = column[0];

				// catch german Sonderzeichen for Baden-Württemberg and Thüringen
				if (name.indexOf("Baden") != -1) {
					if (localname.indexOf("Baden") != -1) {
						name = localname;
					}
				}
				if (name.indexOf("Th") != -1) {
					if (localname.indexOf("Th") != -1) {
						name = localname;
					}
				}
				// join of csv file and geojson
				// return the appropriate value
				if (name.equals(localname)) {
					result = Integer.parseInt(column[indexColumn]);
				}
			}
		}
		return result;
	}
}
