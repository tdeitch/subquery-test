package subquerytest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.postgresql.core.Query;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

public final class SubqueryTest {
  public static void main(String[] args) {
    SubqueryRunner runner = new SubqueryRunner("localhost", 5432, "tdeitch", "tdeitch");
    List<String> queriesToTest = Arrays.asList(
      "select 1; select 2;",
      "WITH regional_sales AS ( SELECT region, SUM(amount) AS total_sales FROM orders GROUP BY region  ), top_regions AS ( SELECT region FROM regional_sales WHERE total_sales > (SELECT SUM(total_sales)/10 FROM regional_sales)  ) SELECT region,  product,  SUM(quantity) AS product_units,  SUM(amount) AS product_sales FROM orders WHERE region IN (SELECT region FROM top_regions) GROUP BY region, product;", // https://www.postgresql.org/docs/9.1/queries-with.html
      "select count(*) from ( select distinct \"x\", \"y\" from \"test-database\".\"my-table\" ) xyz"
    );

    for (String query: queriesToTest) {
      System.out.println(runner.getSubqueryReport(query));
      System.out.println("---\n");
    }
  }
}

final class SubqueryRunner {
  private PgConnection conn;

  SubqueryRunner(String host, int port, String database, String user) {
    try {
      HostSpec[] hostSpecs = new HostSpec[1];
      hostSpecs[0] = new HostSpec(host, port);
      conn = new PgConnection(hostSpecs, user, database, new Properties(),
          String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
    }
    catch (SQLException e) {
      throw new RuntimeException("Failed to connect to Postgres database", e);
    }
  }

  private Query getQuery(String queryStr) {
    try {
      return conn.createQuery(queryStr, false, true).query;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create query", e);
    }
  }

  String getSubqueryReport(String queryStr) {
    Query bigQuery = getQuery(queryStr);
    StringBuilder result = new StringBuilder("Query:\n");
    result.append(bigQuery.toString());

    Query[] subqueries = bigQuery.getSubqueries();
    if (subqueries == null) {
      result.append("\n\nNo subqueries.\n");
      return result.toString();
    }

    result.append("\n\nSubqueries:\n");
    for (Query subquery : subqueries) {
      result.append(subquery.toString());
      result.append("\n");
    }
    return result.toString();
  }
}
