-- DROP STATEMENTS
DROP TABLE IF EXISTS Airline CASCADE;
DROP TABLE IF EXISTS Passenger CASCADE;
DROP TABLE IF EXISTS Flight CASCADE;
DROP TABLE IF EXISTS Ratings CASCADE;
DROP TABLE IF EXISTS Booking CASCADE;

DROP DOMAIN IF EXISTS _YEAR CASCADE;
DROP DOMAIN IF EXISTS _HOURS CASCADE;
DROP DOMAIN IF EXISTS _SEATS CASCADE;
DROP DOMAIN IF EXISTS _SCORE CASCADE;

-- CREATE DOMAINS
CREATE DOMAIN _YEAR AS int4 CHECK(VALUE >= 1900);--YEAR ONLY GREATER THAN 1900
CREATE DOMAIN _HOURS AS int4 CHECK(VALUE > 0 AND VALUE < 24);--At most 24 hours duration
CREATE DOMAIN _SEATS AS int4 CHECK(VALUE > 0 AND VALUE < 500);--Plane Seats
CREATE DOMAIN _SCORE AS int4 CHECK(VALUE >= 0 AND VALUE <= 5);--Zero to five stars rating

-- CREATE TABLES
CREATE TABLE Airline(
	airId INTEGER NOT NULL,
	name CHAR(24) NOT NULL,
	founded _YEAR NOT NULL,
	country CHAR(40) NOT NULL,
	hub CHAR(24) NOT NULL,
	PRIMARY KEY(airId)
);

CREATE TABLE Passenger(
	pID INTEGER NOT NULL,
	passNum CHAR(10) NOT NULL,
	fullName CHAR(24) NOT NULL,
	bdate DATE NOT NULL,
	country CHAR(24) NOT NULL,
	PRIMARY KEY(pID),
	UNIQUE(passNum)
);

CREATE TABLE Flight(
	airId INTEGER NOT NULL,
	flightNum CHAR(8) NOT NULL,
	origin CHAR(16) NOT NULL,
	destination CHAR(16) NOT NULL,
	plane CHAR(16) NOT NULL,
	seats _SEATS NOT NULL,
	duration _HOURS NOT NULL,
	PRIMARY KEY(flightNum),
	FOREIGN KEY (airId) REFERENCES Airline(airId)
);

CREATE TABLE Ratings(
	rID INTEGER NOT NULL,
	pID INTEGER NOT NULL,
	flightNum CHAR(8) NOT NULL,
	score _SCORE NOT NULL,
	comment TEXT,
	PRIMARY KEY (rID),
	FOREIGN KEY (pID) REFERENCES Passenger(pID),
	FOREIGN KEY (flightNum) REFERENCES Flight(flightNum)
);

CREATE TABLE Booking(
	bookRef CHAR(10) NOT NULL,
	departure DATE NOT NULL,
	flightNum CHAR(8) NOT NULL,
	pID INTEGER NOT NULL,
	PRIMARY KEY(bookRef),
	FOREIGN KEY (flightNum) REFERENCES Flight(flightNum),
	FOREIGN KEY (pID) REFERENCES Passenger(pID),
	UNIQUE(departure,flightNum,pID)
);

CREATE INDEX booking_flight_no_index
ON Booking USING BTREE (flightNum, departure); --This will increase search speed for flight on a specific day

CREATE INDEX passenger_passport_no_index
ON Passenger USING BTREE (passNum); --This will increase search speed for finding passengers with a specific passport num

CREATE INDEX flight_dest_orig_index
ON Flight USING BTREE (origin, destination); --This will let us search origin and destination pairs much faster

CREATE INDEX ratings_pID_flightNum
ON Ratings USING BTREE (pID, flightNum); --Quickly check to see if a customer has already rated a flight



--CREATE USER WITH PASSWORD TO CONNECT TO DATABASE--CHANGE username accordingly
DROP USER IF EXISTS username;
CREATE USER username WITH PASSWORD '';
--GRANT USER PRIVELEGES TO ACCESS THE TABLES
GRANT ALL PRIVILEGES ON TABLE Airline TO username;
GRANT ALL PRIVILEGES ON TABLE Passenger TO username;
GRANT ALL PRIVILEGES ON TABLE Flight TO username;
GRANT ALL PRIVILEGES ON TABLE Ratings TO username;
GRANT ALL PRIVILEGES ON TABLE Booking TO username;
------------------------------------------------------------------------------------

COPY Airline (
	airId,
	name,
	founded,
	country,
	hub)
FROM 'airline.csv'
WITH DELIMITER ',';
--SELECT * FROM Airline;

COPY Passenger (
	pID,
	passNum,
	fullName,
	bdate,
	country)
FROM 'passenger.csv'
WITH DELIMITER ',';
--SELECT * FROM Passenger;

COPY Flight (
	airId,
	flightNum,
	origin,
	destination,
	plane,
	seats,
	duration)
FROM 'flights.csv'
WITH DELIMITER ',';
--SELECT * FROM Flight;

COPY Ratings (
	rID,
	pID,
	flightNum,
	score,
	comment)
FROM 'ratings.csv'
WITH DELIMITER ',';
--SELECT * FROM Ratings;

COPY Booking (
	bookRef,
	departure,
	flightNum,
	pID)
FROM 'bookings.csv'
WITH DELIMITER ',';
--SELECT * FROM Booking;


