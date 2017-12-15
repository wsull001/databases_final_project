/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	
	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	public int getNextPID() throws SQLException {
		return Integer.parseInt(executeQueryAndReturnResult("SELECT MAX(pID) FROM Passenger;").get(0).get(0)) + 1;
	}

	public int getNextRID() throws SQLException {
		return Integer.parseInt(executeQueryAndReturnResult("SELECT MAX(rID) FROM Ratings;").get(0).get(0)) + 1;
	}


	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		AirBooking esql = null;
		
		try{
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new AirBooking (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddPassenger(AirBooking esql){//1
		try {
			int pID = esql.getNextPID();
			System.out.print("Enter the passport number: ");
			String passNum = in.readLine();
			System.out.print("Enter your customer's full name: ");
			String fullName = in.readLine();
			System.out.print("Enter their birth date in the format YYYY-MM-DD: ");
			String bdate = in.readLine();
			System.out.print("Enter the country they are from: ");
			String country = in.readLine();
			esql.executeUpdate("INSERT INTO Passenger (pID, passNum, fullName, bdate, country) VALUES (" + pID + ", '" + passNum + "', '" 
				+ fullName +"', DATE '" + bdate + "', '" + country + "');");
			System.out.println("Congratulations, " + fullName + ", you have successfully registered. To book flights remember your customer will need their passNum: " + passNum);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		//Add a new passenger to the database
	}
	
	public static String genRandRef(AirBooking esql) throws SQLException { //generate a new reference number
		do {
			String ref = "";
			for (int i = 0; i < 10; i++) 
				ref += (char)('A' + (int)(Math.random() * 26));
			if (esql.executeQuery("SELECT * FROM Booking WHERE bookRef = '" + ref +"';") == 0) return ref;
		} while (true);
	}
	
	public static void BookFlight(AirBooking esql){//2
		try {
			System.out.print("Enter the passport number: ");
			String passNum = in.readLine();
			List<List<String>> results = esql.executeQueryAndReturnResult("SELECT pID, fullName FROM Passenger WHERE passNum = '" + passNum + "';");
			if (results.size() == 0) {
				System.out.println("ERROR: We're sorry, we don't seem to have your passport number in our database.");
				return;
			}
			int pID = Integer.parseInt(results.get(0).get(0));
			String ref = genRandRef(esql);
			System.out.print("Please enter the flight number you wish to book " + results.get(0).get(1).trim() + " on: ");
			String flightNo = in.readLine();
			if (0 == esql.executeQuery("SELECT * FROM Flight WHERE flightNum = '" + flightNo + "';")) {
				System.out.println("ERROR: We're sorry we couldn't find the flight you entered");
				return;
			}
			System.out.print("Please enter the date they would like to fly on (YYYY-MM-DD): ");
			String date = in.readLine();
			esql.executeUpdate("INSERT INTO Booking (bookRef, departure, flightNum, pID) VALUES ('" + ref + "', DATE '" + date + "', '"
				+ flightNo + "', " + pID + ");");
			System.out.println("Congrats, you have created a booking. Remember the reference number: " + ref);
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		//Book Flight for an existing customer
	}
	
	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table
		try {
			System.out.print("Enter the passenger's passport number: ");
			String passNum = in.readLine();
			List<List<String>> results = esql.executeQueryAndReturnResult("SELECT pID FROM Passenger WHERE passNum = '" + passNum + "';");
			if (results.size() == 0) {
				System.out.println("ERROR: Passport number not recognized");
				return;
			}
			int pID = Integer.parseInt(results.get(0).get(0));
			System.out.print("Enter the flight number the passenger wants to review: ");
			String flightNum = in.readLine();
			if (esql.executeQuery("SELECT * FROM Flight WHERE flightNum = '" + flightNum + "';") == 0) {
				System.out.println("ERROR: Flight number not found");
				return;
			}
			// make sure that passenger hasn't already reviewed the flight
			if (esql.executeQuery("SELECT * FROM Ratings WHERE pID = " + pID + " AND flightNum = '" + flightNum + "';") != 0) {
				System.out.println("ERROR: This passenger has already reviewed this flight");
				return;
			}
			System.out.print("Please enter a rating for flight " + flightNum + " (0-5): ");
			int rating = Integer.parseInt(in.readLine());
			if (rating < 0 || rating > 5) {
				System.out.println("ERROR: invalid rating");
				return;
			}
			System.out.print("Would they like to leave a comment? (y/n): ");
			boolean hasComment = false;
			String comment = "";
			if (in.readLine().equals("y")) {
				hasComment = true;
				System.out.print("Enter the comment text: ");
				comment = in.readLine();
			}
			int rID = esql.getNextRID();
			String qurrry = "INSERT INTO Ratings (rID, pID, flightNum, score";
			if (hasComment) {
				qurrry += ", comment";
			}
			qurrry += ") VALUES (" + rID + ", " + pID + ", '" + flightNum + "', " + rating;
			if (hasComment) {
				qurrry += ", '" + comment + "'";
			}
			qurrry += ");";
			esql.executeUpdate(qurrry);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		
	}
	
	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
		
	}
	
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
		try {
			System.out.print("Please enter the destination: ");
			String destination = in.readLine();
			System.out.print("Please enter the origin: ");
			String origin = in.readLine();
			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT flightNum, origin, destination, plane, duration FROM Flight WHERE origin = '" + origin + "' AND destination = '" + destination + "';");
			for (int i = 0; i < result.size(); i++) {
				System.out.print("" + (i+1) + ". ");
				System.out.print("origin: " + result.get(i).get(1).trim() + " destination: " + result.get(i).get(2).trim() + " flight number: " + result.get(i).get(0).trim() + " plane: " + result.get(i).get(3).trim() + " duration: " + result.get(i).get(4).trim() + "\n");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
	
	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
		try {	
			System.out.print("Enter the number of most popular destinations you want to see: ");
			int num = Integer.parseInt(in.readLine());
			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT destination from Flight GROUP BY destination ORDER BY count(*) DESC LIMIT " + num + ";"); 
			for (int i = 0; i < result.size(); i++) {
				System.out.print("" + (i+1) + ". ");
				System.out.println("desination: " + result.get(i).get(0));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
		try {
			System.out.print("Enter the number of highest rated routes you'd like to see: ");
			int num = Integer.parseInt(in.readLine());
			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT flightNum, avg(score) FROM Ratings GROUP BY flightNum ORDER BY avg(score) DESC LIMIT " + num + ";");
			for (int i = 0; i < result.size(); i++) {
				System.out.print("" + (i+1) + ". ");
				System.out.println("flight number: " + result.get(i).get(0) + " score: " + result.get(i).get(1));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
		try {
			System.out.print("Please enter the destination: ");
			String destination = in.readLine();
			System.out.print("Please enter the origin: ");
			String origin = in.readLine();
			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT flightNum, origin, destination, plane, duration FROM Flight WHERE origin = '" + origin + "' AND destination = '" + destination + "' ORDER BY duration;");
			for (int i = 0; i < result.size(); i++) {
				System.out.print("" + (i+1) + ". ");
				System.out.print("origin: " + result.get(i).get(1).trim() + " destination: " + result.get(i).get(2).trim() + " flight number: " + result.get(i).get(0).trim() + " plane: " + result.get(i).get(3).trim() + " duration: " + result.get(i).get(4).trim() + "\n");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//
		try {
			System.out.print("Please enter the flight number: ");
			String flight = in.readLine();
			List<List<String>> result1 = esql.executeQueryAndReturnResult("SELECT seats FROM Flight WHERE flightNum = '" + flight + "';");
			if (result1.size() == 0) {
				System.out.println("ERROR: flight not found");
				return;
			}
			int seats = Integer.parseInt(result1.get(0).get(0));
			System.out.print("Please enter the date of the flight (YYYY-MM-DD): ");
			String day = in.readLine();
			int books = esql.executeQuery("SELECT * FROM Booking WHERE departure = '" + day + "' AND flightNum = '" + flight + "';");
			System.out.println("There are " + (seats - books) + " available seats");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
}
