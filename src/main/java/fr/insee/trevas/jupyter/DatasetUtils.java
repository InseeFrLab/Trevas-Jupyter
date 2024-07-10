/* (C)2024 */
package fr.insee.trevas.jupyter;

import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.PersistentDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.spark.SparkDataset;

import java.util.Map;

public class DatasetUtils {

	public static String datasetToDisplay(Dataset dataset) throws ClassNotFoundException {
		Map<String, Dataset.Role> roles = dataset.getDataStructure().getRoles();
		SparkDataset sparkDs;
		if (dataset instanceof PersistentDataset) {
			sparkDs = ((SparkDataset) ((PersistentDataset) dataset).getDelegate());
		} else if (dataset instanceof SparkDataset) {
			sparkDs = (SparkDataset) dataset;
		} else {
			throw new ClassNotFoundException(dataset.getClass().getName() +
					" unhandled in datasetToDisplay method");
		}
		var reducedDataset = new SparkDataset(
				sparkDs.getSparkDataset().limit(50),
				roles
		);
		var b = new StringBuilder();
		b.append("<table id='dataset_").append(reducedDataset.hashCode()).append("' class='display'>");
		b.append("<thead>");
		b.append("<tr>");
		reducedDataset.getDataStructure()
				.forEach(
						(name, component) -> {
							b.append("<th>").append(name).append("</th>");
						});
		b.append("</tr>");
		b.append("</thead>");
		b.append("<tbody>");
		reducedDataset.getDataPoints()
				.forEach(
						row -> {
							b.append("<tr>");
							reducedDataset.getDataStructure()
									.keySet()
									.forEach(
											name -> {
												b.append("<td>")
														.append(row.get(name))
														.append("</td>");
											});
							b.append("</tr>");
						});
		b.append("</tbody>");
		b.append("</table>");
		b.append(
				"<script\n"
						+ "  src=\"https://code.jquery.com/jquery-3.6.0.slim.min.js\"\n"
						+ "  integrity=\"sha256-u7e5khyithlIdTpu22PHhENmPcRdFiHRjhAuHcs05RI=\"\n"
						+ "  crossorigin=\"anonymous\"></script>");
		b.append(
				"<link rel=\"stylesheet\" type=\"text/css\""
						+ " href=\"https://cdn.datatables.net/1.12.1/css/jquery.dataTables.css\">\n"
						+ "  \n"
						+ "<script type=\"text/javascript\" charset=\"utf8\""
						+ " src=\"https://cdn.datatables.net/1.12.1/js/jquery.dataTables.js\"></script>\n");
		b.append("<script type=\"text/javascript\">" + "$(document).ready( function () {\n" + "    $('#dataset_").append(reducedDataset.hashCode()).append("').DataTable();\n").append("} );").append("</script>");

		return b.toString();
	}

	public static String datasetMetadataToDisplay(Dataset dataset) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		Structured.DataStructure dataStructure = dataset.getDataStructure();
		dataStructure.forEach(
				(key, value) -> {
					sb.append("<li>")
							.append(key)
							.append(" (")
							.append(value.getRole().name())
							.append(" - ")
							.append(value.getType().getSimpleName());
					String valuedomain = value.getValuedomain();
					if (null != valuedomain) {
						sb.append(" - ").append(valuedomain);
					}
					sb.append(")").append("</li>").append("\n");
				});
		sb.append("</ul>");
		return sb.toString();
	}
}
