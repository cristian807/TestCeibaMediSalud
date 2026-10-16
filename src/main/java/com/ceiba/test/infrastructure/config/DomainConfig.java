package com.ceiba.test.infrastructure.config;

import com.ceiba.test.domain.port.HolidayCalendar;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.domain.repository.PenaltyRepository;
import com.ceiba.test.domain.service.AppointmentScheduler;
import com.ceiba.test.domain.service.AvailabilityCalculator;
import com.ceiba.test.domain.service.WorkingHoursPolicy;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public Clock clock(@Value("${app.clock.zone:America/Bogota}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    @Bean
    public WorkingHoursPolicy workingHoursPolicy(HolidayCalendar holidayCalendar) {
        return new WorkingHoursPolicy(holidayCalendar);
    }

    @Bean
    public AppointmentScheduler appointmentScheduler(AppointmentRepository appointments,
                                                     PenaltyRepository penalties, WorkingHoursPolicy workingHours,
                                                     @Value("${app.penalties.max:3}") int maxPenalties,
                                                     @Value("${app.penalties.window-days:30}") int windowDays) {
        return new AppointmentScheduler(appointments, penalties, workingHours, maxPenalties, windowDays);
    }

    @Bean
    public AvailabilityCalculator availabilityCalculator(WorkingHoursPolicy workingHours,
                                                         AppointmentRepository appointments) {
        return new AvailabilityCalculator(workingHours, appointments);
    }
}
