package com.valeerp.mrp;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mrp/summary")
public class MrpSummaryController {

    private final MrpSummaryService mrpSummaryService;

    public MrpSummaryController(MrpSummaryService mrpSummaryService) {
        this.mrpSummaryService = mrpSummaryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public MrpSummaryService.Summary summary() {
        return mrpSummaryService.summary();
    }
}
