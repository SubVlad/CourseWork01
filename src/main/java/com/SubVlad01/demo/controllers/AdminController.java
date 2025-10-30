package com.SubVlad01.demo.controllers;

import com.SubVlad01.demo.MyUserDetails;
import com.SubVlad01.demo.models.*;
import com.SubVlad01.demo.repo.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.Data;

@Controller
public class AdminController {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingStatusRepository bookingStatusRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SettlementStatusRepository settlementStatusRepository;
    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private SettlingPersonRepository settlingPersonRepository;
    @Autowired
    private SettlingPersonByBookingRepository settlingPersonByBookingRepository;
    @Data
    class Unit{
        Booking booking;
        int bookingId;
        User clientMakingBooking;
        Date bookingMakingDate;
        Date plannedSettlementDate;
        Date plannedDepartureDate;
        Room room;
        BookingStatus bookingStatus;
        Iterable<SettlingPerson> persons;

        Unit(Booking booking){
            this.bookingId = booking.getBookingId();
            this.clientMakingBooking = booking.getClientMakingBooking();
            this.bookingMakingDate = booking.getBookingMakingDate();
            this.plannedSettlementDate = booking.getPlannedSettlementDate();
            this.plannedDepartureDate = booking.getPlannedDepartureDate();
            this.bookingStatus = booking.getBookingStatus();
            this.room = booking.getRoom();
            List<SettlingPersonByBooking> settlingPersonsByBooking =
                    settlingPersonByBookingRepository
                            .findByBooking(booking);
            ArrayList<SettlingPerson> personsList = new ArrayList<>();
            for(int i = 0; i < settlingPersonsByBooking.size(); i++){
                SettlingPerson settlingPerson = settlingPersonsByBooking.get(i).getSettlingPerson();
                personsList.add(settlingPerson);
            }
            this.persons = personsList;
        }
    }

    @GetMapping("/test/window/admin")
    @PreAuthorize("hasAuthority('1')")
    public String toAdminWindow(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        return "window-admin";
    }

    //начиная от сюда идет функционал первой кнопки в окне администратора - "комнаты"
    @GetMapping("/test/rooms/all")
    @PreAuthorize("hasAuthority('1')")
    public String roomsAll(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<Room> ITrooms = roomRepository.findAll();
        model.addAttribute("rooms", ITrooms);
        return "rooms-all";
    }

    @GetMapping("/test/roomTypes/add")
    @PreAuthorize("hasAuthority('1')")
    public String roomTypeAdd(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        return "roomtypes-add";
    }

    @PostMapping("/test/roomtypes/add_confirm")
    @PreAuthorize("hasAuthority('1')")
    public String roomTypeAddConfirm(@RequestParam String name, Model model) {
        RoomType type = new RoomType();
        type.setRoomTypeName(name);
        roomTypeRepository.save(type);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Тип комнаты добавлен");
        model.addAttribute("fio", user.getFIO());
        Iterable<Room> ITrooms = roomRepository.findAll();
        model.addAttribute("rooms", ITrooms);
        return "rooms-all";
    }

    @GetMapping("/test/rooms/add")
    @PreAuthorize("hasAuthority('1')")
    public String roomAdd(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<RoomType> ITroomTypes = roomTypeRepository.findAll();
        model.addAttribute("roomtypes", ITroomTypes);
        return "rooms-add";
    }

    @PostMapping("/test/rooms/add_confirm")
    @PreAuthorize("hasAuthority('1')")
    public String roomAddConfirm(@RequestParam int roomtypeId, Model model) {
        Room room = new Room();
        Optional<RoomType> type = roomTypeRepository.findById(roomtypeId);
        room.setRoomType(type.get());
        type.get().addRoom(room);
        roomRepository.save(room);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Комната №" + room.getRoomId() + " добавлена");
        model.addAttribute("fio", user.getFIO());
        Iterable<Room> ITrooms = roomRepository.findAll();
        model.addAttribute("rooms", ITrooms);
        return "rooms-all";
    }

    //начиная от сюда идет функционал второй кнопки окна администратора - "Бронирования"
    @GetMapping("/test/bookings/all")
    @PreAuthorize("hasAuthority('1')")
    public String bookingsAll(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
        model.addAttribute("bStatuses", ITbookingStatuses);
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        ITbookings.forEach(booking -> unitsList.add(new Unit(booking)));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-all";
    }
    @PostMapping("/test/bookings/{bookingId}/one")
    @PreAuthorize("hasAuthority('1')")
    public String bookingsOne(@PathVariable(value = "bookingId") int bookingId, Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
        model.addAttribute("bStatuses", ITbookingStatuses);
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        ArrayList<Unit> unitsList = new ArrayList<>();
        unitsList.add(new Unit(booking.get()));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-all";
    }
    @PostMapping("/test/bookings/{bookingId}/changeStatus")
    @PreAuthorize("hasAuthority('1')")
    public String bookingChangeStatus(@PathVariable(value = "bookingId") int bookingId,
                                      @RequestParam int bookingStatusId,
                                              Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Booking booking = bookingRepository.findById(bookingId).get();
        BookingStatus bookingStatus = bookingStatusRepository.findById(bookingStatusId).get();
        BookingStatus oldBookingStatus = booking.getBookingStatus();
        oldBookingStatus.removeBooking(booking);
        booking.setBookingStatus(bookingStatus);
        bookingStatus.addBooking(booking);
        bookingRepository.save(booking);
        model.addAttribute("message", "Статус изменен на \"" + bookingStatus.getBookingStatusName() + "\"");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
        model.addAttribute("bStatuses", ITbookingStatuses);
        return "bookings-all";
    }

    @PostMapping("/test/bookings/{bookingId}/accept")
    @PreAuthorize("hasAuthority('1')")
    public String bookingAccept(@PathVariable(value = "bookingId") int bookingId,
                                Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Booking booking = bookingRepository.findById(bookingId).get();
        Optional<BookingStatus> bStatus2 = bookingStatusRepository.findById(2);
        Optional<BookingStatus> bStatus3 = bookingStatusRepository.findById(3);
        if(!bStatus2.isPresent()){
            model.addAttribute("message", "Похоже, что вы не добавили в систему статус бронирования для \"одобрено\"");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
            model.addAttribute("bStatuses", ITbookingStatuses);
            return "bookings-all";
        }
        if(!bStatus3.isPresent()){
            model.addAttribute("message", "Похоже, что вы не добавили в систему статус бронирования для \"выселено\"");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
            model.addAttribute("bStatuses", ITbookingStatuses);
            return "bookings-all";
        }
        if(bStatus2.get() == booking.getBookingStatus() || bStatus3.get() == booking.getBookingStatus()){
            model.addAttribute("message", "Это бронирование уже одобрено");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
            model.addAttribute("bStatuses", ITbookingStatuses);
            return "bookings-all";
        }
        Settlement settlement = new Settlement();
        settlement.setBooking(booking);
        SettlementStatus sStatus = settlementStatusRepository.findById(1).get();
        settlement.setSettlementStatus(sStatus);
        sStatus.addSettlement(settlement);
        settlementRepository.save(settlement);
        model.addAttribute("message", "Заселение оформлено");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
        model.addAttribute("bStatuses", ITbookingStatuses);
        return "bookings-all";
    }
    @GetMapping("/test/bookingStatuses/add")
    @PreAuthorize("hasAuthority('1')")
    public String bookingStatusesAdd(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        return "bookingStatuses-add";
    }
    @PostMapping("/test/bookingStatuses/add_confirm")
    @PreAuthorize("hasAuthority('1')")
    public String bookingStatusesAddConfirm(@RequestParam String name, Model model) {
        BookingStatus bookingStatus = new BookingStatus();
        bookingStatus.setBookingStatusName(name);
        bookingStatusRepository.save(bookingStatus);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Статус бронирования \"" + bookingStatus.getBookingStatusName() + "\" добавлен");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        ITbookings.forEach(new Consumer<Booking>(){
            @Override
            public void accept(Booking booking){
                unitsList.add(new Unit(booking));
            }
        });
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        Iterable<BookingStatus> ITbookingStatuses = bookingStatusRepository.findAll();
        model.addAttribute("bStatuses", ITbookingStatuses);
        return "bookings-all";
    }
    //начиная от сюда идет функционал третьей кнопки окна администратора - "заселения"
    @GetMapping("/test/settlements/all")
    @PreAuthorize("hasAuthority('1')")
    public String settlementsAll(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
        model.addAttribute("sStatuses", ITsettlementStatuses);
        Iterable<Settlement> ITsettlements = settlementRepository.findAll();
        model.addAttribute("settlements", ITsettlements);
        return "settlements-all";
    }
    @PostMapping("/test/settlements/{settlementId}/changeStatus")
    @PreAuthorize("hasAuthority('1')")
    public String settlementChangeStatus(@PathVariable(value = "settlementId") int settlementId,
                                      @RequestParam int settlementStatusId,
                                      Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Settlement settlement = settlementRepository.findById(settlementId).get();
        SettlementStatus settlementStatus = settlementStatusRepository.findById(settlementStatusId).get();
        SettlementStatus oldSettlementStatus = settlement.getSettlementStatus();
        oldSettlementStatus.removeSettlement(settlement);
        settlement.setSettlementStatus(settlementStatus);
        settlementStatus.addSettlement(settlement);
        settlementRepository.save(settlement);
        Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
        model.addAttribute("sStatuses", ITsettlementStatuses);
        model.addAttribute("message", "Статус изменен на \"" + settlementStatus.getSettlementStatusName() + "\"");
        model.addAttribute("fio", user.getFIO());
        Iterable<Settlement> settlements = settlementRepository.findAll();
        model.addAttribute("settlements", settlements);
        return "settlements-all";
    }
    @GetMapping("/test/settlementStatuses/add")
    @PreAuthorize("hasAuthority('1')")
    public String settlementStatusesAdd(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        return "settlementStatuses-add";
    }

    @PostMapping("/test/settlementStatuses/add_confirm")
    @PreAuthorize("hasAuthority('1')")
    public String settlementStatusesAddConfirm(@RequestParam String name, Model model) {
        SettlementStatus sStatus = new SettlementStatus();
        sStatus.setSettlementStatusName(name);
        settlementStatusRepository.save(sStatus);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Статус заселения \"" + sStatus.getSettlementStatusName() + "\" добавлен");
        model.addAttribute("fio", user.getFIO());
        Iterable<Settlement> ITsettlements = settlementRepository.findAll();
        model.addAttribute("settlements", ITsettlements);
        Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
        model.addAttribute("sStatuses", ITsettlementStatuses);
        return "settlements-all";
    }

    @PostMapping("/test/settlements/{settlementId}/departure")
    @PreAuthorize("hasAuthority('1')")
    public String settlementsDeparture(@PathVariable(value = "settlementId") int settlementId, Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Settlement settlement = settlementRepository.findById(settlementId).get();
        Optional<SettlementStatus> sStatus1 = settlementStatusRepository.findById(1);
        Optional<SettlementStatus> sStatus2 = settlementStatusRepository.findById(2);
        Optional<BookingStatus> bStatus2 = bookingStatusRepository.findById(2);
        Optional<BookingStatus> bStatus3 = bookingStatusRepository.findById(3);
        if(!sStatus2.isPresent()){
            model.addAttribute("message", "Похоже, что вы не добавили в систему статус заселения для \"Выселено\"");
            model.addAttribute("fio", user.getFIO());
            Iterable<Settlement> ITsettlements = settlementRepository.findAll();
            model.addAttribute("settlements", ITsettlements);
            Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
            model.addAttribute("sStatuses", ITsettlementStatuses);
            return "settlements-all";
        }
        if(!bStatus3.isPresent()){
            model.addAttribute("message", "Похоже, что вы не добавили в систему статус бронирования для \"Выселено\"");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(booking1 -> unitsList.add(new Unit(booking1)));
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            return "bookings-all";
        }
        if(sStatus2.get() == settlement.getSettlementStatus()){
            model.addAttribute("message", "Клиенты по этому заселению уже выселены");
            model.addAttribute("fio", user.getFIO());
            Iterable<Settlement> ITsettlements = settlementRepository.findAll();
            model.addAttribute("settlements", ITsettlements);
            Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
            model.addAttribute("sStatuses", ITsettlementStatuses);
            return "settlements-all";
        }
        sStatus1.get().removeSettlement(settlement);//"заселено"
        settlement.setSettlementStatus(sStatus2.get());
        sStatus2.get().addSettlement(settlement);//"выселено"
        settlementRepository.save(settlement);
        bStatus2.get().removeBooking(settlement.getBooking());
        settlement.getBooking().setBookingStatus(bStatus3.get());
        bStatus3.get().addBooking(settlement.getBooking());
        Date departureDate = new Date(System.currentTimeMillis());
        settlement.setActualDepartureDate(departureDate);
        bookingRepository.save(settlement.getBooking());
        model.addAttribute("message", "Клиенты выселены");
        model.addAttribute("fio", user.getFIO());
        Iterable<Settlement> ITsettlements = settlementRepository.findAll();
        model.addAttribute("settlements", ITsettlements);
        Iterable<SettlementStatus> ITsettlementStatuses = settlementStatusRepository.findAll();
        model.addAttribute("sStatuses", ITsettlementStatuses);
        return "settlements-all";
    }
    //четвертая кнопка - "администраторы"
    @GetMapping("/test/admins/all")
    @PreAuthorize("hasAuthority('1')")
    public String adminsAll(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<User> ITusers = userRoleRepository.findById(1).get()
                .getUsers();
        model.addAttribute("users", ITusers);
        return "users-all";
    }


    //начиная от сюда идет функционал пятой кнопки окна администратора - "клиенты"
    @GetMapping("/test/clients/all")
    @PreAuthorize("hasAuthority('1')")
    public String clientsAll(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<User> ITusers = userRoleRepository.findById(2).get()
                .getUsers();
        model.addAttribute("users", ITusers);
        return "users-all";
    }
    //функционал изменения пользователей - администратора и клиента
    @PostMapping("/test/users/{userId}/change")
    @PreAuthorize("hasAuthority('1')")
    public String userChange(@PathVariable(value = "userId") int userId, Model model) {
        User user = userRepository.findById(userId).get();
        model.addAttribute("userId",user.getUserId());
        model.addAttribute("surname",user.getSurname());
        model.addAttribute("name",user.getName());
        model.addAttribute("patr",user.getPatronymic());
        model.addAttribute("phone",user.getContactPhoneNumber());
        model.addAttribute("city",user.getCity().getCityName());
        model.addAttribute("email",user.getEmail());
        model.addAttribute("passport",user.getPassportSeriesNumber());
        model.addAttribute("login",user.getLogin());
        model.addAttribute("userRole",user.getUserRole().getUserRoleName());

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<City> ITcities = cityRepository.findAll();
        model.addAttribute("cities", ITcities);
        Iterable<UserRole> ITuserRoles = userRoleRepository.findAll();
        model.addAttribute("userRoles", ITuserRoles);
        return "users-change";
    }
    @PostMapping("/test/users/{userId}/change_confirm")
    @PreAuthorize("hasAuthority('1')")
    public String userChangeConfirm(@PathVariable(value = "userId") int userId,
                                    @RequestParam(required = false) String surname,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String patr,
                                    @RequestParam(required = false) String phone,
                                    @RequestParam(required = false) Integer cityId,
                                    @RequestParam(required = false) String cityAdd,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String passport,
                                    @RequestParam(required = false) String login,
                                    @RequestParam(required = false) String password,
                                    @RequestParam(required = false) Integer userRoleId,
                                    Model model){
        User user = userRepository.findById(userId).get();
        if(!surname.isEmpty())
            user.setSurname(surname);
        if(!name.isEmpty())
            user.setName(name);
        if(!patr.isEmpty())
            user.setPatronymic(patr);

        if(!phone.isEmpty()) {
            try {
                int ph = Integer.parseInt(phone);
                user.setContactPhoneNumber(ph);
            } catch (NumberFormatException e) {
                model.addAttribute("message1", "В поле ввода телефона должен быть введен");
                model.addAttribute("message2", "телефон в виде числа без символов и букв");
                Iterable<City> cities = cityRepository.findAll();
                model.addAttribute("cities", cities);
                return "window-admin";
            }
        }
        City city;
        if(!cityAdd.isEmpty()){
            city = new City();
            city.setCityName(cityAdd);
            cityRepository.save(city);
            user.setCity(city);
            city.addUser(user);
        }else{
            if(cityId > -1){
                city = cityRepository.findById(cityId).get();
                user.setCity(city);
                city.addUser(user);
            }
        }
        if(!email.isEmpty())
            user.setEmail(email);
        if(!passport.isEmpty()){
            try{
                int pas = Integer.parseInt(passport);
                user.setPassportSeriesNumber(pas);
            }catch (NumberFormatException e){
                model.addAttribute("message1","В поле ввода паспортных данных должны быть введены");
                model.addAttribute("message2","серия и номер в виде одного числа без символов и букв");
                Iterable<City> cities = cityRepository.findAll();
                model.addAttribute("cities", cities);
                return "index";
            }
        }
        if(!login.isEmpty())
            user.setLogin(login);
        //добавление криптографичекого ключа, который шифрует поступающие
        //в базу пароли
        if(!password.isEmpty()) {
            BCryptPasswordEncoder enc = new BCryptPasswordEncoder(5);
            user.setPassword(enc.encode(password));
        }
        if(userRoleId > -1){
            UserRole userRole = userRoleRepository.findById(userRoleId).get();
            user.setUserRole(userRole);
            userRole.addUser(user);
        }
        userRepository.save(user);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message1", "Данные пользователя " + surname);
        model.addAttribute("fio", "благополучно изменены");
        return "window-admin";
    }
}
