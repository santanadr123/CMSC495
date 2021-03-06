--Drop tables:
DROP TABLE CarReservations;
DROP TABLE Cars;
DROP TABLE HotelReservations;
DROP TABLE Hotels;
DROP TABLE AirlineReservations;
DROP TABLE Airlines;
--Create tables:
CREATE TABLE Cars(
CarID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
PassengerCapacity int,
CarType varchar(15),
Make varchar(25),
Model varchar(25),
CarYear int,
Price int,
PRIMARY KEY (CarID)
);

CREATE TABLE CarReservations(
CarReservationID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
CarID int NOT NULL,
ClientName varchar(50),
CheckOutDate Date,
CheckInDate Date,
PRIMARY KEY (CarReservationID),
FOREIGN KEY (CarID) REFERENCES Cars(CarID)
);
CREATE TABLE Hotels(
HotelRoomID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
RoomCapacity int NOT NULL,
BedNumber int NOT NULL,
BedType varchar(35),
Features varchar(50),
Price int,
PRIMARY KEY (HotelRoomID)
);
CREATE TABLE HotelReservations(
HotelReservationID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
HotelRoomID int NOT NULL,
ClientName varchar(50),
CheckOutDate Date,
CheckInDate Date,
PRIMARY KEY (HotelReservationID),
FOREIGN KEY (HotelRoomID) REFERENCES Hotels(HotelRoomID)
);
CREATE TABLE Airlines(
FlightID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
Airline varchar(25),
FlightClass varchar(10),
DepartureDate Date,
ArrivalDate Date,
Price int,
QuantityAvailable int,
QuantityReserved int,
PRIMARY KEY (FlightID)
);
CREATE TABLE AirlineReservations(
FlightReservationID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
FlightID int NOT NULL,
ClientName varchar(50),
DepartureDate Date,
ArrivalDate Date,
QuantityAvailable int,
QuantityReserved int,
PRIMARY KEY (FlightReservationID),
FOREIGN KEY (FlightID) REFERENCES Airlines(FlightID)
);