package com.feedback.report.application.port.out;

import java.util.List;

/** Finds who receives the report. */
public interface AdminDirectory {

    /** Emails of the admins whose address is verified; empty if there is none. */
    List<String> verifiedAdminEmails();
}
