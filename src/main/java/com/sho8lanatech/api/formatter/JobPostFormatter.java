package com.sho8lanatech.api.formatter;

import com.sho8lanatech.api.dto.JobAnalysisResponse;

public class JobPostFormatter {

    public static String format(JobAnalysisResponse job) {

        StringBuilder builder = new StringBuilder();

        if (job.getHashtags() != null && !job.getHashtags().isEmpty()) {

            for (String hashtag : job.getHashtags()) {
                builder.append(hashtag).append(" ");
            }

            builder.append("\n\n");
        }

        appendInlineField(builder, "Title", job.getTitle());
        appendInlineField(builder, "Company", job.getCompany());
        appendInlineField(builder, "Level", job.getLevel());
        appendInlineField(builder, "Location", job.getLocation());
        appendInlineField(builder, "Work Mode", job.getWork_mode());
        appendInlineField(builder, "Employment Type", job.getEmployment_type());
        appendInlineField(builder, "Required Years", job.getRequired_years());

        appendBlockField(builder, "Description", job.getDescription());

        appendInlineField(builder, "Apply", String.join(", ", job.getApply()));

        builder.append("\n");
        builder.append("🤖 Rewritten by AI via @sho8lanaTech");

        return builder.toString().trim();
    }

    private static void appendInlineField(
            StringBuilder builder,
            String label,
            String value
    ) {

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        builder.append(label)
                .append(": ")
                .append(value.trim())
                .append("\n\n");
    }

    private static void appendBlockField(
            StringBuilder builder,
            String label,
            String value
    ) {

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        builder.append(label)
                .append(":\n")
                .append(value.trim())
                .append("\n\n");
    }
}
