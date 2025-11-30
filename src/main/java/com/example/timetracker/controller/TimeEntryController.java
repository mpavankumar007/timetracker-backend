package com.example.timetracker.controller;

import com.example.timetracker.dto.TimeEntryRequest;
import com.example.timetracker.model.TimeEntry;
import com.example.timetracker.repo.TimeEntryRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/time-entries")
@CrossOrigin(origins = "*")  // allow Chrome extension to call this
public class TimeEntryController {

    private final TimeEntryRepository repository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public TimeEntryController(TimeEntryRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public java.util.List<TimeEntry> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public TimeEntry create(@RequestBody TimeEntryRequest req) {
        TimeEntry entry = new TimeEntry();
        entry.setEmployeeId(req.getEmployeeId());
        entry.setWorkDate(LocalDate.parse(req.getWorkDate(), dateFormatter));
        entry.setStartTime(Instant.parse(req.getStartTime()));
        entry.setEndTime(Instant.parse(req.getEndTime()));
        entry.setDurationMs(req.getDurationMs());
        return repository.save(entry);
    }
}
