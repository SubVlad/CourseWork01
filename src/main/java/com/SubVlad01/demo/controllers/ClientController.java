package com.SubVlad01.demo.controllers;

import com.SubVlad01.demo.MyUserDetails;
import com.SubVlad01.demo.models.*;
import com.SubVlad01.demo.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class ClientController {
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
    private SettlingPersonRepository settlingPersonRepository;
    @Autowired
    private SettlingPersonStatusRepository settlingPersonStatusRepository;
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

    @GetMapping("/test/window/client")
    @PreAuthorize("hasAuthority('2')")
    public String toClientWindow(Model model){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        model.addAttribute("message","Текущий пользователь:");
        model.addAttribute("fio",user.getFIO());
        return "window-client";
    }

//оформление бронирования
    @GetMapping("/test/bookings/add")
    @PreAuthorize("hasAuthority('2')")
    public String bookingsAdd(Model model) {
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        model.addAttribute("message","Текущий пользователь:");
        model.addAttribute("fio",user.getFIO());
        return "bookings-add";
    }
    //бронирование будет поделено на две части: сначал выбор даты и кол-ва дней,
    //затем выбор комнаты и добавление сожителей.
    //было принято такое решение в связи с тем, что, во-первых, тег Календарь в HTML
    //не позволяет ограничить какие-то определенные даты для выбора,
    //а во-вторых, клиент прежде всего выбирает время, и только потом - комнату
    @PostMapping("/test/bookings/add_confirm")
    @PreAuthorize("hasAuthority('2')")
    public String bookingsAddConfirm(@RequestParam(required = false) Integer daysCount,
                                     @RequestParam(required = false) Date date,
                                     Model model){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        //сперва проверим, не является ли какое-то поле пустым
        if(daysCount == null || date == null){
            model.addAttribute("message","Какое-то поле пустое");
            model.addAttribute("fio",user.getFIO());
            return "bookings-add";
        }
        //затем проверим, не ввел ли клиент дату, которая раньше сегодняшнего дня
        Date today = new Date(System.currentTimeMillis());
        if(date.before(today)){
            model.addAttribute("message","Нельзя выбирать уже прошедшую дату");
            model.addAttribute("fio",user.getFIO());
            return "bookings-add";
        }
        //на следующее окно надо передать информацию о выбранной дате и времени.
        //так же надо подобрать комнаты, которые будут свободны в это время.
        model.addAttribute("daysCount",daysCount);
        model.addAttribute("date",date);
        //ниже - алгоритм подбора свободных комнат
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        //мы ищем список всех резервирований, которые "на рассмотрении" или "одобрены",
        //потому что именно они и только они ставят ограничение на даты
        BookingStatus bStatus1 = bookingStatusRepository.findById(1).get();
        BookingStatus bStatus2 = bookingStatusRepository.findById(2).get();
        //Нам нужно собрать список допустимых комнат. Сначала в нем будут
        //все комнаты, потом мы будем отнимать по одной комнате, если та
        //не удовлетворяет требованию. Оставшиеся комнаты - и есть те,
        // что нам нужны
        Iterable<Room> ITrooms = roomRepository.findAll();
        ArrayList<Room> rooms = new ArrayList<>();
        ITrooms.forEach(r -> rooms.add(r));
        //для вычисления нам понадобится дата, которая запланирована
        //клиентом для ухода
        Date departureDate = Date.valueOf(date.toLocalDate().plusDays(daysCount));
        ITbookings.forEach(b -> {
            if(b.getBookingStatus() == bStatus1 ||
                    b.getBookingStatus() == bStatus2){
                Date sDate = b.getPlannedSettlementDate();
                Date dDate = b.getPlannedDepartureDate();
                boolean mustBeRemoved = false;
                //это по сути классический for(int i...), но вместо i - iDate, т.е. дата-итератор,
                //цикл начинается с первого дня заселения клиента и продолжается до последнего дня
                //заселения клиента. С каждой итерацией итератор прибавляется на 1
                for(Date iDate = date; !mustBeRemoved && !iDate.equals(departureDate); iDate = Date.valueOf(iDate.toLocalDate().plusDays(1))){
                    if(iDate.equals(sDate) || iDate.equals(dDate)){
                        mustBeRemoved = true;
                    }
                }
                //эта переменная проверяет, что дата ухода клиента
                //"до" даты прихода другого клиента по проверяемому резервированию,
                //или дата прихода клиента "после" даты ухода другого клиента
                //по проверяемому резервированию. Если это так, то это резервирование
                //ничем не мешает. Если нет - убираем комнату этого резервирования
                if(mustBeRemoved){
                    rooms.remove(b.getRoom());
                }
            }
        });
        model.addAttribute("rooms",rooms);
        model.addAttribute("message","Оформляем бронирование");
        model.addAttribute("fio",user.getFIO());
        return "bookings-add-2";
    }
    //второй этап создания бронирования
    @PostMapping("/test/bookings/add_confirm2")
    @PreAuthorize("hasAuthority('2')")
    public String bookingsAddConfirm2(@RequestParam Integer roomId,
                                     @RequestParam Integer daysCount,
                                     @RequestParam Date date,
                                     @RequestParam(required = false) String surname1,
                                     @RequestParam(required = false) String name1,
                                     @RequestParam(required = false) String patr1,
                                     @RequestParam(required = false) String surname2,
                                     @RequestParam(required = false) String name2,
                                     @RequestParam(required = false) String patr2,
                                     @RequestParam(required = false) String surname3,
                                     @RequestParam(required = false) String name3,
                                     @RequestParam(required = false) String patr3,
                                     Model model){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        Booking booking = new Booking();
        BookingStatus status = bookingStatusRepository.findById(1).get();
        booking.setBookingStatus(status);
        status.addBooking(booking);
        User bufUser = userRepository.findById(user.getUserId()).get();
        bufUser.addBooking(booking);
        booking.setClientMakingBooking(bufUser);
        Room room = roomRepository.findById(roomId).get();
        booking.setRoom(room);
        room.addBooking(booking);
        booking.setBookingMakingDate(new Date(System.currentTimeMillis()));
        booking.setPlannedSettlementDate(date);
        Date departureDate = Date.valueOf(date.toLocalDate().plusDays(daysCount));
        booking.setPlannedDepartureDate(departureDate);
        bookingRepository.save(booking);

        if(settlingPersonStatusRepository.count() == 0){
            SettlingPersonStatus sStatus = new SettlingPersonStatus();
            sStatus.setSettlingPersonStatusName("Не прибыл(а)");
            settlingPersonStatusRepository.save(sStatus);
            sStatus = new SettlingPersonStatus();
            sStatus.setSettlingPersonStatusName("Прибыл(а)");
            settlingPersonStatusRepository.save(sStatus);
            sStatus = new SettlingPersonStatus();
            sStatus.setSettlingPersonStatusName("Отбыл(а)");
            settlingPersonStatusRepository.save(sStatus);
        }

        //добавление сожителей
        if(!surname1.isEmpty() || !name1.isEmpty() || !patr1.isEmpty()){
            SettlingPerson person = new SettlingPerson();
            if(!surname1.isEmpty()){
                person.setSurname(surname1);
            }else{
                person.setSurname("");

            }
            if(!name1.isEmpty()){
                person.setName(name1);
            }else{
                person.setName("");
            }
            if(!patr1.isEmpty()){
                person.setPatronymic(patr1);
            }else{
                person.setPatronymic("");
            }
            SettlingPersonStatus sStatus = settlingPersonStatusRepository.findById(1).get();
            person.setSettlingPersonStatus(sStatus);
            sStatus.addSettlingPerson(person);
            SettlingPersonByBooking spbb = new SettlingPersonByBooking();
            spbb.setBooking(booking);
            spbb.setSettlingPerson(person);
            settlingPersonByBookingRepository.save(spbb);
        }
        if(!surname2.isEmpty() || !name2.isEmpty() || !patr2.isEmpty()){
            SettlingPerson person = new SettlingPerson();
            if(!surname2.isEmpty()){
                person.setSurname(surname2);
            }else{
                person.setSurname("");
            }
            if(!name2.isEmpty()){
                person.setName(name2);
            }else{
                person.setName("");
            }
            if(!patr2.isEmpty()){
                person.setPatronymic(patr2);
            }else{
                person.setPatronymic("");
            }
            SettlingPersonStatus sStatus = settlingPersonStatusRepository.findById(1).get();
            person.setSettlingPersonStatus(sStatus);
            sStatus.addSettlingPerson(person);
            SettlingPersonByBooking spbb = new SettlingPersonByBooking();
            spbb.setBooking(booking);
            spbb.setSettlingPerson(person);
            settlingPersonByBookingRepository.save(spbb);
        }
        if(!surname3.isEmpty() || !name3.isEmpty() || !patr3.isEmpty()){
            SettlingPerson person = new SettlingPerson();
            if(!surname3.isEmpty()){
                person.setSurname(surname3);
            }else{
                person.setSurname("");
            }
            if(!name3.isEmpty()){
                person.setName(name3);
            }else{
                person.setName("");
            }
            if(!patr3.isEmpty()){
                person.setPatronymic(patr3);
            }else{
                person.setPatronymic("");
            }
            SettlingPersonStatus sStatus = settlingPersonStatusRepository.findById(1).get();
            person.setSettlingPersonStatus(sStatus);
            sStatus.addSettlingPerson(person);
            SettlingPersonByBooking spbb = new SettlingPersonByBooking();
            spbb.setBooking(booking);
            spbb.setSettlingPerson(person);
            settlingPersonByBookingRepository.save(spbb);
        }

        model.addAttribute("message","Бронирование оформлено");
        model.addAttribute("fio",user.getFIO());
        return "window-client";
    }
    //вторая кнопка - "Мои бронирования"
    @GetMapping("/test/bookings/ofClient")
    @PreAuthorize("hasAuthority('2')")
    public String bookingsOfClient(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        User bufUser = userRepository.findById(user.getUserId()).get();
        ITbookings.forEach(b -> {
            if(b.getClientMakingBooking() == bufUser &&
                    b.getBookingStatus().getBookingStatusId() != 3 &&
                    b.getBookingStatus().getBookingStatusId() != 4)
            unitsList.add(new Unit(b));
        });
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-ofClient";
    }
    @PostMapping("/test/bookings/{bookingId}/cancel")
    @PreAuthorize("hasAuthority('2')")
    public String bookingCancel(@PathVariable(value = "bookingId") int bookingId, Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Booking booking = bookingRepository.findById(bookingId).get();
        if(booking.getBookingStatus().getBookingStatusId() == 2) {
            model.addAttribute("message", "Удалить одобренное бронирование невозможно");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            User bufUser = userRepository.findById(user.getUserId()).get();
            ITbookings.forEach(b -> {
                if(b.getClientMakingBooking() == bufUser &&
                        b.getBookingStatus().getBookingStatusId() != 3 &&
                        b.getBookingStatus().getBookingStatusId() != 4)
                    unitsList.add(new Unit(b));
            });
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            return "bookings-ofClient";
        }else {
            User bufUser = userRepository.findById(user.getUserId()).get();
            BookingStatus previousStatus = booking.getBookingStatus();
            previousStatus.removeBooking(booking);
            BookingStatus bStatus4 = bookingStatusRepository.findById(4).get();
            booking.setBookingStatus(bStatus4);
            bStatus4.addBooking(booking);
            bookingStatusRepository.save(bStatus4);
            model.addAttribute("message", "Бронирование № " + booking.getBookingId() + " удалено");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(b -> {
                if(b.getClientMakingBooking() == bufUser &&
                        b.getBookingStatus().getBookingStatusId() != 3 &&
                        b.getBookingStatus().getBookingStatusId() != 4)
                    unitsList.add(new Unit(b));
            });
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            return "bookings-ofClient";
        }
    }
    @PostMapping("/test/bookings/{bookingId}/prolong")
    @PreAuthorize("hasAuthority('2')")
    public String bookingProlong(@PathVariable(value = "bookingId") int bookingId,
                                 @RequestParam(required = false) Integer daysCount,
                                 Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        Booking booking = bookingRepository.findById(bookingId).get();
        User bufUser = userRepository.findById(user.getUserId()).get();
        //сперва проверим, что в поле количества дней - не пустая строка
        if(daysCount == null){
            model.addAttribute("message", "Вы не указали количество дней");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(b -> {
                if(b.getClientMakingBooking() == bufUser &&
                        b.getBookingStatus().getBookingStatusId() != 3 &&
                        b.getBookingStatus().getBookingStatusId() != 4)
                    unitsList.add(new Unit(b));
            });
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            return "bookings-ofClient";
        }
        if(booking.getBookingStatus().getBookingStatusId() == 4){
            model.addAttribute("message", "Вы не можете продлить уже отмененное бронирование");
            model.addAttribute("fio", user.getFIO());
            Iterable<Booking> ITbookings = bookingRepository.findAll();
            ArrayList<Unit> unitsList = new ArrayList<>();
            ITbookings.forEach(b -> {
                if(b.getClientMakingBooking() == bufUser &&
                        b.getBookingStatus().getBookingStatusId() != 3 &&
                        b.getBookingStatus().getBookingStatusId() != 4)
                    unitsList.add(new Unit(b));
            });
            Iterable<Unit> ITunits = unitsList;
            model.addAttribute("bookings", ITunits);
            return "bookings-ofClient";
        }

        //на следующее окно надо передать информацию о выбранной дате и времени.
        //так же надо подобрать комнаты, которые будут свободны в это время.
        model.addAttribute("daysCount",daysCount);
        //ниже - алгоритм подбора свободных комнат
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        //мы ищем список всех резервирований, которые "одобрены",
        //потому что именно они и только они ставят ограничение на даты
        BookingStatus bStatus1 = bookingStatusRepository.findById(1).get();
        BookingStatus bStatus2 = bookingStatusRepository.findById(2).get();
        //Нам нужно собрать список допустимых комнат. Сначала в нем будут
        //все комнаты, потом мы будем отнимать по одной комнате, если та
        //не удовлетворяет требованию. Оставшиеся комнаты - и есть те,
        // что нам нужны
        Iterable<Room> ITrooms = roomRepository.findAll();
        ArrayList<Room> rooms = new ArrayList<>();
        ITrooms.forEach(r -> rooms.add(r));
        //для вычисления нам понадобится дата, которая запланирована
        //клиентом для ухода
        Date date = booking.getPlannedDepartureDate();
        Date departureDate = Date.valueOf(date.toLocalDate().plusDays(daysCount));
        ITbookings.forEach(b -> {
            if(b.getBookingStatus() == bStatus1 ||
                    b.getBookingStatus() == bStatus2){
                Date sDate = b.getPlannedSettlementDate();
                Date dDate = b.getPlannedDepartureDate();
                boolean mustBeRemoved = false;
                //это по сути классический for(int i...), но вместо i - iDate, т.е. дата-итератор,
                //цикл начинается с первого дня заселения клиента и продолжается до последнего дня
                //заселения клиента. С каждой итерацией итератор прибавляется на 1
                for(Date iDate = Date.valueOf(date.toLocalDate().plusDays(1));
                    !mustBeRemoved && !iDate.equals(departureDate); iDate = Date.valueOf(iDate.toLocalDate().plusDays(1))){
                    if(iDate.equals(sDate) || iDate.equals(dDate)){
                        mustBeRemoved = true;
                    }
                }
                //эта переменная проверяет, что дата ухода клиента
                //"до" даты прихода другого клиента по проверяемому резервированию,
                //или дата прихода клиента "после" даты ухода другого клиента
                //по проверяемому резервированию. Если это так, то это резервирование
                //ничем не мешает. Если нет - убираем комнату этого резервирования
                if(mustBeRemoved){
                    rooms.remove(b.getRoom());
                }
            }
        });
        model.addAttribute("rooms",rooms);
        model.addAttribute("message","Оформляем продление");
        model.addAttribute("fio",user.getFIO());
        ArrayList<Unit> unitsList = new ArrayList<>();
        unitsList.add(new Unit(booking));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-prolong";
    }
    @PostMapping("/test/bookings/{bookingId}/prolong_confirm")
    @PreAuthorize("hasAuthority('2')")
    public String bookingProlongs(@PathVariable(value = "bookingId") int bookingId,
                                  @RequestParam Integer roomId,
                                  @RequestParam Integer daysCount,
                                  Model model) {
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        Booking oldBooking = bookingRepository.findById(bookingId).get();
        Booking newBooking = new Booking();
        BookingStatus status = bookingStatusRepository.findById(1).get();
        newBooking.setBookingStatus(status);
        status.addBooking(newBooking);
        Room room = roomRepository.findById(roomId).get();
        newBooking.setRoom(room);
        room.addBooking(newBooking);
        User bufUser = userRepository.findById(user.getUserId()).get();
        newBooking.setClientMakingBooking(bufUser);
        bufUser.addBooking(newBooking);
        newBooking.setBookingMakingDate(new Date(System.currentTimeMillis()));
        newBooking.setPlannedSettlementDate(oldBooking.getPlannedDepartureDate());
        Date departureDate = Date.valueOf(
                oldBooking.getPlannedDepartureDate().toLocalDate().plusDays(daysCount));
        newBooking.setPlannedDepartureDate(departureDate);
        bookingRepository.save(newBooking);
        model.addAttribute("message", "Бронирование № "
                + oldBooking.getBookingId() + " продлено на " + daysCount + " дней");
        model.addAttribute("fio", user.getFIO());
        Iterable<Booking> ITbookings = bookingRepository.findAll();
        ArrayList<Unit> unitsList = new ArrayList<>();
        ITbookings.forEach(b -> {
            if(b.getClientMakingBooking() == bufUser &&
                    b.getBookingStatus().getBookingStatusId() != 3 &&
                    b.getBookingStatus().getBookingStatusId() != 4)
                unitsList.add(new Unit(b));
        });
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-ofClient";
    }

        //третья кнопка - "Мои заселения"
    @GetMapping("/test/settlements/ofClient")
    @PreAuthorize("hasAuthority('2')")
    public String settlementsOfClient(Model model){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        model.addAttribute("message","Текущий пользователь:");
        model.addAttribute("fio",user.getFIO());
        Iterable<Settlement> ITsettlements = settlementRepository.findByBookingClientMakingBooking(user);
        model.addAttribute("settlements", ITsettlements);
        return "settlements-ofClient";
    }
    @PostMapping("/test/bookings/{bookingId}/ofClient/one")
    @PreAuthorize("hasAuthority('2')")
    public String bookingOfClientOne(@PathVariable(value = "bookingId") int bookingId, Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails) userDetails).getUser();
        model.addAttribute("message", "Текущий пользователь:");
        model.addAttribute("fio", user.getFIO());
        Booking booking = bookingRepository.findById(bookingId).get();
        ArrayList<Unit> unitsList = new ArrayList<>();
        unitsList.add(new Unit(booking));
        Iterable<Unit> ITunits = unitsList;
        model.addAttribute("bookings", ITunits);
        return "bookings-ofClient";
    }
}