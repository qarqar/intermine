package org.flymine.objectstore.query;

import junit.framework.TestCase;

/**
 * Test for testing the parser on the flymine query object.
 *
 * @author Matthew Wakeling
 */
public class QueryParserTest extends TestCase
{
    public QueryParserTest(String arg) {
        super(arg);
    }

    /**
     * Set up all the results expected for a given subset of queries
     */
    public void setUpResults() {
    }

    public void testConstants() throws Exception {
        Query q = new Query("select 1 as b, false as c, true as d, 1.2 as e, 'hello' as f, '2003-04-30 14:12:30.333' as g from Company", "org.flymine.model.testmodel");
        assertEquals("SELECT 1 AS b, false AS c, true AS d, 1.2 AS e, hello AS f, Wed Apr 30 14:12:30 BST 2003 AS g FROM org.flymine.model.testmodel.Company AS Company", q.toString());
    }

    public void testValidPathExpressions() throws Exception {
        Query q = new Query("select subquery.c_.name as a, subquery.b as b from (select c_, c_.name as b from Company as c_) as subquery", "org.flymine.model.testmodel");
        assertEquals("SELECT subquery.c_name AS a, subquery.b AS b FROM (SELECT c_, c_.name AS b FROM org.flymine.model.testmodel.Company AS c_) AS subquery", q.toString());
    }

    public void testInvalidPathExpressions() throws Exception {
        try {
            Query q = new Query("select Company.nonExistentField from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because that field does not exist in a Company object");
        } catch (IllegalArgumentException e) {
            assertEquals("java.lang.NoSuchFieldException: Field nonExistentField not found in class org.flymine.model.testmodel.Company", e.getMessage());
        }
        try {
            Query q = new Query("select Company.name.something from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because the path expression cannot extend beyond a field");
        } catch (IllegalArgumentException e) {
            assertEquals("Path expression Company.name.something extends beyond a field", e.getMessage());
        }
        try {
            Query q = new Query("select c from (select Company from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because the path expression cannot end at a subquery");
        } catch (IllegalArgumentException e) {
            assertEquals("Path expression c cannot end at a subquery", e.getMessage());
        }
        try {
            Query q = new Query("select c.Company from (select Company from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because we cannot reference classes inside subqueries");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot reference classes inside subqueries - only QueryEvaluables, and fields inside classes inside subqueries, for path expression c.Company", e.getMessage());
        }
        try {
            Query q = new Query("select c.Company.nonExistentField from (select Company from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because that field does not exist in a Company object");
        } catch (IllegalArgumentException e) {
            assertEquals("java.lang.NoSuchFieldException: Field nonExistentField not found in class org.flymine.model.testmodel.Company", e.getMessage());
        }
        try {
            Query q = new Query("select c.Company.name.something from (select Company from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because the path expression cannot extend beyond a field");
        } catch (IllegalArgumentException e) {
            assertEquals("Path expression c.Company.name.something extends beyond a field", e.getMessage());
        }
        try {
            Query q = new Query("select c.name.something from (select Company.name as name from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because the path expression cannot extend beyond a field");
        } catch (IllegalArgumentException e) {
            assertEquals("Path expression c.name.something extends beyond a field", e.getMessage());
        }
        try {
            Query q = new Query("select c.subquery from (select subquery.Company.name as name from (select Company from Company) as subquery) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because the path expression references a subquery");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot reference subquery subquery inside subquery c", e.getMessage());
        }
        try {
            Query q = new Query("select c.something from (select Company from Company) as c", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because there is no object c.something");
        } catch (IllegalArgumentException e) {
            assertEquals("No such object something found in subquery c", e.getMessage());
        }
        try {
            Query q = new Query("select c from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because there is no object c");
        } catch (IllegalArgumentException e) {
            assertEquals("No such object c", e.getMessage());
        }
        try {
            Query q = new Query("select a.Company.name as a from (select Company.name as a from Company) as a", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because Company is not in the SELECT list of the subquery a");
        } catch (IllegalArgumentException e) {
            assertEquals("a.Company.name is not available, because Company is not in the SELECT list of subquery a", e.getMessage());
        }
    }
    
    public void testNormalExpressions() throws Exception {
        Query q = new Query("select 1 + Company.vatNumber as a, 3 - 4 as b, 5 * 6 as c, 7 / 8 as d from Company", "org.flymine.model.testmodel");
        assertEquals("SELECT 1 + Company.vatNumber AS a, 3 - 4 AS b, 5 * 6 AS c, 7 / 8 AS d FROM org.flymine.model.testmodel.Company AS Company", q.toString());
    }

    public void testInvalidNormalExpressions() throws Exception {
        try {
            Query q = new Query("select Company + 3 as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because a class cannot appear in an expression");
        } catch (IllegalArgumentException e) {
            assertEquals("Expressions cannot contain classes as arguments", e.getMessage());
        }
        try {
            Query q = new Query("select 1 + 2 + 3 as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because an expression may only have two arguments");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:14: expecting \"as\", found '+'", e.getMessage());
        }
        try {
            Query q = new Query("select 'flibble' + 3 as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because an expression must type-match");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid arguments for specified operation", e.getMessage());
        }
        try {
            Query q = new Query("select Company.name + 3 as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because an expression must type-match");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid arguments for specified operation", e.getMessage());
        }
    }

    public void testSafeFunctions() throws Exception {
        Query q = new Query("select count(*) as a, sum(Company.vatNumber + 3) as b, avg(Company.vatNumber) as c, min(Company.vatNumber) as d, substr('flibble', 3, max(Company.vatNumber)) as e from Company", "org.flymine.model.testmodel");
        assertEquals("SELECT COUNT(*) AS a, SUM(Company.vatNumber + 3) AS b, AVG(Company.vatNumber) AS c, MIN(Company.vatNumber) AS d, SUBSTR(flibble, 3, MAX(Company.vatNumber)) AS e FROM org.flymine.model.testmodel.Company AS Company", q.toString());
    }

    public void testInvalidSafeFunctions() throws Exception {
        try {
            Query q = new Query("select count(5) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because count does not take an argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:14: expecting ASTERISK, found '5'", e.getMessage());
        }
        try {
            Query q = new Query("select sum(5, 3) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because sum only takes one argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:13: expecting CLOSE_PAREN, found ','", e.getMessage());
        }
        try {
            Query q = new Query("select substr('fdsafds', 3, 4, 5) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because substr only takes three arguments");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:30: expecting CLOSE_PAREN, found ','", e.getMessage());
        }
        try {
            Query q = new Query("select max(Company) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because functions cannot have classes as arguments");
        } catch (IllegalArgumentException e) {
            assertEquals("Functions cannot contain classes as arguments", e.getMessage());
        }
        try {
            Query q = new Query("select substr('fdsafds', 3) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because substr takes three arguments");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:27: expecting COMMA, found ')'", e.getMessage());
        }
        try {
            Query q = new Query("select min() as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because min takes an argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception: line 1:8: unexpected token: min", e.getMessage());
        }
        try {
            Query q = new Query("select min(4) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because min's argument must be a field or expression");
        } catch (IllegalArgumentException e) {
            assertEquals("Arguments to aggregate functions may be fields or expressions only", e.getMessage());
        }
        try {
            Query q = new Query("select min(Company.name) as a from Company", "org.flymine.model.testmodel");
            fail("Expected: IllegalArgumentException, because min's argument must be numerical");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid argument type for specified operation", e.getMessage());
        }
    }

    public void testGroupOrder() throws Exception {
        Query q = new Query("select Company from Company group by 2 order by Company", "org.flymine.model.testmodel");
        assertEquals("SELECT Company FROM org.flymine.model.testmodel.Company AS Company GROUP BY 2 ORDER BY Company", q.toString());
    }

    public void testValidConstraints() throws Exception {
        Query q = new Query("select c_, d_, e_ from Company as c_, Department as d_, CEO as e_ where c_.departments does not contain d_ and c_.CEO contains e_ and (c_.vatNumber < 5 or c_.name like 'fish%') and e_.salary is not null and c_.vatNumber > e_.age and c_.name in (select Company.name as name from Company)", "org.flymine.model.testmodel");
        assertEquals("SELECT c_, d_, e_ FROM org.flymine.model.testmodel.Company AS c_, org.flymine.model.testmodel.Department AS d_, org.flymine.model.testmodel.CEO AS e_ WHERE (c_.CEO CONTAINS e_ AND c_.departments DOES NOT CONTAIN d_ AND (c_.name LIKE fish% OR c_.vatNumber < 5) AND e_.salary IS NOT NULL AND c_.vatNumber > e_.age AND c_.name IN (SELECT Company.name AS name FROM org.flymine.model.testmodel.Company AS Company))", q.toString());
    }

}
