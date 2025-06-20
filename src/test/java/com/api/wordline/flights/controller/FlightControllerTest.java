package com.api.wordline.flights.controller;

import com.api.wordline.flights.controller.dto.ErrorResponse;
import com.api.wordline.flights.controller.dto.FlightRequest;
import com.api.wordline.flights.controller.dto.FlightResponse;
import com.api.wordline.flights.model.Flight;
import com.api.wordline.flights.repository.FlightRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FlightControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FlightRepository flightRepository;

    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost:" + port + "/api";

        flightRepository.deleteAll();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("feign.crazy-suppliers-url", () -> "http://localhost:" + wireMockServer.port());
    }


    @Test
    public void testGetFlightsOnlyFromSupplier() {
        wireMockServer.stubFor(WireMock.post("/flights")
                .willReturn(ok()
                        .withHeader("content-type", "application/json")
                        .withBody("[{\n" +
                                "  \"carrier\": \"Lufthansa\",\n" +
                                "  \"basePrice\": 200.0,\n" +
                                "  \"tax\": 50.0,\n" +
                                "  \"departureAirportName\": \"FRA\",\n" +
                                "  \"arrivalAirportName\": \"JFK\",\n" +
                                "  \"outboundDateTime\": \"2025-07-10T14:30:00\",\n" +
                                "  \"inboundDateTime\": \"2025-07-20T09:45:00\"\n" +
                                "}]")));

        List<FlightResponse> response = given()
                .contentType("application/json")
                .when()
                .get("/flights")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        Assertions.assertEquals(1, response.size());

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(0, allAfter.size());

    }

    @Test
    public void testGetFlightsFromDBAndSupplier() {
        Flight flight = createFlight();
        Flight savedFlight = flightRepository.save(flight);

        wireMockServer.stubFor(WireMock.post("/flights")
                .willReturn(ok()
                        .withHeader("content-type", "application/json")
                        .withBody("[{\n" +
                                "  \"carrier\": \"Lufthansa\",\n" +
                                "  \"basePrice\": 200.0,\n" +
                                "  \"tax\": 50.0,\n" +
                                "  \"departureAirportName\": \"FRA\",\n" +
                                "  \"arrivalAirportName\": \"JFK\",\n" +
                                "  \"outboundDateTime\": \"2025-07-10T14:30:00\",\n" +
                                "  \"inboundDateTime\": \"2025-07-20T09:45:00\"\n" +
                                "}]")));

        List<FlightResponse> response = given()
                .contentType("application/json")
                .when()
                .param("airline", savedFlight.getAirline())
                .get("/flights")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        Assertions.assertEquals(2, response.size());

        List<Flight> flightsDB = flightRepository.findAll();
        Assertions.assertEquals(1, flightsDB.size());

    }

    @Test
    public void testGetFlightsWrongFilter() {
        Flight flight = createFlight();
        Flight savedFlight = flightRepository.save(flight);

        wireMockServer.stubFor(WireMock.post("/flights")
                .willReturn(ok()
                        .withHeader("content-type", "application/json")
                        .withBody("[]")));

        List<FlightRequest> response = given()
                .contentType("application/json")
                .when()
                .param("airline", "WRONG")
                .get("/flights")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        Assertions.assertEquals(0, response.size());

        List<Flight> flightsDB = flightRepository.findAll();
        Assertions.assertEquals(1, flightsDB.size());

    }

    @Test
    public void testGetFlightsCrazySupplierError() {
        wireMockServer.stubFor(WireMock.post("/flights")
                .willReturn(serverError()));

        List<FlightRequest> response = given()
                .contentType("application/json")
                .when()
                .param("airline", "WRONG")
                .get("/flights")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        Assertions.assertEquals(0, response.size());

    }

    @Test
    public void testSaveFlight() {
        FlightRequest flightRequest = new FlightRequest(
                "Delta",
                "Worldline",
                200.1,
                "AMS",
                "JFK",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<Flight> allBefore = flightRepository.findAll();
        Assertions.assertEquals(0, allBefore.size());

        given()
                .contentType("application/json")
                .body(flightRequest)
                .when()
                .post("/flights")
                .then()
                .statusCode(201);

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(1, allAfter.size());
    }

    @Test
    public void testSaveFlightAirportMoreThen3Chars() {
        FlightRequest flightRequest = new FlightRequest(
                "Delta",
                "Worldline",
                200.1,
                "AMSS",
                "JFKSS",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<Flight> allBefore = flightRepository.findAll();
        Assertions.assertEquals(0, allBefore.size());

        ErrorResponse errorResponse = given()
                .contentType("application/json")
                .body(flightRequest)
                .when()
                .post("/flights")
                .then()
                .statusCode(400).extract()
                .as(new TypeRef<>() {
                });

        Assertions.assertEquals(2, errorResponse.messages().size());

        List<String> messages = List.of("Destination airport must be exactly 3 characters", "Departure airport code must be exactly 3 characters");
        for (String message : errorResponse.messages()) {
            Assertions.assertTrue(messages.contains(message));
        }

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(0, allAfter.size());
    }

    @Test
    public void testUpdateFlight() {
        Flight flight = createFlight();
        Flight savedFlight = flightRepository.save(flight);

        List<Flight> allBefore = flightRepository.findAll();
        Assertions.assertEquals(1, allBefore.size());

        FlightRequest flightRequest = new FlightRequest(
                "Delta",
                "Worldline",
                200.1,
                "AMS",
                "JFK",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given()
                .contentType("application/json")
                .body(flightRequest)
                .param("id", savedFlight.getId())
                .when()
                .put("/flights")
                .then()
                .statusCode(200);

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(1, allAfter.size());

        Flight flightUpdated = allAfter.getFirst();
        Assertions.assertEquals(flightRequest.airline(), flightUpdated.getAirline());
        Assertions.assertEquals(flightRequest.supplier(), flightUpdated.getSupplier());
        Assertions.assertEquals(flightRequest.fare(), flightUpdated.getFare());
        Assertions.assertEquals(flightRequest.departureAirport(), flightUpdated.getDepartureAirport());
        Assertions.assertEquals(flightRequest.destinationAirport(), flightUpdated.getDestinationAirport());
        Assertions.assertEquals(flightRequest.departureTime(), flightUpdated.getDepartureTime());
        Assertions.assertEquals(flightRequest.arrivalTime(), flightUpdated.getArrivalTime());
    }

    @Test
    public void testUpdateNonExistingFlight() {
        Flight flight = createFlight();
        Flight savedFlight = flightRepository.save(flight);

        List<Flight> allBefore = flightRepository.findAll();
        Assertions.assertEquals(1, allBefore.size());

        FlightRequest flightRequest = new FlightRequest(
                "Delta",
                "Worldline",
                200.1,
                "AMS",
                "JFK",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given()
                .contentType("application/json")
                .body(flightRequest)
                .param("id", "999")
                .when()
                .put("/flights")
                .then()
                .statusCode(400)
                .body("messages[0]", equalTo("Flight doenst exist"));
        ;

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(1, allAfter.size());

        Flight flightUpdated = allAfter.getFirst();
        Assertions.assertEquals(flight.getAirline(), flightUpdated.getAirline());
        Assertions.assertEquals(flight.getSupplier(), flightUpdated.getSupplier());
        Assertions.assertEquals(flight.getFare(), flightUpdated.getFare());
        Assertions.assertEquals(flight.getDepartureAirport(), flightUpdated.getDepartureAirport());
        Assertions.assertEquals(flight.getDestinationAirport(), flightUpdated.getDestinationAirport());
        Assertions.assertEquals(flight.getDepartureTime(), flightUpdated.getDepartureTime());
        Assertions.assertEquals(flight.getArrivalTime(), flightUpdated.getArrivalTime());
    }

    @Test
    public void testDeleteFlight() {
        Flight flight = createFlight();
        Flight savedFlight = flightRepository.save(flight);

        List<Flight> allBefore = flightRepository.findAll();
        Assertions.assertEquals(1, allBefore.size());

        given()
                .contentType("application/json")
                .when()
                .param("id", savedFlight.getId())
                .delete("/flights")
                .then()
                .statusCode(204);

        List<Flight> allAfter = flightRepository.findAll();
        Assertions.assertEquals(0, allAfter.size());
    }

    public Flight createFlight() {
        Flight flightInfo = new Flight();
        flightInfo.setAirline("Lufthansa");
        flightInfo.setSupplier("WorldLine");
        flightInfo.setFare(250.50);
        flightInfo.setDepartureAirport("FRA");
        flightInfo.setDestinationAirport("JFK");
        flightInfo.setDepartureTime(LocalDateTime.of(2025, 7, 1, 10, 0));
        flightInfo.setArrivalTime(LocalDateTime.of(2025, 7, 1, 18, 0));
        return flightInfo;
    }

}
