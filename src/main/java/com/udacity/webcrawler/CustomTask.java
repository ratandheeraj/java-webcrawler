package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.util.*;
import java.time.Instant;
import java.time.Clock;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class CustomTask extends RecursiveTask<Boolean> {
    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final String url;
    private final Instant deadLine;
    private final int maxDepth;
    private final ConcurrentMap<String, Integer> counts;
    private final List<Pattern> ignoredUrls;
    private final ConcurrentSkipListSet<String> visitedUrls;

    public CustomTask(Clock clock, String url, Instant deadLine, int maxDepth,
            ConcurrentMap<String, Integer> counts, ConcurrentSkipListSet<String> visitedUrls,
            List<Pattern> ignoredUrls, PageParserFactory parserFactory) {
        this.clock = clock;
        this.url = url;
        this.deadLine = deadLine;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }

    @Override
    protected Boolean compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadLine))
            return false;

        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches())
                return false;
        }
        if (!visitedUrls.add(url))
            return false;

        PageParser.Result result = parserFactory.get(url).parse();

        for (ConcurrentMap.Entry<String, Integer> entry : result.getWordCounts().entrySet()) {
            counts.compute(entry.getKey(),
                    (key, value) -> (value == null) ? entry.getValue() : entry.getValue() + value);
        }

        List<CustomTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CustomTask(clock, link, deadLine, maxDepth - 1, counts, visitedUrls, ignoredUrls,
                    parserFactory));
        }
        invokeAll(subtasks);
        return true;
    }
}