package com.SubVlad01.demo.controllers;

import com.SubVlad01.demo.MyUserDetails;
import com.SubVlad01.demo.models.*;
import com.SubVlad01.demo.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
public class MainController {
    //подключиться к БД
    @Autowired
    private DataSource dataSource;
    private final String BASE_NAME = "hoteldb";

    //репозитории
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
    private CityRepository cityRepository;
    @Autowired
    private SettlementStatusRepository settlementStatusRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message1","Мини-гостиница для идеального отдыха");
        Iterable<City> cities = cityRepository.findAll();
        model.addAttribute("cities", cities);
        return "index";
    }
    @GetMapping("/test/logout")
    public String logout(Model model, HttpServletRequest request){
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        model.addAttribute("message1","Мини-гостиница для идеального отдыха");
        Iterable<City> cities = cityRepository.findAll();
        model.addAttribute("cities", cities);
        return "index";
    }
    @GetMapping("/test/find")
    public String findWindow(Model model){
        String address = "index";
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((MyUserDetails)userDetails).getUser();
        if(user != null){
            if(user.getUserRole().getUserRoleId() == 1){
                return "redirect:/test/window/admin";
            }else if(user.getUserRole().getUserRoleId() == 2){
                address = "redirect:/test/window/client";
            }
        }else{
            model.addAttribute("message1","Такой пользователь не найден. Попробуйте снова");
            model.addAttribute("message2","");
            address = "index";
        }
        return address;
    }
    @PostMapping("/test/users/add")
    public String userAdd(@RequestParam String surname,
                          @RequestParam String name,
                          @RequestParam(required = false) String patr,
                          @RequestParam String phone,
                          @RequestParam(required = false) Integer cityId,
                          @RequestParam(required = false) String cityAdd,
                          @RequestParam(required = false) String email1,
                          @RequestParam(required = false) String passport,
                          @RequestParam String login,
                          @RequestParam String password1,
                          @RequestParam String repeatPassword,
                          Model model){


        if(Objects.equals(password1, repeatPassword)){
            if(userRepository.findByLogin(login).isPresent()){
                model.addAttribute("message1","Этот логин занят");
                Iterable<City> cities = cityRepository.findAll();
                model.addAttribute("cities", cities);
                return "index";
            }
            if(email1 != ""){
                if(!email1.matches("\\w+@\\w+.\\w+")){
                    model.addAttribute("message1","Эл.почта не соответствует");
                    model.addAttribute("message2","требуемом формату (XX@XX.XX)");
                    Iterable<City> cities = cityRepository.findAll();
                    model.addAttribute("cities", cities);
                    return "index";
                }
            }
            User user = new User();
            user.setSurname(surname);
            user.setName(name);
            if(!patr.isEmpty()){
                user.setPatronymic(patr);
            }else{
                user.setPatronymic("");
            }
            try{
                int ph = Integer.parseInt(phone);
                user.setContactPhoneNumber(ph);
            }catch (NumberFormatException e){
                model.addAttribute("message1","В поле ввода телефона должен быть введен");
                model.addAttribute("message2","телефон в виде числа без символов и букв");
                Iterable<City> cities = cityRepository.findAll();
                model.addAttribute("cities", cities);
                return "index";
            }
            user.setEmail(email1);
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
            user.setLogin(login);
            //добавление криптографичекого ключа, который шифрует поступающие
            //в базу пароли
            BCryptPasswordEncoder enc = new BCryptPasswordEncoder(5);
            user.setPassword(enc.encode(password1));

            //Этот блок срабатывает когда список пользователей пуст, т.е.
            //в самом начале работы программы.
            //Самый первый зарегистрированный пользователь становится
            //администратором. Остальные - клиентами.
            //Только первый пользователь сможет добавить новых администраторов
            if(userRepository.count() == 0){
                //метод ниже автоматически создает в БД триггеры, необходимые для
                //корректной работы приложения
                makeTriggers(BASE_NAME);
                if(cityRepository.count() == 0){
                    //добавляется дефолтный город, для первого администратора
                    City city = new City();
                    city.setCityName("(без города)");
                    cityRepository.save(city);
                    user.setCity(city);
                    city.addUser(user);
                }
                if(userRoleRepository.count() == 0){
                    UserRole userRole = new UserRole();
                    userRole.setUserRoleName("Администратор");
                    userRoleRepository.save(userRole);
                    userRole = new UserRole();
                    userRole.setUserRoleName("Клиент");
                    userRoleRepository.save(userRole);
                }
                if(bookingStatusRepository.count() == 0){
                    BookingStatus bStatus = new BookingStatus();
                    bStatus.setBookingStatusName("На рассмотрении");
                    bookingStatusRepository.save(bStatus);
                    bStatus = new BookingStatus();
                    bStatus.setBookingStatusName("Одобрено");
                    bookingStatusRepository.save(bStatus);
                    bStatus = new BookingStatus();
                    bStatus.setBookingStatusName("Выселены");
                    bookingStatusRepository.save(bStatus);
                    bStatus = new BookingStatus();
                    bStatus.setBookingStatusName("Отменено пользователем");
                    bookingStatusRepository.save(bStatus);
                }
                if(settlementStatusRepository.count() == 0){
                    SettlementStatus sStatus = new SettlementStatus();
                    sStatus.setSettlementStatusName("Заселено");
                    settlementStatusRepository.save(sStatus);
                    sStatus = new SettlementStatus();
                    sStatus.setSettlementStatusName("Выселено");
                    settlementStatusRepository.save(sStatus);
                }
                Optional<UserRole> role = userRoleRepository.findById(1);
                user.setUserRole(role.get());
                role.get().addUser(user);
            }
            else{
                Optional<UserRole> role = userRoleRepository.findById(2);
                user.setUserRole(role.get());
                role.get().addUser(user);
                City city;
                if(!cityAdd.isEmpty()){
                    city = new City();
                    city.setCityName(cityAdd);
                    cityRepository.save(city);
                }else{
                    city = cityRepository.findById(cityId).get();
                }
                user.setCity(city);
                city.addUser(user);
            }
            userRepository.save(user);
            model.addAttribute("message1","Мы рады видеть вас на нашем сервисе,");
            model.addAttribute("message2",user.getFIO());
            Iterable<City> cities = cityRepository.findAll();
            model.addAttribute("cities", cities);
        }else{
            model.addAttribute("message1","К сожалению, пароли в двух полях");
            model.addAttribute("message2","не совпадают");
            Iterable<City> cities = cityRepository.findAll();
            model.addAttribute("cities", cities);
        }
        return "index";
    }
    private void makeTriggers(String baseName){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        //запрос на создание триггеров для исправления ошибки фреймворка,
        //из-за которых после каждого нового включения приложения
        //неправильно выставляются первичные ключи: вместо прибавления на 1,
        //после нового запуска приложение прибавляется сначала 50, потом опять 1,
        //и это выглядело так: 1)room1, id-1, 2)room2, id-2, (перезапуск) 3)room3, id-53,
        //(перезапуск) 4)room4, id-104 и т.п.
        String queryGetTables = "show tables in hoteldb where Tables_in_"+baseName+" not like '%_seq'";
        List<Map<String, Object>> strings = jdbcTemplate.queryForList(queryGetTables);
        String query;
        for(int i = 0; i < strings.size(); i ++) {
            query = "CREATE TRIGGER `"+strings.get(i).get("Tables_in_" + baseName)+"_seq_trigger1` " +
                    "AFTER INSERT ON `"+strings.get(i).get("Tables_in_" + baseName)+"`\n" +
                    " FOR EACH ROW update "+strings.get(i).get("Tables_in_" + baseName)+"_seq\n" +
                    "set next_val = (select count("+strings.get(i).get("Tables_in_" + baseName)+"_id) from "+strings.get(i).get("Tables_in_" + baseName)+") + 50";
            jdbcTemplate.execute(query);
        }
        //триггер, который при создании нового заселения меняет соответствующему бронированию
        //статус на "заселено"
        query = "CREATE TRIGGER `settlement__trigger_2` AFTER INSERT ON `settlement`\n" +
                " FOR EACH ROW update booking \n" +
                "set booking_status_id = 2\n" +
                "where booking_id = NEW.booking_id";
        jdbcTemplate.execute(query);
        //триггер, который при добавлении заселения ставит в поле "Дата въезда"
        //текущую дату
        query = "CREATE TRIGGER `settlement__trigger_3` BEFORE INSERT ON `settlement`\n" +
                " FOR EACH ROW set NEW.actual_settlement_date = CURRENT_DATE()";
        jdbcTemplate.execute(query);
        //триггер, который при создании заселения всем заселившимся по бронированию
        //ставит статус "Прибыл(а)"
        query = "CREATE TRIGGER `settlement__trigger_4` BEFORE INSERT ON `settlement`\n" +
                " FOR EACH ROW update settling_person \n" +
                "left join settling_person_by_booking as spbb\n" +
                "on settling_person.settling_person_id = spbb.settling_person_id\n" +
                "set settling_person.settling_person_status_id = 2\n" +
                "where spbb.booking_id = NEW.booking_id";
        jdbcTemplate.execute(query);
    }
}