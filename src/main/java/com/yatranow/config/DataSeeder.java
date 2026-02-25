package com.yatranow.config;

import com.yatranow.entity.*;
import com.yatranow.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final AdminRepository adminRepository;
    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;
    private final SeatRepository seatRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedData() {
        long adminCount = adminRepository.count();
        long routeCount = routeRepository.count();
        long scheduleCount = scheduleRepository.count();

        System.out.println("Seeding diagnostic - Admins: " + adminCount + ", Routes: " + routeCount + ", Schedules: "
                + scheduleCount);

        // Seed Admin if missing
        if (adminCount == 0) {
            System.out.println("No admin found. Creating default admin...");
            Admin admin = new Admin();
            admin.setName("System Admin");
            admin.setEmail("admin@yatranow.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            adminRepository.save(admin);
        }

        // Check if we have any future schedules
        long futureSchedules = scheduleRepository.findByScheduleDateGreaterThanEqual(LocalDate.now()).size();
        System.out.println("Future schedules count: " + futureSchedules);

        if (futureSchedules == 0) {
            System.out.println("No future schedules found. Seeding new schedules...");

            List<Route> routes = routeRepository.count() == 0 ? createSampleRoutes() : routeRepository.findAll();
            List<Owner> owners = ownerRepository.count() == 0 ? createSampleOwners() : ownerRepository.findAll();
            List<Vehicle> vehicles = vehicleRepository.count() == 0 ? createSampleVehicles(owners)
                    : vehicleRepository.findAll();

            createSampleSchedules(vehicles, routes);
            System.out.println("Future schedules seeded successfully.");
        }

        System.out.println("Database seeding check completed.");
    }

    private List<Owner> createSampleOwners() {
        List<Owner> owners = new ArrayList<>();

        String[][] ownerData = {
                { "Rajesh Kumar", "Royal Travels", "rajesh@royal.com", "rajesh123" },
                { "Priya Sharma", "Express Transport", "priya@express.com", "priya123" },
                { "Amit Patel", "Swift Buses", "amit@swift.com", "amit123" },
                { "Neha Gupta", "Comfort Rides", "neha@comfort.com", "neha123" },
                { "Vikram Singh", "Premium Railways", "vikram@premium.com", "vikram123" }
        };

        for (String[] data : ownerData) {
            Owner owner = new Owner();
            owner.setOwnerName(data[0]);
            owner.setAgencyName(data[1]);
            owner.setEmail(data[2]);
            owner.setPassword(passwordEncoder.encode(data[3]));
            owner.setMobile("9876543210");
            owner.setRole("OWNER");
            owner.setIsBlocked(false);
            // Note: In real scenario, would need actual image bytes
            owner.setAgencyImage(new byte[0]); // Empty for seed data
            owners.add(ownerRepository.save(owner));
        }

        return owners;
    }

    private List<Vehicle> createSampleVehicles(List<Owner> owners) {
        List<Vehicle> vehicles = new ArrayList<>();

        // Buses with different types
        Vehicle bus1 = createVehicle(owners.get(0), Vehicle.VehicleType.BUS, "MH-12-AB-1234", "Royal Express",
                Vehicle.BusType.SUPER_LUXURY);
        vehicles.add(bus1);

        Vehicle bus2 = createVehicle(owners.get(1), Vehicle.VehicleType.BUS, "DL-1C-5678", "Swift Deluxe",
                Vehicle.BusType.DELUXE);
        vehicles.add(bus2);

        Vehicle bus3 = createVehicle(owners.get(2), Vehicle.VehicleType.BUS, "GJ-05-XY-9012", "Comfort Sleeper",
                Vehicle.BusType.SLEEPER);
        vehicles.add(bus3);

        Vehicle bus4 = createVehicle(owners.get(3), Vehicle.VehicleType.BUS, "KA-03-CD-3456", "City Seater",
                Vehicle.BusType.SEATER);
        vehicles.add(bus4);

        // Trains
        Vehicle train1 = createTrainVehicle(owners.get(4), "TRAIN-001", "Rajdhani Express", 100);
        vehicles.add(train1);

        Vehicle train2 = createTrainVehicle(owners.get(4), "TRAIN-002", "Shatabdi Express", 80);
        vehicles.add(train2);

        return vehicles;
    }

    private Vehicle createVehicle(Owner owner, Vehicle.VehicleType type, String number, String name,
            Vehicle.BusType busType) {
        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(owner.getId());
        vehicle.setVehicleType(type);
        vehicle.setVehicleNumber(number);
        vehicle.setName(name);
        vehicle.setBusType(busType);
        vehicle.setTotalSeats(busType.getSeatCount());

        vehicle = vehicleRepository.save(vehicle);

        // Auto-generate seats
        generateSeatsForBus(vehicle, busType);

        return vehicle;
    }

    private Vehicle createTrainVehicle(Owner owner, String number, String name, int totalSeats) {
        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(owner.getId());
        vehicle.setVehicleType(Vehicle.VehicleType.TRAIN);
        vehicle.setVehicleNumber(number);
        vehicle.setName(name);
        vehicle.setTotalSeats(totalSeats);

        vehicle = vehicleRepository.save(vehicle);

        // Generate seats for train
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setVehicleId(vehicle.getId());
            seat.setSeatNumber("S" + i);
            seat.setSeatType(Seat.SeatType.SEATER);
            seat.setIsAvailable(true);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);

        return vehicle;
    }

    private void generateSeatsForBus(Vehicle vehicle, Vehicle.BusType busType) {
        List<Seat> seats = new ArrayList<>();

        switch (busType) {
            case SUPER_LUXURY:
                for (int row = 1; row <= 10; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D' }) {
                        seats.add(createSeat(vehicle.getId(), row + String.valueOf(col), Seat.SeatType.SEATER));
                    }
                }
                break;
            case DELUXE:
                for (int row = 1; row <= 11; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D' }) {
                        seats.add(createSeat(vehicle.getId(), row + String.valueOf(col), Seat.SeatType.SEATER));
                        if (seats.size() >= 45)
                            break;
                    }
                    if (seats.size() >= 45)
                        break;
                }
                break;
            case SLEEPER:
                for (int i = 1; i <= 18; i++) {
                    seats.add(createSeat(vehicle.getId(), "L" + i, Seat.SeatType.SLEEPER));
                    seats.add(createSeat(vehicle.getId(), "U" + i, Seat.SeatType.SLEEPER));
                }
                break;
            case SEATER:
                for (int row = 1; row <= 11; row++) {
                    for (char col : new char[] { 'A', 'B', 'C', 'D', 'E' }) {
                        seats.add(createSeat(vehicle.getId(), row + String.valueOf(col), Seat.SeatType.SEATER));
                        if (seats.size() >= 52)
                            break;
                    }
                    if (seats.size() >= 52)
                        break;
                }
                break;
        }

        seatRepository.saveAll(seats);
    }

    private Seat createSeat(Long vehicleId, String seatNumber, Seat.SeatType seatType) {
        Seat seat = new Seat();
        seat.setVehicleId(vehicleId);
        seat.setSeatNumber(seatNumber);
        seat.setSeatType(seatType);
        seat.setIsAvailable(true);
        return seat;
    }

    private List<Route> createSampleRoutes() {
        List<Route> routes = new ArrayList<>();

        String[][] routeData = {
                { "Mumbai", "Pune", "150" },
                { "Delhi", "Jaipur", "280" },
                { "Bangalore", "Chennai", "350" },
                { "Hyderabad", "Vijayawada", "275" },
                { "Ahmedabad", "Surat", "265" },
                { "Kolkata", "Bhubaneswar", "450" },
                { "Mumbai", "Goa", "450" },
                { "Delhi", "Agra", "230" },
                { "Chennai", "Coimbatore", "500" },
                { "Pune", "Nashik", "210" }
        };

        for (String[] data : routeData) {
            Route route = new Route();
            route.setFromLocation(data[0]);
            route.setToLocation(data[1]);
            route.setDistanceKm(Double.parseDouble(data[2]));
            routes.add(routeRepository.save(route));
        }

        return routes;
    }

    private void createSampleSchedules(List<Vehicle> vehicles, List<Route> routes) {
        LocalDate today = LocalDate.now();

        // Create schedules for next 7 days
        for (int day = 0; day < 7; day++) {
            LocalDate scheduleDate = today.plusDays(day);

            // Create multiple schedules per day
            for (int i = 0; i < Math.min(vehicles.size(), routes.size()); i++) {
                Vehicle vehicle = vehicles.get(i % vehicles.size());
                Route route = routes.get(i % routes.size());

                // Morning schedule
                createSchedule(vehicle, route, scheduleDate,
                        LocalTime.of(6, 0), LocalTime.of(10, 30),
                        calculatePrice(route.getDistanceKm(), 1.0));

                // Afternoon schedule
                createSchedule(vehicle, route, scheduleDate,
                        LocalTime.of(14, 0), LocalTime.of(18, 30),
                        calculatePrice(route.getDistanceKm(), 1.2));

                // Evening schedule
                createSchedule(vehicle, route, scheduleDate,
                        LocalTime.of(20, 0), LocalTime.of(0, 30),
                        calculatePrice(route.getDistanceKm(), 1.5));
            }
        }
    }

    private void createSchedule(Vehicle vehicle, Route route, LocalDate date,
            LocalTime departure, LocalTime arrival, Double price) {
        Schedule schedule = new Schedule();
        schedule.setVehicleId(vehicle.getId());
        schedule.setRouteId(route.getId());
        schedule.setScheduleDate(date);
        schedule.setDepartureTime(departure);
        schedule.setArrivalTime(arrival);
        schedule.setPrice(price);
        schedule.setAvailableSeats(vehicle.getTotalSeats());
        scheduleRepository.save(schedule);
    }

    private Double calculatePrice(Double distanceKm, Double multiplier) {
        return Math.round(distanceKm * 2.5 * multiplier * 100.0) / 100.0;
    }
}
