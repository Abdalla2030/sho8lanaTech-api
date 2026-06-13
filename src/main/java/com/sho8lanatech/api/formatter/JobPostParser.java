package com.sho8lanatech.api.formatter;

import com.sho8lanatech.api.dto.JobResponse;

import java.util.ArrayList;
import java.util.List;

public class JobPostParser {

    public static JobResponse parse(String formattedText) {

        JobResponse jobResponse = new JobResponse();

        jobResponse.setHashtags(new ArrayList<String>());
        jobResponse.setApply(new ArrayList<String>());

        if (formattedText == null || formattedText.trim().isEmpty()) {
            return jobResponse;
        }

        String[] lines = formattedText.split("\\r?\\n");

        String currentBlock = null;
        StringBuilder blockBuilder = new StringBuilder();

        for (String line : lines) {

            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (trimmedLine.startsWith("#")) {
                jobResponse.setHashtags(parseHashtags(trimmedLine));
                continue;
            }

            if (trimmedLine.startsWith("🤖")) {
                continue;
            }

            if (isInlineField(trimmedLine, "Title")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setTitle(getInlineValue(trimmedLine, "Title"));
                continue;
            }

            if (isInlineField(trimmedLine, "Company")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setCompany(getInlineValue(trimmedLine, "Company"));
                continue;
            }

            if (isInlineField(trimmedLine, "Level")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setLevel(getInlineValue(trimmedLine, "Level"));
                continue;
            }

            if (isInlineField(trimmedLine, "Location")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setLocation(getInlineValue(trimmedLine, "Location"));
                continue;
            }

            if (isInlineField(trimmedLine, "Work Mode")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setWorkMode(getInlineValue(trimmedLine, "Work Mode"));
                continue;
            }

            if (isInlineField(trimmedLine, "Employment Type")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setEmploymentType(getInlineValue(trimmedLine, "Employment Type"));
                continue;
            }

            if (isInlineField(trimmedLine, "Required Years")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setRequiredYears(getInlineValue(trimmedLine, "Required Years"));
                continue;
            }

            if (isInlineField(trimmedLine, "Apply")) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = null;
                jobResponse.setApply(parseApply(getInlineValue(trimmedLine, "Apply")));
                continue;
            }

            if ("Description:".equals(trimmedLine)) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = "Description";
                blockBuilder = new StringBuilder();
                continue;
            }

            if ("Responsibilities:".equals(trimmedLine)) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = "Responsibilities";
                blockBuilder = new StringBuilder();
                continue;
            }

            if ("Requirements:".equals(trimmedLine)) {
                flushBlock(jobResponse, currentBlock, blockBuilder);
                currentBlock = "Requirements";
                blockBuilder = new StringBuilder();
                continue;
            }

            if (currentBlock != null) {
                blockBuilder.append(trimmedLine).append("\n");
            }
        }

        flushBlock(jobResponse, currentBlock, blockBuilder);

        return jobResponse;
    }

    private static List<String> parseHashtags(String line) {

        List<String> hashtags = new ArrayList<String>();

        String[] values = line.split("\\s+");

        for (String value : values) {
            if (value.startsWith("#")) {
                hashtags.add(value.trim());
            }
        }

        return hashtags;
    }

    private static List<String> parseApply(String value) {

        List<String> applyList = new ArrayList<String>();

        if (value == null || value.trim().isEmpty()) {
            return applyList;
        }

        String[] values = value.split(",");

        for (String item : values) {
            if (!item.trim().isEmpty()) {
                applyList.add(item.trim());
            }
        }

        return applyList;
    }

    private static boolean isInlineField(String line, String label) {
        return line.startsWith(label + ":");
    }

    private static String getInlineValue(String line, String label) {
        return line.substring((label + ":").length()).trim();
    }

    private static void flushBlock(
            JobResponse jobResponse,
            String currentBlock,
            StringBuilder blockBuilder
    ) {

        if (currentBlock == null || blockBuilder == null) {
            return;
        }

        String value = blockBuilder.toString().trim();

        if (value.isEmpty()) {
            return;
        }

        if ("Description".equals(currentBlock)) {
            jobResponse.setDescription(value);
            return;
        }

        if ("Responsibilities".equals(currentBlock)) {
            appendToDescription(jobResponse, "Responsibilities:\n" + value);
            return;
        }

        if ("Requirements".equals(currentBlock)) {
            appendToDescription(jobResponse, "Requirements:\n" + value);
        }
    }

    private static void appendToDescription(JobResponse jobResponse, String value) {

        if (jobResponse.getDescription() == null || jobResponse.getDescription().trim().isEmpty()) {
            jobResponse.setDescription(value);
            return;
        }

        jobResponse.setDescription(jobResponse.getDescription() + "\n\n" + value);
    }
}
