Subquery Test

Test out Postgres JDBC's getSubqueries() call, to see what it calls a subquery.

Run with `gradle run`. Postgres must be running. Connection details are
hard-coded in `SubqueryTest.java`.

Output:

    Query:
    select 1; select 2

    Subqueries:
    select 1
     select 2

    ---

    Query:
    WITH regional_sales AS ( SELECT region, SUM(amount) AS total_sales FROM
    orders GROUP BY region  ), top_regions AS ( SELECT region FROM
    regional_sales WHERE total_sales > (SELECT SUM(total_sales)/10 FROM
    regional_sales)  ) SELECT region,  product,  SUM(quantity) AS product_units,
    SUM(amount) AS product_sales FROM orders WHERE region IN (SELECT region FROM
    top_regions) GROUP BY region, product

    No subqueries.

    ---

    Query:
    select count(*) from ( select distinct "x", "y" from
    "test-database"."my-table" ) xyz

    No subqueries.

    ---

